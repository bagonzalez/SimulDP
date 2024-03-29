/*
 * The org.opensourcephysics.media package defines the Open Source Physics
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
package org.opensourcephysics.media.gif;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.controls.*;

/**
 * This is a video that wraps an animated gif image.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class GifVideo extends VideoAdapter {

  // instance fields
  protected GifDecoder decoder;
  protected int[] startTimes; // in milliseconds
  private javax.swing.Timer timer;
  private HashSet<DrawingPanel> panels = new HashSet<DrawingPanel>();

  /**
   * Creates a GifVideo and loads a gif image specified by name
   *
   * @param gifName the name of the image file
   * @throws IOException
   */
  public GifVideo(String gifName) throws IOException {
    load(gifName);
    createTimer();
  }

  /**
   * Draws the video image on the panel.
   *
   * @param panel the drawing panel requesting the drawing
   * @param g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics g) {
    panels.add(panel);
    super.draw(panel, g);
  }

  /**
   * Called by the garbage collector when this video is no longer in use.
   */
  protected void finalize() {
//    System.out.println("gif garbage");
  }

  /**
   * Plays the video at the current rate.
   */
  public void play() {
    if (getFrameCount() == 1) return;
    if (!timer.isRunning()) {
      if (getFrameNumber() >= getEndFrameNumber())
        setFrameNumber(getStartFrameNumber());
      timer.restart();
      support.firePropertyChange("playing", null, new Boolean(true)); //$NON-NLS-1$
    }
  }

  /**
   * Stops the video.
   */
  public void stop() {
    if (timer.isRunning()) {
      timer.stop();
      support.firePropertyChange("playing", null, new Boolean(false)); //$NON-NLS-1$
    }
  }

  /**
   * Overrides ImageVideo setFrameNumber method.
   *
   * @param n the desired frame number
   */
  public void setFrameNumber(int n) {
    super.setFrameNumber(n);
    n = getFrameNumber();
    int index = Math.min(n, decoder.getFrameCount()-1);
    rawImage = decoder.getFrame(index);
    isValidImage = false;
    isValidFilteredImage = false;
    support.firePropertyChange("framenumber", null,  new Integer(n)); //$NON-NLS-1$
    // repaint panels in case they don't listen
    Iterator<DrawingPanel> it = panels.iterator();
    while (it.hasNext()) {
      DrawingPanel panel = it.next();
      panel.repaint();
    }
  }

  /**
   * Gets the start time of the specified frame in milliseconds.
   *
   * @param n the frame number
   * @return the start time of the frame in milliseconds, or -1 if not known
   */
  public double getFrameTime(int n) {
    if (n >= startTimes.length || n < 0) return -1;
    return startTimes[n];
  }

  /**
   * Gets the current video time in milliseconds.
   *
   * @return the current time in milliseconds, or -1 if not known
   */
  public double getTime() {
    return getFrameTime(getFrameNumber());
  }

  /**
   * Sets the video time in milliseconds.
   *
   * @param millis the desired time in milliseconds
   */
  public void setTime(double millis) {
    millis = Math.abs(millis);
    for (int i = 0; i < startTimes.length; i++) {
      int t = startTimes[i];
      if (millis < t) { // find first frame with later start time
        setFrameNumber(i - 1);
        break;
      }
    }
  }

  /**
   * Gets the start time in milliseconds.
   *
   * @return the start time in milliseconds, or -1 if not known
   */
  public double getStartTime() {
    return getFrameTime(getStartFrameNumber());
  }

  /**
   * Sets the start time in milliseconds. NOTE: the actual start time
   * is normally set to the beginning of a frame.
   *
   * @param millis the desired start time in milliseconds
   */
  public void setStartTime(double millis) {
    millis = Math.abs(millis);
    for (int i = 0; i < startTimes.length; i++) {
      int t = startTimes[i];
      if (millis < t) { // find first frame with later start time
        setStartFrameNumber(i - 1);
        break;
      }
    }
  }

  /**
   * Gets the end time in milliseconds.
   *
   * @return the end time in milliseconds, or -1 if not known
   */
  public double getEndTime() {
    int n = getEndFrameNumber();
    return getFrameTime(n) + decoder.getDelay(n);
  }

  /**
   * Sets the end time in milliseconds. NOTE: the actual end time
   * is set to the end of a frame.
   *
   * @param millis the desired end time in milliseconds
   */
  public void setEndTime(double millis) {
    millis = Math.abs(millis);
    millis = Math.min(getDuration(), millis);
    for (int i = 0; i < startTimes.length; i++) {
      int t = startTimes[i];
      if (millis < t) { // find first frame with later start time
        setEndFrameNumber(i - 1);
        break;
      }
    }
  }

  /**
   * Gets the duration of the video.
   *
   * @return the duration of the video in milliseconds, or -1 if not known
   */
  public double getDuration() {
    int n = getFrameCount() - 1;
    return getFrameTime(n) + decoder.getDelay(n);
  }

  /**
   * Loads a gif image specified by name.
   *
   * @param gifName the gif image name
   * @throws IOException
   */
  protected void load(String gifName) throws IOException {
    decoder = new GifDecoder();
    int status = decoder.read(gifName);
    if (status == GifDecoder.STATUS_OPEN_ERROR)
      throw new IOException("Gif " + gifName + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
    else if (status == GifDecoder.STATUS_FORMAT_ERROR)
      throw new IOException("File format error"); //$NON-NLS-1$
    setProperty("name", gifName); //$NON-NLS-1$
    // set path to be saved in XMLControl
    // if name is relative, path is name
    if (gifName.indexOf(":") == -1) //$NON-NLS-1$
      setProperty("path", XML.forwardSlash(gifName)); //$NON-NLS-1$
    // else path is relative to user directory
    else {
      setProperty("path", XML.getRelativePath(gifName)); //$NON-NLS-1$
      setProperty("absolutePath", gifName); //$NON-NLS-1$
    }
    frameCount = decoder.getFrameCount();
    startFrameNumber = 0;
    endFrameNumber = frameCount - 1;
    // create startTimes array
    startTimes = new int[frameCount];
    startTimes[0] = 0;
    for (int i = 1; i < startTimes.length; i++) {
      startTimes[i] = startTimes[i-1] + decoder.getDelay(i-1);
    }
    setImage(decoder.getFrame(0));
  }

  /**
   * Sets the image.
   *
   * @param image the image
   */
  private void setImage(BufferedImage image) {
    rawImage = image;
    size = new Dimension(image.getWidth(), image.getHeight());
    refreshBufferedImage();
    // create coordinate system and relativeAspects
    coords = new ImageCoordSystem(frameCount);
    coords.addPropertyChangeListener(this);
    aspects = new DoubleArray(frameCount, 1);
  }

  /**
   * Creates the timer.
   */
  private void createTimer() {
    int delay = decoder.getDelay(0);
    timer = new javax.swing.Timer(delay, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (getFrameNumber() < getEndFrameNumber()) {
          int delay = decoder.getDelay(getFrameNumber() + 1);
          timer.setDelay((int)(delay/getRate()));
          setFrameNumber(getFrameNumber() + 1);
        }
        else if (looping) {
          int delay = decoder.getDelay(getStartFrameNumber());
          timer.setDelay((int)(delay/getRate()));
          setFrameNumber(getStartFrameNumber());
        }
        else stop();
      }
    });
  }

  /**
   * Returns an XML.ObjectLoader to save and load GifVideo data.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load ImageVideo data.
   */
  static class Loader
      implements XML.ObjectLoader {

    /**
     * Saves GifVideo data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the GifVideo object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      GifVideo video = (GifVideo) obj;
      control.setValue("path", video.getProperty("path")); //$NON-NLS-1$ //$NON-NLS-2$
      if (!video.getFilterStack().isEmpty()) {
        control.setValue("filters", video.getFilterStack().getFilters()); //$NON-NLS-1$
      }
    }

    /**
     * Creates a new GifVideo.
     *
     * @param control the control
     * @return the new GifVideo
     */
    public Object createObject(XMLControl control) {
      try {
        GifVideo video = new GifVideo(control.getString("path")); //$NON-NLS-1$
        return video;
      }
      catch (IOException ex) {
        return null;
      }
    }

    /**
     * This does nothing, but is required by the XML.ObjectLoader interface
     *
     * @param control the control
     * @param obj the GifVideo object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      GifVideo video = (GifVideo) obj;
      Collection<?> filters = (Collection<?>)control.getObject("filters"); //$NON-NLS-1$
      if (filters != null) {
        video.getFilterStack().clear();
        Iterator<?> it = filters.iterator();
        while (it.hasNext()) {
          Filter filter = (Filter)it.next();
          video.getFilterStack().addFilter(filter);
        }
      }
      return obj;
    }
  }
}
