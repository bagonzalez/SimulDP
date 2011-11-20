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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

import org.opensourcephysics.display.*;

/**
 * This displays and sets VideoClip properties.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class ClipInspector extends JDialog {

	// instance fields
  protected VideoClip clip;
  protected ClipControl clipControl;
  protected Video inVid, outVid;
  protected DrawingPanel inPanel, outPanel;
  protected JPanel dataPanel;
  protected JLabel startLabel;
  protected JLabel stepSizeLabel;
  protected JLabel t0Label;
  protected JLabel endLabel;
  protected JLabel dtLabel;
  protected JLabel rateLabel;
  protected IntegerField startField;
  protected IntegerField stepSizeField;
  protected NumberField t0Field;
  protected IntegerField endField;
  protected NumberField dtField;
  protected NumberField rateField;
  protected JButton okButton;
  protected JButton cancelButton;
  protected int prevFrame;
  protected int prevStart;
  protected int prevSize;
  protected int prevCount;
  protected double prevDt;
  protected double prevRate;

  /**
   * Constructs a ClipInspector.
   *
   * @param videoClip the video clip
   */
  public ClipInspector(VideoClip videoClip) {
    super((Frame)null, true); // modal dialog with no owner
    setTitle(MediaRes.getString("ClipInspector.Title")); //$NON-NLS-1$
    setResizable(false);
    clip = videoClip;
    createGUI();
    initialize();
    pack();
  }

  /**
   * Constructs a non-modal ClipInspector.
   *
   * @param videoClip the video clip
   * @param frame the owner
   */
  public ClipInspector(VideoClip videoClip, Frame frame) {
    super(frame, false); // non-modal dialog
    setTitle(MediaRes.getString("ClipInspector.Title")); //$NON-NLS-1$
    setResizable(false);
    clip = videoClip;
    createGUI();
    initialize();
    pack();
  }

  /**
   * Constructs a ClipInspector with access to the clip control.
   *
   * @param videoClip the video clip
   * @param control the clip control
   */
  public ClipInspector(VideoClip videoClip, ClipControl control) {
    super((Frame)null, true); // modal dialog with no owner
    setTitle(MediaRes.getString("ClipInspector.Title")); //$NON-NLS-1$
    setResizable(false);
    clip = videoClip;
    clipControl = control;
    createGUI();
    createClipControlGUI();
    initialize();
    pack();
  }

  /**
   * Constructs a non-modal ClipInspector with access to the clip control.
   *
   * @param videoClip the video clip
   * @param control the clip control
   * @param frame the owner
   */
  public ClipInspector(VideoClip videoClip, ClipControl control, Frame frame) {
    super(frame, false); // non-modal dialog
    setTitle(MediaRes.getString("ClipInspector.Title")); //$NON-NLS-1$
    setResizable(false);
    clip = videoClip;
    clipControl = control;
    createGUI();
    createClipControlGUI();
    initialize();
    pack();
  }

  /**
   * Enables the startField. When enabled, the startField sets the clip
   * start frame number.
   *
   * @param enabled <code>true</code> to enable the startField
   */
  public void setStartFrameEnabled(final boolean enabled) {
    Runnable enable = new Runnable() {
      public void run() {
        startField.setEnabled(enabled);
      }
    };
    try {
      EventQueue.invokeAndWait(enable);
    } catch(Exception ex) {
    	ex.printStackTrace();
    } 
  }

  /**
   * Enables the stepSizeField. When enabled, the stepSizeField sets the
   * clip step size.
   *
   * @param enabled <code>true</code> to enable the stepSizeField
   */
  public void setStepSizeEnabled(final boolean enabled) {
    Runnable enable = new Runnable() {
      public void run() {
        stepSizeField.setEnabled(enabled);
      }
    };
    try {
      EventQueue.invokeAndWait(enable);
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Enables the countField. When enabled, the countField sets the
   * clip step count. Changed to apply to endField 11/06 DB
   *
   * @param enabled <code>true</code> to enable the countField
   */
  public void setStepCountEnabled(final boolean enabled) {
    Runnable enable = new Runnable() {
      public void run() {
        endField.setEnabled(enabled);
      }
    };
    try {
      EventQueue.invokeAndWait(enable);
    } catch(Exception ex) {
    	ex.printStackTrace();
    }
  }

  /**
   * Initializes this clip inpector.
   */
  public void initialize() {
    updateDisplay();
    prevStart = clip.getStartFrameNumber();
    prevSize = clip.getStepSize();
    prevCount = clip.getStepCount();
    if (clipControl != null) {
      prevDt = clipControl.getMeanFrameDuration();
      prevRate = clipControl.getRate();
    }
    Video vid = clip.getVideo();
    if (vid != null) prevFrame = vid.getFrameNumber();
  }

  /**
   * Refreshes the GUI.
   */
  public void refresh() {
    setTitle(MediaRes.getString("ClipInspector.Title")); //$NON-NLS-1$
    startLabel.setText(MediaRes.getString("ClipInspector.Label.StartFrame")); //$NON-NLS-1$
    stepSizeLabel.setText(MediaRes.getString("ClipInspector.Label.StepSize")); //$NON-NLS-1$
    t0Label.setText(MediaRes.getString("ClipInspector.Label.StartTime")); //$NON-NLS-1$
    endLabel.setText(MediaRes.getString("ClipInspector.Label.EndFrame")); //$NON-NLS-1$
    cancelButton.setText(MediaRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
    okButton.setText(MediaRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
  	if (inPanel != null) {
  		inPanel.setMessage(MediaRes.getString("ClipInspector.StartFrame.Message")); //$NON-NLS-1$
      outPanel.setMessage(MediaRes.getString("ClipInspector.EndFrame.Message")); //$NON-NLS-1$
  	}
    if (clipControl != null) {
    	dtLabel.setText(MediaRes.getString("ClipInspector.Label.DeltaT")); //$NON-NLS-1$
      rateLabel.setText(MediaRes.getString("ClipInspector.Label.PlayRate")); //$NON-NLS-1$
    }
    pack();
  }

//_____________________________ private methods ____________________________

/**
 * Creates the visible components for the clip.
 */
  private void createGUI() {
    JPanel inspectorPanel = new JPanel(new BorderLayout());
    setContentPane(inspectorPanel);
    JPanel controlPanel = new JPanel(new BorderLayout());
    inspectorPanel.add(controlPanel, BorderLayout.SOUTH);
    // create in and out videos and panels
    Video video = clip.getVideo();
    if (video != null && video.getFrameCount() > 1) {
      inVid = VideoIO.clone(video);
      if (inVid != null) {
        outVid = VideoIO.clone(video);
        JPanel vidPanel = new JPanel(new GridLayout(1, 2));
        vidPanel.setBorder(BorderFactory.createEtchedBorder());
        inspectorPanel.add(vidPanel, BorderLayout.CENTER);
        inVid.setWidth(1);
        inPanel = new DrawingPanel();
        inPanel.setAutoscaleMargin(0.0);
        inPanel.setPreferredSize(new Dimension(160, 120));
        inPanel.addDrawable(inVid);
        inPanel.setMessage(MediaRes.getString("ClipInspector.StartFrame.Message")); //$NON-NLS-1$
        vidPanel.add(inPanel);
        outVid.setWidth(1);
        outPanel = new DrawingPanel();
        outPanel.setAutoscaleMargin(0.0);
        outPanel.setPreferredSize(new Dimension(160, 120));
        outPanel.addDrawable(outVid);
        outPanel.setMessage(MediaRes.getString("ClipInspector.EndFrame.Message")); //$NON-NLS-1$
        vidPanel.add(outPanel);
      }
    }
    // create start label and field
    startLabel = new JLabel(MediaRes.getString("ClipInspector.Label.StartFrame")); //$NON-NLS-1$
    startLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    startLabel.setForeground(new Color(0, 0, 102));
    startLabel.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
    startField = new IntegerField(4);
    startField.setMaximumSize(startField.getPreferredSize());
    startField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clip.setStartFrameNumber(startField.getIntValue());
        updateDisplay();
        startField.selectAll();
      }
    });
    startField.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        startField.selectAll();
      }

      public void focusLost(FocusEvent e) {
        clip.setStartFrameNumber(startField.getIntValue());
        updateDisplay();
      }
    });
    // create stepSize label and field
    stepSizeLabel = new JLabel(MediaRes.getString("ClipInspector.Label.StepSize")); //$NON-NLS-1$
    stepSizeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    stepSizeLabel.setForeground(new Color(0, 0, 102));
    stepSizeLabel.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
    stepSizeField = new IntegerField(4);
    stepSizeField.setMaximumSize(stepSizeField.getPreferredSize());
    stepSizeField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clip.setStepSize(stepSizeField.getIntValue());
        updateDisplay();
        stepSizeField.selectAll();
      }
    });
    stepSizeField.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        stepSizeField.selectAll();
      }

      public void focusLost(FocusEvent e) {
        clip.setStepSize(stepSizeField.getIntValue());
        updateDisplay();
      }
    });
    // create end frame label and field
    endLabel = new JLabel(MediaRes.getString("ClipInspector.Label.EndFrame")); //$NON-NLS-1$
    endLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    endLabel.setForeground(new Color(0, 0, 102));
    endLabel.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
    endField = new IntegerField(4);
    endField.setMaximumSize(endField.getPreferredSize());
    endField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clip.setEndFrameNumber(endField.getIntValue());
        updateDisplay();
        endField.selectAll();
      }
    });
    endField.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
      	endField.selectAll();
      }

      public void focusLost(FocusEvent e) {
        clip.setEndFrameNumber(endField.getIntValue());
        updateDisplay();
      }
    });
    // create start time label and field
    t0Label = new JLabel(MediaRes.getString("ClipInspector.Label.StartTime")); //$NON-NLS-1$
    t0Label.setAlignmentX(Component.RIGHT_ALIGNMENT);
    t0Label.setForeground(new Color(0, 0, 102));
    t0Label.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
    t0Field = new DecimalField(4, 3);
    t0Field.setUnits(" s"); //$NON-NLS-1$
    t0Field.setMaximumSize(t0Field.getPreferredSize());
    t0Field.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clip.setStartTime(t0Field.getValue()*1000);
        updateDisplay();
        t0Field.selectAll();
      }
    });
    t0Field.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
      	t0Field.selectAll();
      }

      public void focusLost(FocusEvent e) {
        clip.setStartTime(t0Field.getValue()*1000);
        updateDisplay();
      }
    });
    // create data panel and add labels and fields
    dataPanel = new JPanel(new GridLayout(1, 3));
    Border lined = BorderFactory.createLineBorder(Color.GRAY);
    Border empty = BorderFactory.createEmptyBorder(2, 6, 2, 6);
    dataPanel.setBorder(BorderFactory.createCompoundBorder(lined, empty));
    controlPanel.add(dataPanel, BorderLayout.CENTER);    
    GridBagConstraints c = new GridBagConstraints();
    // create startframe pane
    JPanel startPane = new JPanel(new GridBagLayout());
    c.gridwidth = 2;   // 2 columns wide
    c.weightx = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    c.gridx = 0;
    c.gridy = 0;
    startPane.add(startLabel, c);
    c.weightx = 0;
    c.anchor = GridBagConstraints.LINE_START;
    c.gridwidth = 1;   // 1 column wide
    c.gridx = 2;
    startPane.add(startField, c);
    dataPanel.add(startPane);
    // create stepsize pane
    JPanel sizePane = new JPanel(new GridBagLayout());
    c.weightx = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    c.gridwidth = 2;   // 2 columns wide
    c.gridx = 0;
    sizePane.add(stepSizeLabel, c);
    c.weightx = 0;
    c.anchor = GridBagConstraints.LINE_START;
    c.gridwidth = 1;   // 1 column wide
    c.gridx = 2;
    sizePane.add(stepSizeField, c);
    dataPanel.add(sizePane);
    // create endframe pane
    JPanel endPane = new JPanel(new GridBagLayout());
    c.weightx = 0.5;
    c.anchor = GridBagConstraints.LINE_END;
    c.gridwidth = 2;   // 2 columns wide
    c.gridx = 0;
    endPane.add(endLabel, c);
    c.weightx = 0;
    c.anchor = GridBagConstraints.LINE_START;
    c.gridwidth = 1;   // 1 column wide
    c.gridx = 2;
    endPane.add(endField, c);
    dataPanel.add(endPane);
    // create cancel button
    cancelButton = new JButton(MediaRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
    cancelButton.setForeground(new Color(0, 0, 102));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        revert();
        setVisible(false);
      }
    });
    // create ok button
    okButton = new JButton(MediaRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
    okButton.setForeground(new Color(0, 0, 102));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    // create buttonbar and add buttons
    JPanel buttonbar = new JPanel(new GridLayout(1, 4));
    buttonbar.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
    controlPanel.add(buttonbar, BorderLayout.SOUTH);
    buttonbar.add(Box.createHorizontalBox());
    buttonbar.add(okButton);
    buttonbar.add(cancelButton);
    buttonbar.add(Box.createHorizontalBox());
  }

  /**
   * Creates the visible components for the clip control.
   */
    private void createClipControlGUI() {
      // create dt label and field
      dtLabel = new JLabel(MediaRes.getString("ClipInspector.Label.DeltaT")); //$NON-NLS-1$
      dtLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
      dtLabel.setForeground(new Color(0, 0, 102));
      dtLabel.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
      dtField = new DecimalField(4, 3);
      dtField.setUnits(" s"); //$NON-NLS-1$
      dtField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          clipControl.setFrameDuration(dtField.getValue()*1000/clip.getStepSize());
          updateDisplay();
          dtField.selectAll();
        }
      });
      dtField.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) {
          dtField.selectAll();
        }

        public void focusLost(FocusEvent e) {
          clipControl.setFrameDuration(dtField.getValue()*1000/clip.getStepSize());
          updateDisplay();
        }
      });
      // create rate label and field
      rateLabel = new JLabel(MediaRes.getString("ClipInspector.Label.PlayRate")); //$NON-NLS-1$
      rateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
      rateLabel.setForeground(new Color(0, 0, 102));
      rateLabel.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
      rateField = new NumberField(4);
    	char d = rateField.format.getDecimalFormatSymbols().getDecimalSeparator();
    	String exp = "0"+d+"0E0"; //$NON-NLS-1$ //$NON-NLS-2$
    	String fixed = "0"; //$NON-NLS-1$
      rateField.setPatterns(new String[] {exp, fixed, fixed, fixed, exp});
      rateField.setMaxValue(1000000);
      rateField.setUnits("%"); //$NON-NLS-1$
      rateField.setMinValue(.000001);
      rateField.setMaximumSize(rateField.getPreferredSize());
      rateField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          clipControl.setRate(rateField.getValue()/100.0);
          updateDisplay();
          rateField.selectAll();
        }
      });
      rateField.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) {
          rateField.selectAll();
        }

        public void focusLost(FocusEvent e) {
          clipControl.setRate(rateField.getValue()/100.0);
          updateDisplay();
        }
      });
      // modify data panel and add labels and fields
      dataPanel.setLayout(new GridLayout(2, 3));
      GridBagConstraints c = new GridBagConstraints();
      // create t0 pane
      JPanel t0Pane = new JPanel(new GridBagLayout());
      c.weightx = 0.5;
      c.anchor = GridBagConstraints.LINE_END;
      c.gridwidth = 2;   // 2 columns wide
      c.gridx = 0;
      t0Pane.add(t0Label, c);
      c.weightx = 0;
      c.anchor = GridBagConstraints.LINE_START;
      c.gridwidth = 1;   // 1 column wide
      c.gridx = 2;
      t0Pane.add(t0Field, c);
      dataPanel.add(t0Pane);
      // create dt pane
      JPanel dtPane = new JPanel(new GridBagLayout());
      c.weightx = 0.5;
      c.anchor = GridBagConstraints.LINE_END;
      c.gridwidth = 2;   // 2 columns wide
      c.gridx = 0;
      c.gridy = 0;
      dtPane.add(dtLabel, c);
      c.weightx = 0;
      c.anchor = GridBagConstraints.LINE_START;
      c.gridwidth = 1;   // 1 column wide
      c.gridx = 2;
      dtPane.add(dtField, c);
      dataPanel.add(dtPane);
      // create rate pane
      JPanel ratePane = new JPanel(new GridBagLayout());
      c.weightx = 0.5;
      c.anchor = GridBagConstraints.LINE_END;
      c.gridwidth = 2;   // 2 columns wide
      c.gridx = 0;
      ratePane.add(rateLabel, c);
      c.weightx = 0;
      c.anchor = GridBagConstraints.LINE_START;
      c.gridwidth = 1;   // 1 column wide
      c.gridx = 2;
      ratePane.add(rateField, c);
      dataPanel.add(ratePane);
    }

  /**
   * Updates this clip inpector to reflect the current clip settings.
   */
  public void updateDisplay() {
    if (inVid != null) {
      inVid.setFrameNumber(clip.getStartFrameNumber());
      outVid.setFrameNumber(clip.getEndFrameNumber());
    }
    startField.setIntValue(clip.getStartFrameNumber());
    stepSizeField.setIntValue(clip.getStepSize());
    t0Field.setValue(clip.getStartTime()/1000);
    endField.setIntValue(clip.getEndFrameNumber());
    if (clipControl != null) {
      dtField.setValue(clip.getStepSize()*clipControl.getMeanFrameDuration()/1000);
      rateField.setValue(100 * clipControl.getRate());
    }
    repaint();
  }

  /**
   * Reverts to the previous clip settings.
   */
  private void revert() {
    clip.setStartFrameNumber(prevStart);
    clip.setStepSize(prevSize);
    clip.setStepCount(prevCount);
    if (clipControl != null) {
      clipControl.setRate(prevRate);
      clipControl.setFrameDuration(prevDt);
    }
    Video vid = clip.getVideo();
    if (vid != null) vid.setFrameNumber(prevFrame);
  }

}
