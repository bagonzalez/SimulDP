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

/**
 * This defines methods common to all video types.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public interface VideoType {

  /**
   * Opens a new video with the specified name.
   *
   * @param name the name of the video
   * @return the new video
   */
  public Video getVideo(String name);

  /**
   * Return true if the specified video is this type.
   *
   * @param video the video
   * @return true if the video is this type
   */
  public boolean isType(Video video);

  /**
   * Gets a video recorder. Returns null if canRecord() is false.
   *
   * @return the video recorder
   */
  public VideoRecorder getRecorder();

  /**
   * Reports whether this type can record videos
   *
   * @return true if this can record videos
   */
  public boolean canRecord();

  /**
   * Gets the name and/or description of this type.
   *
   * @return a description
   */
  public String getDescription();

  /**
   * Gets the name and/or description of this type.
   *
   * @return a description
   */
  public String getDefaultExtension();

  /**
   * Gets the file filter for this type.
   *
   * @return a file filter
   */
  public javax.swing.filechooser.FileFilter[] getFileFilters();

}
