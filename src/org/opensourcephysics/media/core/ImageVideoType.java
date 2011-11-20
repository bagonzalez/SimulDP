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

import java.io.*;

import javax.swing.filechooser.FileFilter;

/**
 * This implements the VideoType interface with a buffered image type.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class ImageVideoType implements VideoType {
	
  protected FileFilter jpgFilter;
  protected FileFilter pngFilter;

  /**
   * Opens a video file as an ImageVideo.
   *
   * @param file the video file
   * @return a new image video
   */
  public Video getVideo(File file) {
    try {
      return new ImageVideo(file.getAbsolutePath());
    }
    catch (IOException ex) {
      return null;
    }
  }

  /**
   * Opens a named image as an ImageVideo.
   *
   * @param name the name of the image
   * @return a new image video
   */
  public Video getVideo(String name) {
    try {
      return new ImageVideo(name, true);
    }
    catch (IOException ex) {
      return null;
    }
  }

  /**
   * Gets a video recorder.
   *
   * @return the video recorder
   */
  public VideoRecorder getRecorder() {
    return new ImageVideoRecorder();
  }

  /**
   * Reports whether this type can record videos
   *
   * @return true if this can record videos
   */
  public boolean canRecord() {
    return true;
  }

  /**
   * Gets the name and/or description of this type.
   *
   * @return a description
   */
  public String getDescription() {
    return MediaRes.getString("ImageVideoType.Description"); //$NON-NLS-1$
  }

  /**
   * Gets the default extension for this type.
   *
   * @return a description
   */
  public String getDefaultExtension() {
    return "jpg"; //$NON-NLS-1$
  }

  /**
   * Gets the file filter for this type.
   *
   * @return a file filter
   */
  public javax.swing.filechooser.FileFilter[] getFileFilters() {
  	if (jpgFilter == null) {
  		jpgFilter = new JPGFileFilter();
  		pngFilter = new PNGFileFilter();
  	}
    return new javax.swing.filechooser.FileFilter[] {jpgFilter, pngFilter};
  }

  /**
   * Return true if the specified video is this type.
   *
   * @param video the video
   * @return true if the video is this type
   */
  public boolean isType(Video video) {
    return video.getClass().equals(ImageVideo.class);
  }

  class JPGFileFilter extends FileFilter {

    // Accept directories and jpg files.
    public boolean accept(File f) {
      if (f.isDirectory()) return true;
      String extension = VideoIO.getExtension(f);
      if (extension != null && extension.equals("jpg")) return true; //$NON-NLS-1$
      return false;
    }

    public String getDescription() {
      return MediaRes.getString("ImageVideoType.JPGFileFilter.Description"); //$NON-NLS-1$
    }
  }

  class PNGFileFilter extends FileFilter {

    // Accept directories and png files.
    public boolean accept(File f) {
      if (f.isDirectory()) return true;
      String extension = VideoIO.getExtension(f);
      if (extension != null && extension.equals("png")) return true; //$NON-NLS-1$
      return false;
    }

    public String getDescription() {
      return MediaRes.getString("ImageVideoType.PNGFileFilter.Description"); //$NON-NLS-1$
    }
  }
}


