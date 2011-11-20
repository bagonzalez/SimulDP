/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2004  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.core;

import java.beans.*;
import java.text.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * This is a GUI component for playing a VideoClip.
 * It uses a subclass of ClipControl to control the clip and updates
 * its display based on PropertyChangeEvents it receives from the
 * ClipControl.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class VideoPlayer extends JComponent
                         implements PropertyChangeListener {

  // instance fields
  protected VideoPanel vidPanel;
  protected ClipControl clipControl;
  private String[] readoutTypes;
  private String readoutType;
  private boolean inspectorButtonVisible = false;
  protected int height = 54;
  // GUI elements
  private JToolBar toolbar;
  private JButton playButton;
  private JButton stepButton;
  private JButton backButton;
  private JButton loopButton;
  private JButton inspectorButton;
  private JSlider slider;
  JLabel readout;
  private Icon playIcon;
  private Icon grayPlayIcon;
  private Icon pauseIcon;
  private Icon resetIcon;
  private Icon loopIcon;
  private Icon noloopIcon;
  private NumberFormat timeFormat = NumberFormat.getNumberInstance();

  /**
   * Constructs a VideoPlayer to play the specified video clip.
   *
   * @param panel the video panel
   * @param clip the video clip
   */
  public VideoPlayer(VideoPanel panel, VideoClip clip) {
    this(panel);
    setVideoClip(clip);
  }

  /**
   * Constructs a VideoPlayer.
   *
   * @param panel the video panel
   */
  public VideoPlayer(VideoPanel panel) {
    vidPanel = panel;
    vidPanel.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        if (vidPanel.isPlayerVisible()) {
          setBounds();
          vidPanel.repaint();
        }
      }
    });
    createGUI();
    timeFormat.setMinimumIntegerDigits(1);
    timeFormat.setMaximumFractionDigits(3);
    timeFormat.setMinimumFractionDigits(3);
    clipControl = ClipControl.getControl(new VideoClip(null));
    clipControl.addPropertyChangeListener(this);
    getVideoClip().addPropertyChangeListener(this);
    updatePlayButton(false);
    slider.setMaximum(getVideoClip().getStepCount() - 1);
    setReadoutTypes("frame time step"); //$NON-NLS-1$
    refresh();
  }

  /**
   * Sets the video clip.
   *
   * @param clip the video clip
   */
  public void setVideoClip(VideoClip clip) {
    boolean playing = clipControl.isPlaying();
    stop();
    if (getVideoClip() == clip) {
      // save current control state
      boolean looping = clipControl.isLooping();
      double rate = clipControl.getRate();
      double duration = clipControl.getMeanFrameDuration();
      // replace clip control
      clipControl.removePropertyChangeListener(this);
      clipControl.dispose();
      clipControl = ClipControl.getControl(clip);
      clipControl.addPropertyChangeListener(this);
      // set state of new control
      clipControl.setLooping(looping);
      clipControl.setRate(rate);
      clipControl.setFrameDuration(duration);
      if (playing) clipControl.play();
      ClipInspector inspector = getVideoClip().inspector;
      if (inspector != null) inspector.clipControl = clipControl;
    }
    else {
      // clean up and replace old clip
      VideoClip oldClip = getVideoClip();
      oldClip.removePropertyChangeListener(this);
      oldClip.hideClipInspector();
	    // dispose of old video, if any
      Video video = oldClip.getVideo();
	    if (video != null) video.dispose();
	    oldClip.video = null;
      if (clip == null) clip = new VideoClip(null);
      clip.addPropertyChangeListener(this);
      // clean up and replace old clip control
      clipControl.removePropertyChangeListener(this);
      clipControl.dispose();
      clipControl = ClipControl.getControl(clip);
      clipControl.addPropertyChangeListener(this);
      // update display
      setReadoutTypes("frame time step"); //$NON-NLS-1$
      updatePlayButton(clipControl.isPlaying());
      updateLoopButton(clipControl.isLooping());
      slider.setMaximum(clip.getStepCount() - 1);
      updateReadout();
      firePropertyChange("videoclip", oldClip, clip); //$NON-NLS-1$
      System.gc();
    }
  }

  /**
   * Gets the video clip.
   *
   * @return the video clip
   */
  public VideoClip getVideoClip() {
    return clipControl.getVideoClip();
  }

  /**
   * Gets the current clip control.
   *
   * @return the clip control
   */
  public ClipControl getClipControl() {
    return clipControl;
  }

  /**
   * Sets the readout data types made available to the user.
   * The type initially displayed in the readout is the first listed.
   *
   * @param types a list of data types. Supported types are "time",
   * "step", and "frame".
   */
  public void setReadoutTypes(String types) {
    // put supported types into map sorted by list order
    TreeMap<Integer, String> map = new TreeMap<Integer, String>();
    String list = types.toLowerCase();
    int i = list.indexOf("time"); //$NON-NLS-1$
    if (i >= 0) map.put(new Integer(i), "time"); //$NON-NLS-1$
    i = list.indexOf("step"); //$NON-NLS-1$
    if (i >= 0) map.put(new Integer(i), "step"); //$NON-NLS-1$
    i = list.indexOf("frame"); //$NON-NLS-1$
    if (i >= 0) map.put(new Integer(i), "frame"); //$NON-NLS-1$
    if (map.isEmpty()) return;
    readoutTypes = map.values().toArray(new String[0]);
    setReadoutType(readoutTypes[0]);
  }

  /**
   * Sets the type of data displayed in the readout.
   *
   * @param type "time", "step", or "frame"
   */
  public void setReadoutType(String type) {
    String name = type.toLowerCase();
    if (name.indexOf("time") >= 0) { //$NON-NLS-1$
      readoutType = "time"; //$NON-NLS-1$
      readout.setToolTipText(MediaRes.getString("VideoPlayer.Readout.ToolTip.Time")); //$NON-NLS-1$
    } else if (name.indexOf("step") >= 0) { //$NON-NLS-1$
      readoutType = "step"; //$NON-NLS-1$
      readout.setToolTipText(MediaRes.getString("VideoPlayer.Readout.ToolTip.Step")); //$NON-NLS-1$
    } else if (name.indexOf("frame") >= 0) { //$NON-NLS-1$
      readoutType = "frame"; //$NON-NLS-1$
      readout.setToolTipText(MediaRes.getString("VideoPlayer.Readout.ToolTip.Frame")); //$NON-NLS-1$
    }
    // add type to readoutTypes if not already present
    boolean isListed = false;
    for (int i = 0; i < readoutTypes.length; i++)
      isListed = isListed || readoutTypes[i] == readoutType;
    if (!isListed) {
      String[] newList = new String[readoutTypes.length + 1];
      newList[0] = readoutType;
      for (int i = 0; i < readoutTypes.length; i++)
        newList[i + 1] = readoutTypes[i];
      readoutTypes = newList;
    }
    updateReadout();
  }

  /**
   * Plays the clip.
   */
  public void play() {
    clipControl.play();
  }

  /**
   * Stops at the next step.
   */
  public void stop() {
    clipControl.stop();
  }

  /**
   * Steps forward one step.
   */
  public void step() {
    clipControl.step();
    updatePlayButton(false);
  }

  /**
   * Steps back one step.
   */
  public void back() {
    clipControl.back();
    updatePlayButton(false);
  }

  /**
   * Sets the play rate.
   *
   * @param rate the desired rate
   */
  public void setRate(double rate) {
    clipControl.setRate(rate);
  }

  /**
   * Gets the play rate.
   *
   * @return the current rate
   */
  public double getRate() {
    return clipControl.getRate();
  }

  /**
   * Turns on/off looping.
   *
   * @param looping <code>true</code> to turn looping on
   */
  public void setLooping(boolean looping) {
    clipControl.setLooping(looping);
  }

  /**
   * Gets the looping status.
   *
   * @return <code>true</code> if looping is on
   */
  public boolean isLooping() {
    return clipControl.isLooping();
  }

  /**
   * Sets the step number.
   *
   * @param n the desired step number
   */
  public void setStepNumber(int n) {
    clipControl.setStepNumber(n);
  }

  /**
   * Gets the step number.
   *
   * @return the current step number
   */
  public int getStepNumber() {
    return clipControl.getStepNumber();
  }

  /**
   * Gets the current frame number.
   *
   * @return the frame number
   */
  public int getFrameNumber() {
    return clipControl.getFrameNumber();
  }

  /**
   * Gets the current time in milliseconds. Includes the start time defined by
   * the video clip.
   *
   * @return the current time
   */
  public double getTime() {
    return clipControl.getTime() + clipControl.clip.getStartTime();
  }

  /**
   * Gets the start time of the specified step in milliseconds. 
   * Includes the start time defined by the video clip.
   *
   * @param stepNumber the step number
   * @return the time
   */
  public double getStepTime(int stepNumber) {
    return clipControl.getStepTime(stepNumber) + clipControl.clip.getStartTime();
  }

  /**
   * Gets the start time of the specified frame in milliseconds. 
   * Includes the start time defined by the video clip.
   *
   * @param frameNumber the frame number
   * @return the time
   */
  public double getFrameTime(int frameNumber) {
    return clipControl.clip.getStartTime() 
    	- (clipControl.clip.getStartFrameNumber()-frameNumber)
    	* clipControl.getMeanFrameDuration();
  }

  /**
   * Gets the mean step duration in milliseconds for the current video clip.
   *
   * @return the mean step duration
   */
  public double getMeanStepDuration() {
    return getClipControl().getMeanFrameDuration() * getVideoClip().getStepSize();
  }

  /**
   * Shows or hides the inspector button. The inspector button shows
   * and hides the clip inspector.
   *
   * @param visible <code>true</code> to show the inspector button
   */
  public void setInspectorButtonVisible(final boolean visible) {
    if (visible == inspectorButtonVisible) return;
    Runnable runner = new Runnable() {
      public void run() {
        inspectorButtonVisible = visible;
        if (visible) toolbar.add(inspectorButton);
        else toolbar.remove(inspectorButton);
        toolbar.revalidate();
      }
    };
    EventQueue.invokeLater(runner);
  }

  /**
   * Shows or hides the looping button.
   *
   * @param visible <code>true</code> to show the looping button
   */
  public void setLoopingButtonVisible(final boolean visible) {
    Runnable runner = new Runnable() {
      public void run() {
        if (visible) toolbar.add(loopButton);
        else toolbar.remove(loopButton);
        toolbar.revalidate();
      }
    };
    EventQueue.invokeLater(runner);
  }

  /**
   * Responds to property change events. VideoPlayer listens for the
   * following events: "playing", "stepnumber". "frameduration" and "looping"
   * from ClipControl, and "startframe", "stepsize", "stepcount" and "starttime"
   * from VideoClip.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name.equals("stepnumber")) {       // from ClipControl //$NON-NLS-1$
      updateReadout();
      if (playButton.getIcon() == resetIcon)
        updatePlayButton(clipControl.isPlaying());
      firePropertyChange("stepnumber", null, e.getNewValue()); // to VideoPanel //$NON-NLS-1$
    }
    else if (name.equals("frameduration")) { // from ClipControl //$NON-NLS-1$
      updateReadout();
      firePropertyChange("frameduration", null, e.getNewValue()); // to VideoPanel //$NON-NLS-1$
    }
    else if (name.equals("playing")) {     // from ClipControl //$NON-NLS-1$
      boolean playing = ((Boolean)e.getNewValue()).booleanValue();
      updatePlayButton(playing);
    }
    else if (name.equals("looping")) {     // from ClipControl //$NON-NLS-1$
      boolean looping = ((Boolean)e.getNewValue()).booleanValue();
      updateLoopButton(looping);
    }
    else if (name.equals("stepcount")) {   // from VideoClip //$NON-NLS-1$
      slider.setMaximum(getVideoClip().getStepCount() - 1);
      updatePlayButton(clipControl.isPlaying());
    }
    else if (name.equals("stepsize")) {    // from VideoClip //$NON-NLS-1$
      updateReadout();
    }
    else if (name.equals("startframe")) {  // from VideoClip //$NON-NLS-1$
      updateReadout();
    }
    else if (name.equals("starttime")) {  // from VideoClip //$NON-NLS-1$
      updateReadout();
    }
  }

  /**
   * Refreshes the GUI.
   */
  public void refresh() {
    stepButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.StepForward.ToolTip")); //$NON-NLS-1$
    backButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.StepBack.ToolTip")); //$NON-NLS-1$
    inspectorButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.ClipSettings.ToolTip")); //$NON-NLS-1$
    loopButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.Looping.ToolTip")); //$NON-NLS-1$
    setReadoutType(readoutType);
    updatePlayButton(clipControl.isPlaying());
    updateLoopButton(clipControl.isLooping());
    if (getVideoClip().inspector != null) {
    	getVideoClip().inspector.refresh();
    }
  }

  //_________________ private methods and inner classes __________________

  /**
   * Sets the bounds of this player.
   */
  private void setBounds() {
    toolbar.revalidate();
    height = playButton.getPreferredSize().height + 8;
    int y = vidPanel.getHeight() - height;
    int w = vidPanel.getWidth();
    setBounds(0, y, w, height);
    toolbar.revalidate();
  }

  /**
   * Creates the visible components of this player.
   */
  private void createGUI() {
    setLayout(new BorderLayout());
    // create toolbar
    toolbar = new JToolBar();
    toolbar.setFloatable(false);
    add(toolbar, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEtchedBorder());
    // create play button
    playIcon = new VideoPlayerIcon("play"); //$NON-NLS-1$
    grayPlayIcon = new VideoPlayerIcon("grayplay"); //$NON-NLS-1$
    pauseIcon = new VideoPlayerIcon("pause"); //$NON-NLS-1$
    resetIcon = new VideoPlayerIcon("reset"); //$NON-NLS-1$
    playButton = new JButton(playIcon);
    playButton.setDisabledIcon(grayPlayIcon);
    playButton.setSelectedIcon(pauseIcon);
    playButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (playButton.isSelected()) stop();
        else if (getVideoClip().getStepCount() == clipControl.getStepNumber() + 1) {
          clipControl.setStepNumber(0);
          updatePlayButton(false);
        }
        else play();
      }
    });
    playButton.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
          if (playButton.isSelected()) stop();
          else if (getVideoClip().getStepCount() == clipControl.getStepNumber() + 1) {
            clipControl.setStepNumber(0);
            updatePlayButton(false);
          }
          else play();
        }
      }
    });
    // create step button
    stepButton = new JButton(new VideoPlayerIcon("step")); //$NON-NLS-1$
    stepButton.setDisabledIcon(new VideoPlayerIcon("graystep")); //$NON-NLS-1$
    stepButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        step();
      }
    });
    // create back button
    backButton = new JButton(new VideoPlayerIcon("back")); //$NON-NLS-1$
    backButton.setDisabledIcon(new VideoPlayerIcon("grayback")); //$NON-NLS-1$
    backButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        back();
      }
    });
  	// create mouse listener and add to step and back buttons
  	MouseListener listener = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
      	if (e.getSource() == stepButton) {
          firePropertyChange("stepbutton", null, new Boolean(true)); //$NON-NLS-1$
      	}
      	else firePropertyChange("backbutton", null, new Boolean(true)); //$NON-NLS-1$
      }
      public void mouseExited(MouseEvent e) {
      	if (e.getSource() == stepButton) {
          firePropertyChange("stepbutton", null, new Boolean(false)); //$NON-NLS-1$
      	}
      	else firePropertyChange("backbutton", null, new Boolean(false)); //$NON-NLS-1$
      }
  	};
  	stepButton.addMouseListener(listener);
  	backButton.addMouseListener(listener);
    // create slider
    slider = new JSlider(0, 0, 0);
    slider.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        int i = slider.getValue();
        if (i != getStepNumber())
          setStepNumber(i);
      }
    });
    InputMap im = slider.getInputMap(JComponent.WHEN_FOCUSED);
    ActionMap am = SwingUtilities.getUIActionMap(slider);
    am.put(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0)), null);
    am.put(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0)), null);
    slider.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
      	switch (e.getKeyCode()) {
      		case KeyEvent.VK_PAGE_UP:
      			back();
      			break;
      		case KeyEvent.VK_PAGE_DOWN:
      			step();
      			break;
      	}
      }
    });
    // create readout label
    readout = new JLabel() {
      // override size methods so label has same height as buttons
      public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.height = playButton.getPreferredSize().height;
        return dim;
      }
      public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        dim.height = playButton.getPreferredSize().height;
        return dim;
      }
      public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        dim.height = playButton.getPreferredSize().height;
        return dim;
      }
    };
    Border empty = BorderFactory.createEmptyBorder(0, 3, 0, 3);
    Border etch = BorderFactory.createEtchedBorder();
    Border compound = BorderFactory.createCompoundBorder(etch, empty);
    readout.setBorder(compound);
    readout.setForeground(new Color(204, 51, 51));
    readout.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (readoutTypes.length < 2) return;
        // inner popup menu listener class
        ActionListener listener = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setReadoutType(e.getActionCommand());
          }
        };
        // create popup menu and add menu items
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        for (int i = 0; i < readoutTypes.length; i++) {
          String type = readoutTypes[i];
          if (type.equals("step")) item = new JMenuItem(MediaRes.getString("VideoPlayer.Readout.MenuItem.Step")); //$NON-NLS-1$ //$NON-NLS-2$
          else if (type.equals("time")) item = new JMenuItem(MediaRes.getString("VideoPlayer.Readout.MenuItem.Time")); //$NON-NLS-1$ //$NON-NLS-2$
          else item = new JMenuItem(MediaRes.getString("VideoPlayer.Readout.MenuItem.Frame")); //$NON-NLS-1$
          item.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
          item.setActionCommand(type);
          item.addActionListener(listener);
          popup.add(item);
        }
        // show popup menu
        popup.show(readout, 0, readout.getHeight());
      }
    });
    // create inspector button
    inspectorButton = new JButton(new VideoPlayerIcon("inspector")); //$NON-NLS-1$
    inspectorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Frame frame = null;
        Container c = vidPanel.getTopLevelAncestor();
        if (c instanceof Frame) frame = (Frame)c;
        ClipInspector inspector = getVideoClip().getClipInspector(clipControl, frame);
        if (inspector.isVisible()) return;
        Point loc = inspector.getLocation();
        if (loc.x == 0 && loc.y == 0) {
          // center inspector on the video panel
          Dimension dim = vidPanel.getSize();
          Point p = vidPanel.getLocationOnScreen();
          int x = (p.x + dim.width - inspector.getBounds().width) / 2;
          int y = (p.y + dim.height - inspector.getBounds().height) / 2;
          inspector.setLocation(x, y);
        }
        inspector.initialize();
        inspector.setVisible(true);
      }
    });
    // create loop button
    loopIcon = new VideoPlayerIcon("loop"); //$NON-NLS-1$
    noloopIcon = new VideoPlayerIcon("noloop"); //$NON-NLS-1$
    loopButton = new JButton(noloopIcon);
    loopButton.setSelectedIcon(loopIcon);
    loopButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        setLooping(!loopButton.isSelected());
      }
    });
    loopButton.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
          setLooping(!loopButton.isSelected());
        }
      }
    });
    // add components to toolbar
    toolbar.add(readout);
    toolbar.add(playButton);
    toolbar.add(slider);
    toolbar.add(backButton);
    toolbar.add(stepButton);
    toolbar.add(loopButton);
    if (inspectorButtonVisible)
      toolbar.add(inspectorButton);
  }

  /**
   * Updates the play button based on the specified play state.
   *
   * @param playing <code>true</code> if the video is playing
   */
  private void updatePlayButton(boolean playing) {
    boolean canPlay = getVideoClip().getStepCount() > 1;
    playButton.setEnabled(canPlay);
    stepButton.setEnabled(canPlay);
    backButton.setEnabled(canPlay);
    playButton.setSelected(playing);
    if (playing) {
      playButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.Pause.ToolTip")); //$NON-NLS-1$
      playButton.setPressedIcon(pauseIcon);
      playButton.setIcon(pauseIcon);
    }
    else if (getVideoClip().getStepCount() <= clipControl.getStepNumber() + 1
        && getVideoClip().getStepCount() > 1) {
      playButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.Reset.ToolTip")); //$NON-NLS-1$
      playButton.setPressedIcon(resetIcon);
      playButton.setIcon(resetIcon);
    }
    else {
      playButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.Play.ToolTip")); //$NON-NLS-1$
      playButton.setPressedIcon(playIcon);
      playButton.setIcon(playIcon);
    }
  }

  /**
   * Updates the loop button based on the specified looping state.
   *
   * @param looping <code>true</code> if the video is looping
   */
  private void updateLoopButton(boolean looping) {
    if (looping == loopButton.isSelected()) return;
    loopButton.setSelected(looping);
    if (looping) {
      loopButton.setPressedIcon(loopIcon);
      loopButton.setIcon(loopIcon);
    } else {
      loopButton.setPressedIcon(noloopIcon);
      loopButton.setIcon(noloopIcon);
    }
  }

  /**
   * Updates the slider and readout based on the current step number.
   */
  private void updateReadout() {
    int stepNumber = clipControl.getStepNumber();
    slider.setValue(stepNumber);
    slider.setToolTipText(MediaRes.getString("VideoPlayer.Slider.ToolTip")+ stepNumber); //$NON-NLS-1$
    String display;
    if (readoutType.equals("step")) { //$NON-NLS-1$
      if (stepNumber < 10) display = "00" + stepNumber; //$NON-NLS-1$
      else if (stepNumber < 100) display = "0" + stepNumber; //$NON-NLS-1$
      else display = "" + stepNumber; //$NON-NLS-1$
    }
    else if (readoutType.equals("frame")) { //$NON-NLS-1$
      int n = clipControl.getFrameNumber();
      if (clipControl instanceof VideoClipControl) {
        n = getVideoClip().getVideo().getFrameNumber();
      }
      if (n < 10) display = "00" + n; //$NON-NLS-1$
      else if (n < 100) display = "0" + n; //$NON-NLS-1$
      else display = "" + n; //$NON-NLS-1$
    } else display = timeFormat.format(getTime() / 1000.0);
    readout.setText(display);
  }

/**
 * VideoPlayerIcon inner class
 */
  private class VideoPlayerIcon implements Icon {
    private Color shapeColor;
    private Color shadowColor;
    private Color hiliteColor;
    private Shape shape;
    private Shape shadow;
    private Shape hilite;

    /**
     * Constructs Icons for this video player.
     *
     * @param name the name of the icon
     */
    VideoPlayerIcon(String name) {
      if (name.equals("play")) createPlayIcon(); //$NON-NLS-1$
      else if (name.equals("grayplay")) {createPlayIcon(); setToGray();} //$NON-NLS-1$
      else if (name.equals("pause")) createPauseIcon(); //$NON-NLS-1$
      else if (name.equals("reset")) createResetIcon(); //$NON-NLS-1$
      else if (name.equals("step")) createStepIcon(); //$NON-NLS-1$
      else if (name.equals("graystep")) {createStepIcon(); setToGray();} //$NON-NLS-1$
      else if (name.equals("back")) createBackIcon(); //$NON-NLS-1$
      else if (name.equals("grayback")) {createBackIcon(); setToGray();} //$NON-NLS-1$
      else if (name.equals("inspector")) createInspectorIcon(); //$NON-NLS-1$
      else if (name.equals("loop")) createLoopIcon(); //$NON-NLS-1$
      else if (name.equals("noloop")) createNoloopIcon(); //$NON-NLS-1$
    }

    private void setToGray() {
      shapeColor = new Color(204, 204, 204);
      shadowColor = new Color(102, 102, 102);
    }

    /**
     * Gets the width of the icon.
     *
     * @return the width of the icon
     */
    public int getIconWidth() {
      return 14;
    }

    /**
     * Gets the height of the icon.
     *
     * @return the height of the icon
     */
    public int getIconHeight() {
      return 14;
    }

    /**
     * Paints the icon.
     *
     * @param c the component
     * @param _g the graphics context to paint on
     * @param x the x position of the icon
     * @param y the y position of the icon
     */
    public void paintIcon(Component c, Graphics _g, int x, int y) {
      if (shape == null) return;
      Graphics2D g = (Graphics2D)_g;
      // save current graphics paint and transform
      Paint gPaint = g.getPaint();
      AffineTransform gat = g.getTransform();
      g.translate(x, y);
      // render shapes
      g.setPaint(shapeColor);
      g.fill(shape);
      if (shadow != null) {
        g.setPaint(shadowColor);
        g.fill(shadow);
      }
      if (hilite != null) {
        g.setPaint(hiliteColor);
        g.fill(hilite);
      }
      // restore graphics paint and transform
      g.setPaint(gPaint);
      g.setTransform(gat);
    }

    /**
     * Creates a play icon.
     */
    private void createPlayIcon() {
      shapeColor = new Color(51, 204, 51);
      shadowColor = new Color(51, 102, 51);
      hiliteColor = Color.white;
      GeneralPath path = new GeneralPath();
      Stroke stroke = new BasicStroke();
      path.moveTo(4.5f, 4.5f);
      path.lineTo(4.5f, 10.5f);
      path.moveTo(5.5f, 5.5f);
      path.lineTo(5.5f, 9.5f);
      path.moveTo(6.5f, 5.5f);
      path.lineTo(6.5f, 9.5f);
      path.moveTo(7.5f, 6.5f);
      path.lineTo(7.5f, 8.5f);
      path.moveTo(8.5f, 6.5f);
      path.lineTo(8.5f, 8.5f);
      path.moveTo(9.5f, 7.5f);
      path.lineTo(10.5f, 7.5f);
      shape = stroke.createStrokedShape(path);
      path.reset();
      path.moveTo(4.5f, 3.5f);
      path.lineTo(3.5f, 3.5f);
      path.lineTo(3.5f, 10.5f);
      path.moveTo(5.5f, 4.5f);
      path.lineTo(6.5f, 4.5f);
      path.moveTo(7.5f, 5.5f);
      path.lineTo(8.5f, 5.5f);
      path.moveTo(9.5f, 6.5f);
      path.lineTo(10.5f, 6.5f);
      shadow = stroke.createStrokedShape(path);
      path.reset();
      path.moveTo(3.5f, 11.5f);
      path.lineTo(4.5f, 11.5f);
      path.moveTo(5.5f, 10.5f);
      path.lineTo(6.5f, 10.5f);
      path.moveTo(7.5f, 9.5f);
      path.lineTo(8.5f, 9.5f);
      path.moveTo(9.5f, 8.5f);
      path.lineTo(10.5f, 8.5f);
      hilite = stroke.createStrokedShape(path);
    }

    /**
     * Creates a reset icon.
     */
    private void createResetIcon() {
      shapeColor = Color.black;
      shadowColor = new Color(51, 51, 51);
      hiliteColor = Color.white;
      GeneralPath path = new GeneralPath();
      Stroke stroke = new BasicStroke();
      path.moveTo(4.5f, 4.5f);
      path.lineTo(4.5f, 10.5f);
      path.moveTo(9.5f, 4.5f);
      path.lineTo(9.5f, 10.5f);
      path.moveTo(8.5f, 5.5f);
      path.lineTo(8.5f, 9.5f);
      path.moveTo(7.5f, 6.5f);
      path.lineTo(7.5f, 8.5f);
      path.moveTo(6.5f, 7.5f);
      path.lineTo(6.5f, 8.0f);
      shape = stroke.createStrokedShape(path);
      path.reset();
      path.moveTo(4.5f, 3.5f);
      path.lineTo(5.5f, 3.5f);
      path.lineTo(5.5f, 10.5f);
      path.moveTo(6.5f, 6.5f);
      path.lineTo(7.0f, 6.5f);
      path.moveTo(7.5f, 5.5f);
      path.lineTo(8.0f, 5.5f);
      path.moveTo(8.5f, 4.5f);
      path.lineTo(9.0f, 4.5f);
      path.moveTo(9.5f, 3.5f);
      path.lineTo(10.5f, 3.5f);
      path.lineTo(10.5f, 10.5f);
      shadow = stroke.createStrokedShape(path);
      path.reset();
      path.moveTo(5.5f, 11.5f);
      path.lineTo(4.5f, 11.5f);
      path.moveTo(10.5f, 11.5f);
      path.lineTo(9.5f, 11.5f);
      path.moveTo(8.5f, 10.5f);
      path.lineTo(9.0f, 10.5f);
      path.moveTo(7.5f, 9.5f);
      path.lineTo(8.0f, 9.5f);
      path.moveTo(6.5f, 8.5f);
      path.lineTo(7.0f, 8.5f);
      hilite = stroke.createStrokedShape(path);
    }

    /**
     * Creates a pause icon.
     */
    private void createPauseIcon() {
      shapeColor = new Color(204, 51, 51);
      shadowColor = new Color(102, 51, 51);
      GeneralPath path = new GeneralPath();
      Stroke stroke = new BasicStroke();
      path.moveTo(5.5f, 4.5f);
      path.lineTo(5.5f, 10.5f);
      path.moveTo(9.5f, 4.5f);
      path.lineTo(9.5f, 10.5f);
      shape = stroke.createStrokedShape(path);
      path.reset();
      path.moveTo(5.5f, 3.5f);
      path.lineTo(4.5f, 3.5f);
      path.lineTo(4.5f, 10.5f);
      path.moveTo(9.5f, 3.5f);
      path.lineTo(8.5f, 3.5f);
      path.lineTo(8.5f, 10.5f);
      shadow = stroke.createStrokedShape(path);
    }

    /**
     * Creates a step icon.
     */
    private void createStepIcon() {
      shapeColor = new Color(51, 51, 204);
      shadowColor = new Color(51, 51, 102);
      hiliteColor = Color.white;
      GeneralPath path = new GeneralPath();
      Stroke stroke = new BasicStroke();
      path.moveTo(4.5f, 4.5f);
      path.lineTo(4.5f, 10.5f);
      path.moveTo(7.5f, 4.5f);
      path.lineTo(7.5f, 10.5f);
      path.moveTo(8.5f, 5.5f);
      path.lineTo(8.5f, 9.5f);
      path.moveTo(9.5f, 6.5f);
      path.lineTo(9.5f, 8.5f);
      path.moveTo(10.5f, 7.5f);
      path.lineTo(10.5f, 8.0f);
      shape = stroke.createStrokedShape(path);
      path.reset();
      path.moveTo(4.5f, 3.5f);
      path.lineTo(3.5f, 3.5f);
      path.lineTo(3.5f, 10.5f);
      path.moveTo(10.5f, 6.5f);
      path.lineTo(11.0f, 6.5f);
      path.moveTo(9.5f, 5.5f);
      path.lineTo(10.0f, 5.5f);
      path.moveTo(8.5f, 4.5f);
      path.lineTo(9.0f, 4.5f);
      path.moveTo(7.5f, 3.5f);
      path.lineTo(6.5f, 3.5f);
      path.lineTo(6.5f, 10.5f);
      shadow = stroke.createStrokedShape(path);
      path.reset();
      path.moveTo(3.5f, 11.5f);
      path.lineTo(4.5f, 11.5f);
      path.moveTo(6.5f, 11.5f);
      path.lineTo(7.5f, 11.5f);
      path.moveTo(8.5f, 10.5f);
      path.lineTo(9.0f, 10.5f);
      path.moveTo(9.5f, 9.5f);
      path.lineTo(10.0f, 9.5f);
      path.moveTo(10.5f, 8.5f);
      path.lineTo(11.0f, 8.5f);
      hilite = stroke.createStrokedShape(path);
    }

    /**
     * Creates a back icon.
     */
    private void createBackIcon() {
      shapeColor = new Color(51, 51, 204);
      shadowColor = new Color(51, 51, 102);
      hiliteColor = Color.white;
      GeneralPath path = new GeneralPath();
      Stroke stroke = new BasicStroke();
      path.moveTo(9.5f, 4.5f);
      path.lineTo(9.5f, 10.5f);
      path.moveTo(6.5f, 4.5f);
      path.lineTo(6.5f, 10.5f);
      path.moveTo(5.5f, 5.5f);
      path.lineTo(5.5f, 9.5f);
      path.moveTo(4.5f, 6.5f);
      path.lineTo(4.5f, 8.5f);
      path.moveTo(3.5f, 7.5f);
      path.lineTo(3.5f, 8.0f);
      shape = stroke.createStrokedShape(path);
      path.reset();
      path.moveTo(9.5f, 3.5f);
      path.lineTo(10.5f, 3.5f);
      path.lineTo(10.5f, 10.5f);
      path.moveTo(3.5f, 6.5f);
      path.lineTo(4.0f, 6.5f);
      path.moveTo(4.5f, 5.5f);
      path.lineTo(5.0f, 5.5f);
      path.moveTo(5.5f, 4.5f);
      path.lineTo(6.0f, 4.5f);
      path.moveTo(6.5f, 3.5f);
      path.lineTo(7.5f, 3.5f);
      path.lineTo(7.5f, 10.5f);
      shadow = stroke.createStrokedShape(path);
      path.reset();
      path.moveTo(10.5f, 11.5f);
      path.lineTo(9.5f, 11.5f);
      path.moveTo(7.5f, 11.5f);
      path.lineTo(6.5f, 11.5f);
      path.moveTo(5.5f, 10.5f);
      path.lineTo(6.0f, 10.5f);
      path.moveTo(4.5f, 9.5f);
      path.lineTo(5.0f, 9.5f);
      path.moveTo(3.5f, 8.5f);
      path.lineTo(4.0f, 8.5f);
      hilite = stroke.createStrokedShape(path);
    }

    /**
     * Creates a clip icon.
     */
    private void createInspectorIcon() {
      shapeColor = new Color(51, 51, 51);
      shadowColor = Color.black;
      hiliteColor = new Color(153, 153, 255);
      GeneralPath path = new GeneralPath();
      Stroke stroke = new BasicStroke();
      path.moveTo(8.5f, 12.5f);
      path.lineTo(2.5f, 12.5f);
      path.lineTo(2.5f, 2.5f);
      path.lineTo(4.5f, 2.5f);
      path.lineTo(4.5f, 11.5f);
      path.moveTo(3.5f, 8.5f);
      path.lineTo(12.5f, 8.5f);
      path.lineTo(12.5f, 2.5f);
      path.lineTo(10.5f, 2.5f);
      path.lineTo(10.5f, 7.5f);
      path.moveTo(3.5f, 4.5f);
      path.lineTo(11.5f, 4.5f);
      path.moveTo(3.5f, 6.5f);
      path.lineTo(3.5f, 6.5f);
      path.moveTo(3.5f, 10.5f);
      path.lineTo(3.5f, 10.5f);
      path.moveTo(11.5f, 6.5f);
      path.lineTo(11.5f, 6.5f);
      shape = stroke.createStrokedShape(path);
      path.reset();
      path.moveTo(9.5f, 10.5f);
      path.lineTo(13.5f, 10.5f);
      path.moveTo(10.5f, 11.5f);
      path.lineTo(12.5f, 11.5f);
      path.moveTo(11.5f, 12.5f);
      path.lineTo(12.0f, 12.5f);
      shadow = stroke.createStrokedShape(path);
      path.reset();
      path.moveTo(5.5f, 2.5f);
      path.lineTo(9.5f, 2.5f);
      path.moveTo(5.5f, 3.5f);
      path.lineTo(9.5f, 3.5f);
      path.moveTo(5.5f, 5.5f);
      path.lineTo(9.5f, 5.5f);
      path.moveTo(5.5f, 6.5f);
      path.lineTo(9.5f, 6.5f);
      path.moveTo(5.5f, 7.5f);
      path.lineTo(9.5f, 7.5f);
      path.moveTo(5.5f, 9.5f);
      path.lineTo(7.5f, 9.5f);
      path.moveTo(5.5f, 10.5f);
      path.lineTo(6.5f, 10.5f);
      path.moveTo(5.5f, 11.5f);
      path.lineTo(7.5f, 11.5f);
      hilite = stroke.createStrokedShape(path);
    }

    /**
     * Creates a loop icon.
     */
    private void createLoopIcon() {
      shapeColor = new Color(0, 153, 0);
      GeneralPath path = new GeneralPath();
      Stroke stroke = new BasicStroke();
      path.moveTo(3.5f, 4.5f);
      path.lineTo(3.5f, 2.5f);
      path.lineTo(10.5f, 2.5f);
      path.lineTo(10.5f, 7.5f);
      path.moveTo(7.5f, 4.5f);
      path.lineTo(13.5f, 4.5f);
      path.moveTo(8.5f, 5.5f);
      path.lineTo(12.5f, 5.5f);
      path.moveTo(9.5f, 6.5f);
      path.lineTo(11.5f, 6.5f);
      path.moveTo(10.5f, 9.5f);
      path.lineTo(10.5f, 11.5f);
      path.lineTo(3.5f, 11.5f);
      path.lineTo(3.5f, 6.5f);
      path.moveTo(0.5f, 9.5f);
      path.lineTo(6.5f, 9.5f);
      path.moveTo(1.5f, 8.5f);
      path.lineTo(5.5f, 8.5f);
      path.moveTo(2.5f, 7.5f);
      path.lineTo(4.5f, 7.5f);
      shape = stroke.createStrokedShape(path);
    }

    /**
     * Creates a noloop icon.
     */
    private void createNoloopIcon() {
      shapeColor = new Color(0, 153, 0);
      shadowColor = Color.black;
      GeneralPath path = new GeneralPath();
      Stroke stroke = new BasicStroke();
      path.moveTo(3.5f, 4.5f);
      path.lineTo(3.5f, 2.5f);
      path.lineTo(10.5f, 2.5f);
      path.lineTo(10.5f, 7.5f);
      path.moveTo(7.5f, 4.5f);
      path.lineTo(13.5f, 4.5f);
      path.moveTo(8.5f, 5.5f);
      path.lineTo(12.5f, 5.5f);
      path.moveTo(9.5f, 6.5f);
      path.lineTo(11.5f, 6.5f);
      shape = stroke.createStrokedShape(path);
      stroke = new BasicStroke(2);
      path.reset();
      path.moveTo(9.0f, 9.0f);
      path.lineTo(12.5f, 9.0f);
      shadow = stroke.createStrokedShape(path);
    }
  }
}
