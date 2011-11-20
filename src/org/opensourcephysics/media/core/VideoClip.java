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
import java.io.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.tools.*;

/**
 * This defines a subset of video frames called steps.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class VideoClip {

  // instance fields
  private int startFrame = 0;
  private int stepSize = 1;
  private int stepCount = 10; // default stepCount if video is null
  private double startTime = 0; // start time in milliseconds
  protected Video video = null;
  private int[] stepFrames;
  ClipInspector inspector;
  private PropertyChangeSupport support;

  /**
   * Constructs a VideoClip.
   *
   * @param video the video
   */
  public VideoClip(Video video) {
    support = new SwingPropertyChangeSupport(this);
    this.video = video;
    if (video != null) {
    	video.setProperty("videoclip", this); //$NON-NLS-1$
      setStartFrameNumber(video.getStartFrameNumber());
      if (video.getFrameCount() > 1) {
        setStepCount(video.getEndFrameNumber() - startFrame + 1);
      }
    }
    updateArray();
  }

  /**
   * Gets the video.
   *
   * @return the video
   */
  public Video getVideo() {
    return video;
  }

  /**
   * Sets the start frame number.
   *
   * @param start the desired start frame number
   */
  public void setStartFrameNumber(int start) {
    if (startFrame == start) return;
    start = Math.abs(start);
    int endFrame = startFrame + stepSize * (stepCount-1);
    if (video != null && video.getFrameCount() > 1) {
      video.setEndFrameNumber(video.getFrameCount()-1);
      video.setStartFrameNumber(start);
      startFrame = video.getStartFrameNumber();
      int max = Math.max(video.getFrameCount() - startFrame - 1, 1);
      if (max < stepSize) stepSize = max;
    }
    else {
      startFrame = start;
      updateArray();
    }
    // reset end frame
    setEndFrameNumber(endFrame);
    support.firePropertyChange("startframe", null, new Integer(start)); //$NON-NLS-1$
  }

  /**
   * Gets the start frame number.
   *
   * @return the start frame number
   */
  public int getStartFrameNumber() {
    return startFrame;
  }

  /**
   * Sets the step size.
   *
   * @param size the desired step size
   */
  public void setStepSize(int size) {
    if (size == 0) return;
    size = Math.abs(size);
    if (video != null && video.getFrameCount() > 1) {
      int maxSize = Math.max(video.getFrameCount() - startFrame - 1, 1);
      size = Math.min(size, maxSize);
    }
    if (stepSize != size) {
    	// get current end frame
      int endFrame = startFrame + stepSize * (stepCount-1);
      stepSize = size;
      support.firePropertyChange("stepsize", null, new Integer(size)); //$NON-NLS-1$
      // reset end frame
      setEndFrameNumber(endFrame);
    }
  }

  /**
   * Gets the step size.
   *
   * @return the step size
   */
  public int getStepSize() {
    return stepSize;
  }

  /**
   * Sets the step count.
   *
   * @param count the desired number of steps
   */
  public void setStepCount(int count) {
    if (count == 0) return;
    count = Math.abs(count);
    if (video != null) {
      if (video.getFrameCount() > 1) {
        int end = video.getFrameCount() - 1;
        int maxCount = 1 + (int) ( (end - startFrame) / (1.0 * stepSize));
        count = Math.min(count, maxCount);
      }
      int end = startFrame + (count - 1) * stepSize;
      if (end != video.getEndFrameNumber())
        video.setEndFrameNumber(end);
      // display the correct video image
    	if (getClipInspector() != null) {
    		ClipControl cc = getClipInspector().clipControl;
    		final int m = stepToFrame(cc.stepNumber);
    		if (video.getFrameNumber() != m) {
          Runnable runner = new Runnable() {
            public void run() {
            	video.setFrameNumber(m);
            }
          };
          SwingUtilities.invokeLater(runner);
    		}
    	}
    }
    Integer prev = new Integer(stepCount);
    stepCount = Math.max(count, 1);
    updateArray();
    support.firePropertyChange("stepcount", prev, new Integer(count)); //$NON-NLS-1$
  }

  /**
   * Gets the step count.
   *
   * @return the number of steps
   */
  public int getStepCount() {
    return stepCount;
  }

  /**
   * Sets the start time.
   *
   * @param t0 the start time in milliseconds
   */
  public void setStartTime(double t0) {
  	if (startTime == t0) return;
  	startTime = Double.isNaN(t0)? 0.0: t0;
    support.firePropertyChange("starttime", null, new Double(startTime)); //$NON-NLS-1$
  }
  	/**
   * Gets the start time.
   *
   * @return the start time in milliseconds
   */
  public double getStartTime() {
    return startTime;
  }

  /**
   * Gets the end frame number.
   *
   * @return the end frame
   */
  public int getEndFrameNumber() {
    return startFrame + stepSize * (stepCount-1);
  }

  /**
   * Sets the end frame number.
   *
   * @param end the desired end frame
   */
  public void setEndFrameNumber(int end) {
  	end = Math.max(end, startFrame);
  	// determine step count needed for desired end frame
  	int rem = (end-startFrame)%stepSize;
  	int count = (end-startFrame)/stepSize;
  	if (rem * 1.0 / stepSize > 0.5) count++;
    // set step count
    setStepCount(count+1);
  }

  /**
   * Converts step number to frame number.
   *
   * @param stepNumber the step number
   * @return the frame number
   */
  public int stepToFrame(int stepNumber) {
    return startFrame + stepNumber * stepSize;
  }

  /**
   * Converts frame number to step number. A frame number that falls
   * between two steps maps to the previous step.
   *
   * @param n the frame number
   * @return the step number
   */
  public int frameToStep(int n) {
    return (int)((n - startFrame) / (1.0 * stepSize));
  }

  /**
   * Determines whether the specified frame is a step frame.
   *
   * @param n the frame number
   * @return <code>true</code> if the frame is a step frame
   */
  public boolean includesFrame(int n) {
    for (int i = 0; i < stepCount; i++)
      if (stepFrames[i] == n) return true;
    return false;
  }

  /**
   * Gets the clip inspector.
   *
   * @return the clip inspector
   */
  public ClipInspector getClipInspector() {
    return inspector;
  }

  /**
   * Gets the clip inspector.
   *
   * @param frame the owner of the inspector
   * @return the clip inspector
   */
  public ClipInspector getClipInspector(Frame frame) {
    if (inspector == null)
      inspector = new ClipInspector(this, frame);
    return inspector;
  }

  /**
   * Gets the clip inspector with access to the specified ClipControl.
   *
   * @param control the clip control
   * @param frame the owner of the inspector
   * @return the clip inspector
   */
  public ClipInspector getClipInspector(ClipControl control, Frame frame) {
    if (inspector == null) {
      inspector = new ClipInspector(this, control, frame);
    }
    return inspector;
  }

  /**
   * Hides the clip inspector.
   */
  public void hideClipInspector() {
    if (inspector != null) inspector.setVisible(false);
  }

  /**
   * Adds a PropertyChangeListener to this video clip.
   *
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  /**
   * Adds a PropertyChangeListener to this video clip.
   *
   * @param property the name of the property of interest to the listener
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(String property,
                                        PropertyChangeListener listener) {
    support.addPropertyChangeListener(property, listener);
  }

  /**
   * Removes a PropertyChangeListener from this video clip.
   *
   * @param listener the listener requesting removal
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  /**
   * Removes a PropertyChangeListener for a specified property.
   *
   * @param property the name of the property
   * @param listener the listener to remove
   */
  public void removePropertyChangeListener(
    String property, PropertyChangeListener listener) {
    support.removePropertyChangeListener(property, listener);
  }

  /**
   * Updates the list of step frames.
   */
  private void updateArray() {
    stepFrames = new int[stepCount];
    for (int i = 0; i < stepCount; i++)
      stepFrames[i] = stepToFrame(i);
  }

  /**
   * Returns an XML.ObjectLoader to save and load data for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load data for this class.
   */
  static class Loader implements XML.ObjectLoader {

    /**
     * Saves object data in an XMLControl.
     *
     * @param control the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      VideoClip clip = (VideoClip) obj;
      Video video = clip.getVideo();
      if (video != null) {
      	if (video instanceof ImageVideo) {
      		ImageVideo vid = (ImageVideo)video;
          if (vid.isFileBased())
            control.setValue("video", video); //$NON-NLS-1$
      	}
      	else control.setValue("video", video); //$NON-NLS-1$
      }
      control.setValue("startframe", clip.getStartFrameNumber()); //$NON-NLS-1$
      control.setValue("stepsize", clip.getStepSize()); //$NON-NLS-1$
      control.setValue("stepcount", clip.getStepCount()); //$NON-NLS-1$
      control.setValue("starttime", clip.getStartTime()); //$NON-NLS-1$
    }

    /**
     * Creates a new object.
     *
     * @param control the XMLControl with the object data
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      // load the video and return a new clip
      boolean hasVideo = control.getPropertyNames().contains("video"); //$NON-NLS-1$
      if (!hasVideo) return new VideoClip(null);
      ResourceLoader.addSearchPath(control.getString("basepath")); //$NON-NLS-1$
      XMLControl child = control.getChildControl("video"); //$NON-NLS-1$
      String path = child.getString("path"); //$NON-NLS-1$
      Video video = null;
      boolean loaded = true;
      try {
        video = (Video) control.getObject("video"); //$NON-NLS-1$
      }
      catch (Exception ex) {loaded = false;}
      catch (Error er) {loaded = false;}
      // inform if video failed to load
      if (!loaded) {
        OSPLog.info("\"" + path + "\" could not be opened"); //$NON-NLS-1$ //$NON-NLS-2$
        JOptionPane.showMessageDialog(
            null,
            MediaRes.getString("VideoClip.Dialog.BadVideo.Message") + path, //$NON-NLS-1$
            MediaRes.getString("VideoClip.Dialog.BadVideo.Title"), //$NON-NLS-1$
            JOptionPane.WARNING_MESSAGE);
      }
      else if (video == null) {
        // look for video in control's basepath
        String fullPath = XML.getResolvedPath(path, control.getString("basepath")); //$NON-NLS-1$
        child.setValue("path", fullPath); //$NON-NLS-1$
        try {
            video = (Video)control.getObject("video"); //$NON-NLS-1$
            loaded = true;
            // reset original path
            child.setValue("path", path); //$NON-NLS-1$
        }
        catch (Exception ex) {/** empty block */}
        catch (Error er) {/** empty block */}
        // query user if video still not found
        if (video == null) {
          int i = JOptionPane.showConfirmDialog(null,
              "\"" + path + "\" " //$NON-NLS-1$ //$NON-NLS-2$
              + MediaRes.getString("VideoClip.Dialog.VideoNotFound.Message"), //$NON-NLS-1$
              MediaRes.getString("VideoClip.Dialog.VideoNotFound.Title"), //$NON-NLS-1$
              JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);
          if (i == JOptionPane.YES_OPTION) {
            VideoIO.getChooser().setFileFilter(VideoIO.videoFileFilter);
            VideoIO.getChooser().setSelectedFile(new File(path));
            java.io.File[] files = VideoIO.getChooserFiles("open"); //$NON-NLS-1$
            if (files != null) {
              video = VideoIO.getVideo(files[0]);
            }
          }
        }
      }
      return new VideoClip(video);
    }

    /**
     * Loads a VideoClip with data from an XMLControl.
     *
     * @param control the XMLControl
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      VideoClip clip = (VideoClip) obj;
      // set start frame
      int n = control.getInt("startframe"); //$NON-NLS-1$
      if (n != Integer.MIN_VALUE) {
        clip.setStartFrameNumber(n);
      }
      // set step size
      n = control.getInt("stepsize"); //$NON-NLS-1$
      if (n != Integer.MIN_VALUE) {
        clip.setStepSize(n);
      }
      // set step count
      n = control.getInt("stepcount"); //$NON-NLS-1$
      if (n != Integer.MIN_VALUE) {
        clip.setStepCount(n);
      }
      // set start time
      double t = control.getDouble("starttime"); //$NON-NLS-1$
      if (!Double.isNaN(t)) {
        clip.setStartTime(t);
      }
      return obj;
    }
  }

}
