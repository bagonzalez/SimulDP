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
 * This is a FileFilter that accepts video files.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class VideoFileFilter extends FileFilter {

  // Accept directories, video and image files.
  public boolean accept(File f) {
    if (f.isDirectory()) return true;
    String extension = VideoIO.getExtension(f);
    if (extension != null &&
       (extension.equals("mov") || //$NON-NLS-1$
        extension.equals("avi") || //$NON-NLS-1$
        extension.equals("dv") || //$NON-NLS-1$
        extension.equals("mpg") || //$NON-NLS-1$
        extension.equals("mp4") || //$NON-NLS-1$
        extension.equals("gif") || //$NON-NLS-1$
        extension.equals("png") || //$NON-NLS-1$
        extension.equals("jpg"))) return true; //$NON-NLS-1$
    return false;
  }

// The description of this filter
  public String getDescription() {
    return MediaRes.getString("VideoFileFilter.Description"); //$NON-NLS-1$
  }
}
