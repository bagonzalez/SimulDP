package org.opensourcephysics.media.core;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.swing.*;

import org.opensourcephysics.tools.*;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.media.gif.*;
/**
 * A video capture utility using media classes.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class VideoGrabber extends VideoCaptureTool {

   /**
    * A shared video capture tool.
    */
  public static VideoGrabber VIDEO_CAPTURE_TOOL=null;
  static Dimension defaultSize = new Dimension (320, 240);

  BufferedImage scratch;
  VideoRecorder recorder;
  VideoType videoType;
  VideoPanel recorderPanel;
  VideoPanel playerPanel;
  String playerFileName;
  JFrame recorderFrame;
  JFrame playerFrame;
  Action clearAction;
  Action saveAsAction;
  Action recordAction;
  Action vidTypeAction;
  Action fpsAction;
  JButton clearButton;
  JButton saveAsButton;
  JCheckBox recordCheckBox;
  JCheckBox loopCheckBox;
  JComboBox vidTypeDropDown;
  JLabel fpsLabel;
  JComboBox fpsDropDown;
  boolean recording = true;
  boolean saved = false;
  int frameCount = 0;
  Dimension imageSize;
  int[] pixels = new int[1];
  Map<String, VideoType> vidTypes = new HashMap<String, VideoType>();
  boolean previewAll = false;

  /**
   * Constructor that uses default video dimensions.
   */
  public VideoGrabber() {
    this(defaultSize);
  }

  /**
   * Constructor that sets the video dimensions.
   * 
   * @param dim the dimension
   */
  public VideoGrabber(Dimension dim) {
    super(false);
    imageSize = dim;
    createGUI();
    vidTypeAction.actionPerformed(null);
    setRecording(false);
  }

  /**
	 * Gets the shared VideoGrabber.
	 *
	 * @return the shared VideoGrabber
	 */
	public static VideoGrabber getTool() {
	   if (VIDEO_CAPTURE_TOOL==null){
	    VIDEO_CAPTURE_TOOL = new VideoGrabber();
	   }
	   return VIDEO_CAPTURE_TOOL;
	}

  /**
	 * Gets the shared VideoGrabber and sets the video dimensions.
	 *
   * @param dim the dimension
	 * @return the shared VideoGrabber
	 */
	public static VideoGrabber getTool(Dimension dim) {
	   if (VIDEO_CAPTURE_TOOL==null){
	    VIDEO_CAPTURE_TOOL = new VideoGrabber(dim);
	   }
	   else {
	  	 VIDEO_CAPTURE_TOOL.imageSize = dim;
	  	 VIDEO_CAPTURE_TOOL.recorderPanel.setPreferredSize(dim);
	  	 VIDEO_CAPTURE_TOOL.recorderFrame.pack();
	   }
	   return VIDEO_CAPTURE_TOOL;
	}

  /**
   * Clear the video from the tool in preparation for a new video.
   */
  public void clear() {
//     setRecording(false);
     clearAction.actionPerformed(null);
  }

  /**
   * Adds a frame to the video if it is recording.
   *
   * @param image the frame to be added
   * @return true if frame was added
   */
  public boolean addFrame(BufferedImage image) {
    if (isRecording()) {
      try {
        int w = image.getWidth();
        int h = image.getHeight();
        if (pixels.length != w*h) pixels = new int[w*h];
        boolean newScratch = false;
        if (previewAll) scratch = null;
        else if (scratch == null || scratch.getWidth() != w || scratch.getHeight() != h) {
        	scratch = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        	newScratch = true;
        }
        BufferedImage copy = previewAll? new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB): scratch;
        image.getRaster().getDataElements(0, 0, w, h, pixels);
        copy.getRaster().setDataElements(0, 0, w, h, pixels);
        Video video = recorderPanel.getVideo();
        if (video == null) { // first frame added
          recorderPanel.setVideo(new ImageVideo(copy));
          recorderPanel.getPlayer().setReadoutTypes("frame"); //$NON-NLS-1$
          if (recorder instanceof GifVideoRecorder) {
          	int i = loopCheckBox.isSelected()? 0: 1;
          	((GifVideoRecorder)recorder).getGifEncoder().setRepeat(i);
          }
        }
        else if (previewAll) {
        	ImageVideo imageVid = (ImageVideo)video;
          imageVid.insert(new Image[] {copy}, imageVid.getFrameCount(), null);
        }
        else if (newScratch) {
          recorderPanel.setVideo(new ImageVideo(copy));
          recorderPanel.getPlayer().setReadoutTypes("frame"); //$NON-NLS-1$        	
        }
        recorder.addFrame(copy);
        frameCount++;
	      String item = (String)fpsDropDown.getSelectedItem();
	      double dt = 1000.0 / Double.parseDouble(item);
	      recorderPanel.getPlayer().getClipControl().setFrameDuration(dt);
	      recorderPanel.getPlayer().getVideoClip().setStepCount(frameCount);
	      recorderPanel.getPlayer().setStepNumber(frameCount-1);
        if (video == null) { // first frame added
          imageSize = new Dimension(image.getWidth(), image.getHeight());
          // give recorderPanel extra room so whole video is visible
          final Dimension dim = new Dimension(image.getWidth()+4,
                                              image.getHeight()+4);
          Runnable runner = new Runnable() {
            public void run() {
              recorderPanel.setPreferredSize(dim);
              recorderFrame.pack();
            }
          };
          SwingUtilities.invokeLater(runner);
          refreshGUI();
        }
        return true;
      }
      catch (IOException ex) {
        return false;
      }
    }
    return false;
  }

  /**
   * Sets the visibility.
   *
   * @param visible true to set this visible
   */
  public void setVisible(boolean visible) {
    recorderFrame.setVisible(visible);
  }

  /**
   * Gets the visibility.
   *
   * @return true if visible
   */
  public boolean isVisible() {
    return recorderFrame.isVisible();
  }

  /**
   * Sets the recording flag.
   *
   * @param record true to record rendered images
   */
  public void setRecording(boolean record) {
    recording = record;
    refreshGUI();
  }

  /**
   * Gets the recording flag.
   *
   * @return true if recording rendered images
   */
  public boolean isRecording() {
    return recording && recorder != null;
  }

  /**
   * Sets the video type.
   *
   * @param type the video type
   */
  public void setVideoType(VideoType type) {
    if (type == null || type == videoType) return;
    videoType = type;
    recorder = type.getRecorder();
    clearAction.actionPerformed(null);
  }

  /**
   * Sets the frame rate.
   *
   * @param fps the frame rate in frames per second
   */
  public void setFrameRate(int fps) {
  	fps = Math.max(fps, 1);
  	fps = Math.min(fps, 30);
  	int n = fpsDropDown.getItemCount();
  	for (int i = 0; i < n; i++) {
  		String item = (String)fpsDropDown.getItemAt(i);
  		int j = Integer.parseInt(item);
  		if (fps == j) {
  			fpsDropDown.setSelectedIndex(i);
  			return; 
  		}
  		if (fps > j) {
  	  	String s = String.valueOf(fps);
  	  	fpsDropDown.insertItemAt(s, i);
  	  	fpsDropDown.setSelectedItem(s);
  	  	return;
  		}
  	}
  }

  /**
   * Saves the video to a file and returns the file name.
   *
   * @return the name of the file, or null if not saved
   */
  public String saveVideoAs() {
    if (recorder != null) {
      try {
        return recorder.saveVideoAs();
      }
      catch (IOException ex) {
  			JOptionPane.showMessageDialog(null, ex.getMessage(),
          	"File Not Saved", JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
      }
    }
    return null;
  }

  /**
   * Gets the video recorder.
   *
   * @return the VideoRecorder
   */
  public VideoRecorder getRecorder() {
    return recorder;
  }

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    createActions();
    // create buttons and dropdowns
    clearButton = new JButton(clearAction);
    saveAsButton = new JButton(saveAsAction);
    recordCheckBox = new JCheckBox(recordAction);
    recordCheckBox.setOpaque(false);
    loopCheckBox = new JCheckBox();
    loopCheckBox.setOpaque(false);
    // fps label and dropdown
    fpsLabel = new JLabel();
    String[] rates = {"30", "25", "20", "15",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    				"12", "10", "8", "6",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    				"5", "4", "3", "2", "1"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    fpsDropDown = new JComboBox(rates) {
    	public Dimension getMaximumSize() {
    		return getMinimumSize();
    	}
    };
    fpsDropDown.addActionListener(fpsAction);
    // video type dropdown
    vidTypeDropDown = new JComboBox() {
    	public Dimension getMaximumSize() {
    		return getMinimumSize();
    	}
    };
    // add gif video type
    VideoType vidType = new GifVideoType();
    String desc = vidType.getDescription();
    vidTypes.put(desc, vidType);
    vidTypeDropDown.addItem(desc);
    // add QT video type, if available
    // modified by W. Christian to use reflection
    try {
      String name="org.opensourcephysics.media.quicktime.QTVideoType"; //$NON-NLS-1$
      Class<?> qtClass = Class.forName(name);
      vidType= (VideoType)qtClass.newInstance();
      desc = vidType.getDescription();
      vidTypes.put(desc, vidType);
      vidTypeDropDown.addItem(desc);
    } catch (Exception ex){
       // QT for Java not available
    } catch (Error err) {
       // QT for Java not available
    }
    // add image video type
    vidType = new ImageVideoType();
    desc = vidType.getDescription();
    vidTypes.put(desc, vidType);
    vidTypeDropDown.addItem(desc);
    vidTypeDropDown.addActionListener(vidTypeAction);
    // create and assemble frame
    recorderFrame = new JFrame();
    recorderFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    recorderFrame.setName("VideoCaptureTool"); //$NON-NLS-1$
    recorderFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
      	if (recorder instanceof ScratchVideoRecorder) {
      		ScratchVideoRecorder svr = (ScratchVideoRecorder)recorder;
          if (svr.scratchFile != null) {
	      		try {
							svr.saveScratch();
							svr.scratchFile.delete();
						}
						catch (IOException ex) {/** empty block */} 
          }
      	}
      }
    });
    JPanel contentPane = new JPanel(new BorderLayout());
    recorderFrame.setContentPane(contentPane);
    recorderPanel = new FixedSizeVideoPanel();
    recorderPanel.setPreferredSize(imageSize);
    // bottom panel contains player and buttonBar
    JPanel bottomPanel = new JPanel(new BorderLayout());
    contentPane.add(bottomPanel, BorderLayout.SOUTH);
    JToolBar playerBar = new JToolBar();
    playerBar.setFloatable(false);
    recorderPanel.getPlayer().setBorder(null);
    recorderPanel.setPlayerVisible(false);
    recorderPanel.getPlayer().setLoopingButtonVisible(false);
    contentPane.add(recorderPanel, BorderLayout.CENTER);
    JToolBar buttonBar = new JToolBar();
    buttonBar.setFloatable(false);
    if (previewAll) {
    	playerBar.add(recorderPanel.getPlayer());
      bottomPanel.add(playerBar, BorderLayout.CENTER);
    }
    else {
      buttonBar.add(recorderPanel.getPlayer().readout);
    }
    bottomPanel.add(buttonBar, BorderLayout.SOUTH);
    buttonBar.add(recordCheckBox);
    buttonBar.add(Box.createHorizontalGlue());
    buttonBar.add(clearButton);
    buttonBar.add(saveAsButton);
    // topBar contains other components
    JToolBar topBar = new JToolBar();
    topBar.setFloatable(false);
    contentPane.add(topBar, BorderLayout.NORTH);
    topBar.add(vidTypeDropDown);
    topBar.addSeparator();
    topBar.add(fpsLabel);
    topBar.add(fpsDropDown);
    topBar.addSeparator();
    topBar.add(loopCheckBox);
    topBar.add(Box.createHorizontalGlue());
    recorderFrame.pack();
    // position recorderFrame in top center of screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width - recorderFrame.getBounds().width) / 2;
    recorderFrame.setLocation(x, 0);
  }

  /**
   * Creates the actions.
   */
  protected void createActions() {
    clearAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        recorderPanel.setVideo(null);
        recorderPanel.getPlayer().getVideoClip().setStepCount(1);
        if (recorder != null) {
          String item = (String)fpsDropDown.getSelectedItem();
          double dt = 1000.0 / Double.parseDouble(item);
          recorder.setFrameDuration(dt);
          try {
            recorder.createVideo();
            frameCount = 0;
            saved = false;
            recorderPanel.setVideo(null);
            recorderPanel.getPlayer().setReadoutTypes("frame"); //$NON-NLS-1$        	
            recorderPanel.getPlayer().getVideoClip().setStepCount(1);
            refreshGUI();
          }
          catch (IOException ex) {ex.printStackTrace();}
        }
        System.gc(); // recover resources used by previous ImageVideo
      }
    };
    saveAsAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if (recorder != null) {
          try {
            String name = recorder.saveVideoAs();
            if (name != null) {
              Video video = videoType.getVideo(name);
              if (playerPanel == null) {
                playerPanel = new FixedSizeVideoPanel();
                playerFrame = new VideoFrame(playerPanel);
                int w = imageSize.width + 4;
                int h = imageSize.height + recorderPanel.getPlayer().height + 4;
                playerPanel.setPreferredSize(new Dimension(w, h));
                playerFrame.pack();
              }
              playerPanel.setVideo(video);
              if (loopCheckBox.isVisible() && loopCheckBox.isSelected()) {
              	playerPanel.getPlayer().setLooping(true);
              	playerPanel.getPlayer().play();
              }
              playerFrame.setVisible(true);
              saved = true;
              playerFileName = name;
              clearAction.actionPerformed(null);
            }
          }
          catch (IOException ex) {ex.printStackTrace();}
        }
      }
    };
    recordAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        recording = !recording;
        refreshGUI();
      }
    };
    vidTypeAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Object desc = vidTypeDropDown.getSelectedItem();
        VideoType vidType = vidTypes.get(desc);
        if (vidType != null) {
          setVideoType(vidType);
        }
      }
    };
    fpsAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	clearAction.actionPerformed(null);
      }
    };
  }


  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
    recordCheckBox.setSelected(isRecording());
    clearButton.setEnabled(frameCount != 0);
    saveAsButton.setEnabled(frameCount != 0);
    vidTypeDropDown.setEnabled(frameCount == 0);
    vidTypeDropDown.setSelectedItem(videoType.getDescription());
    fpsDropDown.setEnabled(frameCount == 0);
    fpsDropDown.setVisible(!(videoType instanceof ImageVideoType));
    fpsLabel.setVisible(!(videoType instanceof ImageVideoType));
    loopCheckBox.setEnabled(frameCount == 0);
    recorderPanel.getPlayer().readout.setEnabled(frameCount != 0);
    recordCheckBox.setText(MediaRes.getString("VideoGrabber.Action.Capture")); //$NON-NLS-1$
    loopCheckBox.setText(MediaRes.getString("VideoGrabber.Action.Loop")); //$NON-NLS-1$
    loopCheckBox.setVisible(videoType instanceof GifVideoType);
    fpsLabel.setText(MediaRes.getString("VideoGrabber.Label.PlayRate") + " "); //$NON-NLS-1$ //$NON-NLS-2$
    clearButton.setText(MediaRes.getString("VideoGrabber.Action.Clear")); //$NON-NLS-1$
    saveAsButton.setText(MediaRes.getString("VideoGrabber.Action.SaveAs")); //$NON-NLS-1$
    if (recordCheckBox.isSelected())
    	recorderFrame.setTitle(MediaRes.getString("VideoGrabber.Title.Capturing")  //$NON-NLS-1$
    				+ videoType.getDescription());
    else recorderFrame.setTitle(videoType.getDescription());
    if (playerFrame != null && playerFrame.isVisible())
    	playerFrame.setTitle(MediaRes.getString("VideoGrabber.Title.Saved")  //$NON-NLS-1$
    					+ XML.getName(playerFileName));
  }

  private class FixedSizeVideoPanel extends VideoPanel {
    FixedSizeVideoPanel() {
      setBackground(Color.black);
      setDrawingInImageSpace(true);
      setShowCoordinates(false);
    }

    protected void scale(ArrayList<Drawable> drawables) {
      // set image border so video size remains fixed
      double w = imageWidth;
      double wBorder = (getWidth() - w - 1) * 0.5 / w;
      double h = imageHeight;
      double hBorder = (getHeight() - h - 1) * 0.5 / h;
      double border = Math.min(wBorder, hBorder);
      super.setImageBorder(border);
      super.scale(drawables);
    }
  }

}
