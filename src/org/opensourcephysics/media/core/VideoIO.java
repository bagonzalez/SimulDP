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
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.gif.*;

/**
 * This provides static methods for managing video and text input/output.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class VideoIO {

  // static fields
  protected static JFileChooser chooser;
  protected static FileFilter videoFileFilter = new VideoFileFilter();
  protected static FileFilter qtFileFilter;
  protected static FileFilter imageFileFilter;
  protected static Collection<VideoType> videoTypes = new ArrayList<VideoType>();
  protected static String defaultXMLExt = "xml"; //$NON-NLS-1$

  static {
    qtFileFilter = new FileFilter() {
      public boolean accept(File f) {
        if (f == null) return false;
        if (f.isDirectory()) return true;
        if (f.getAbsolutePath().indexOf("QTJava.zip") != -1) return true; //$NON-NLS-1$
        return false;
      }
      public String getDescription() {return "QTJava.zip";} //$NON-NLS-1$
    };
    imageFileFilter = new FileFilter() {
      public boolean accept(File f) {
        if (f == null) return false;
        if (f.isDirectory()) return true;
        String extension = VideoIO.getExtension(f);
        if (extension != null &&
           (extension.equals("gif") || //$NON-NLS-1$
            extension.equals("jpg"))) return true; //$NON-NLS-1$
        return false;
      }
      public String getDescription() {return MediaRes.getString("VideoIO.ImageFileFilter.Description");} //$NON-NLS-1$
    };
  }

  /**
   * protected constructor to discourage instantiation
   */
  protected VideoIO() {/** empty block */}

  /**
   * Gets the extension of a file.
   *
   * @param file the file
   * @return the extension of the file
   */
  public static String getExtension(File file) {
    String ext = null;
    String s = file.getName();
    int i = s.lastIndexOf('.');
    if (i > 0 && i < s.length() - 1) {
      ext = s.substring(i + 1).toLowerCase();
    }
    return ext;
  }

  /**
   * Gets the video file chooser.
   *
   * @return the file chooser
   */
  public static JFileChooser getChooser() {
    if (chooser == null) {
      chooser = new JFileChooser(new File(OSPRuntime.chooserDir));
    }
    return chooser;
  }

  /**
   * Sets the default xml extension used when saving data.
   *
   * @param ext the default extension
   */
  public static void setDefaultXMLExtension(String ext) {
    defaultXMLExt = ext;
  }

  /**
   * Gets the path relative to the user directory.
   *
   * @param absolutePath the absolute path
   * @return the relative path
   */
  public static String getRelativePath(String absolutePath) {
    if (absolutePath.indexOf("/") == -1 && absolutePath.indexOf("\\") == -1) //$NON-NLS-1$ //$NON-NLS-2$
      return absolutePath;
    if (absolutePath.startsWith("http:")) return absolutePath; //$NON-NLS-1$
    String path = absolutePath;
    String relativePath = ""; //$NON-NLS-1$
    boolean validPath = false;
    // relative to user directory
    String base = System.getProperty("user.dir"); //$NON-NLS-1$
    if (base == null) return path;
    for (int j = 0; j < 3; j++) {
      if (j > 0) {
        // move up one level
        int k = base.lastIndexOf("\\"); //$NON-NLS-1$
        if (k == -1) k = base.lastIndexOf("/"); //$NON-NLS-1$
        if (k != -1) {
          base = base.substring(0, k);
          relativePath += "../"; //$NON-NLS-1$
        }
        else break; // no more levels!
      }
      if (path.startsWith(base)) {
        path = path.substring(base.length() + 1);
        // replace backslashes with forward slashes
        int i = path.indexOf("\\"); //$NON-NLS-1$
        while (i != -1) {
          path = path.substring(0, i) + "/" + path.substring(i + 1); //$NON-NLS-1$
          i = path.indexOf("\\"); //$NON-NLS-1$
        }
        relativePath += path;
        validPath = true;
        break;
      }
    }
    if (validPath) return relativePath;
    return path;
  }

  /**
   * Adds a video type to the list of available types
   *
   * @param type the video type
   */
  public static void addVideoType(VideoType type) {
    if (type != null) {
      boolean hasType = false;
      Iterator<VideoType> it = videoTypes.iterator();
      while (it.hasNext()) {
        if (it.next().getClass().equals(type.getClass()))
          hasType = true;
      }
      if (!hasType) {
        videoTypes.add(type);
      }
    }
  }

  /**
   * Gets an array of available video types
   *
   * @return the video types
   */
  public static VideoType[] getVideoTypes() {
    return videoTypes.toArray(new VideoType[0]);
  }

  /**
   * Returns a video from a specified file. May return null.
   *
   * @param file the file
   * @return the video
   */
  public static Video getVideo(File file) {
    Video video = null;
    Iterator<VideoType> it = videoTypes.iterator();
    while (it.hasNext()) {
      VideoType vidType = it.next();
      video = vidType.getVideo(file.getAbsolutePath());
      if (video != null)
        break;
    }
    return video;
  }

  /**
   * Returns a clone of the specified video.
   *
   * @param video the video to clone
   * @return the clone
   */
  public static Video clone(Video video) {
    if (video == null) return null;
    // ImageVideo is special case since may have pasted images
    if (video instanceof ImageVideo) {
    	ImageVideo oldVid = (ImageVideo)video;
    	ImageVideo newVid = new ImageVideo(oldVid.getImages());
    	newVid.rawImage = newVid.images[0];
      Collection<Filter> filters = video.getFilterStack().getFilters();
      if (filters != null) {
        Iterator<Filter> it = filters.iterator();
        while (it.hasNext()) {
          Filter filter = it.next();
          newVid.getFilterStack().addFilter(filter);
        }
      }
      return newVid;
    }
    XMLControl control = new XMLControlElement(video);
    return (Video)new XMLControlElement(control).loadObject(null);
  }

  /**
   * Loads the specified video panel from a file selected with a chooser
   * and sets the data file of the panel.
   *
   * @param vidPanel the video panel
   * @return an array containing the loaded object and file
   */
  public static File open(VideoPanel vidPanel) {
    return open((File)null, vidPanel);
  }

  /**
   * Displays a file chooser and returns the chosen files.
   *
   * @param type may be "open", "open video", "save", "qt", "insert image"
   * @return the files, or null if no files chosen
   */
  public static File[] getChooserFiles(String type) {
    JFileChooser chooser = getChooser();
    chooser.setMultiSelectionEnabled(false);
    int result = JFileChooser.CANCEL_OPTION;
    if (type.toLowerCase().equals("open")) { //$NON-NLS-1$
      chooser.removeChoosableFileFilter(qtFileFilter);
      chooser.removeChoosableFileFilter(imageFileFilter);
      chooser.addChoosableFileFilter(videoFileFilter);
      chooser.setFileFilter(chooser.getAcceptAllFileFilter());
      result = chooser.showOpenDialog(null);
    }
    else if (type.toLowerCase().equals("open video")) { //$NON-NLS-1$
      chooser.removeChoosableFileFilter(qtFileFilter);
      chooser.removeChoosableFileFilter(imageFileFilter);
      chooser.addChoosableFileFilter(videoFileFilter);
      chooser.setFileFilter(videoFileFilter);
      result = chooser.showOpenDialog(null);
    }
    else if (type.toLowerCase().equals("save")) { //$NON-NLS-1$
      chooser.removeChoosableFileFilter(videoFileFilter);
      chooser.removeChoosableFileFilter(qtFileFilter);
      chooser.removeChoosableFileFilter(imageFileFilter);
      String filename = MediaRes.getString("VideoIO.FileName.Untitled"); //$NON-NLS-1$
      chooser.setSelectedFile(new File(filename + "." + defaultXMLExt)); //$NON-NLS-1$
      result = chooser.showSaveDialog(null);
    }
    else if (type.toLowerCase().equals("qt")) { //$NON-NLS-1$
      chooser.removeChoosableFileFilter(videoFileFilter);
      chooser.removeChoosableFileFilter(imageFileFilter);
      chooser.addChoosableFileFilter(qtFileFilter);
      chooser.setFileFilter(qtFileFilter);
      chooser.setDialogTitle(MediaRes.getString("VideoIO.Dialog.FindQT.Title")); //$NON-NLS-1$
      result = chooser.showDialog(null, MediaRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
    }
    else if (type.toLowerCase().equals("insert image")) { //$NON-NLS-1$
      chooser.removeChoosableFileFilter(qtFileFilter);
      chooser.removeChoosableFileFilter(videoFileFilter);
      chooser.addChoosableFileFilter(imageFileFilter);
      chooser.setFileFilter(imageFileFilter);
      chooser.setMultiSelectionEnabled(true);
      chooser.setSelectedFile(new File("")); //$NON-NLS-1$
      result = chooser.showOpenDialog(null);
      if (result == JFileChooser.APPROVE_OPTION) {
        return chooser.getSelectedFiles();
      }
    }
    if (result == JFileChooser.APPROVE_OPTION) {
      return new File[] {chooser.getSelectedFile()};
    }
    return null;
  }


  /**
   * Loads data or a video from a specified file into a VideoPanel.
   * If file is null, a file chooser is displayed.
   *
   * @param file the file to be loaded
   * @param vidPanel the video panel
   * @return the file opened
   */
  public static File open(File file, VideoPanel vidPanel) {
    JFileChooser chooser = getChooser();
    if (file == null) {
      chooser.setDialogTitle(MediaRes.getString("VideoIO.Dialog.Open.Title")); //$NON-NLS-1$
      File[] files = getChooserFiles("open"); //$NON-NLS-1$
      if (files != null) file = files[0];
    }
    if (file == null) return null;
    if (videoFileFilter.accept(file)) { // load video
      VideoType[] types = getVideoTypes();
      Video video = null;
      for (int i = 0; i < types.length; i++) {
        video = types[i].getVideo(file.getAbsolutePath());
        if (video != null) break;
      }
      if (video != null) {
        vidPanel.setVideo(video);
        vidPanel.repaint();
      }
      else {
        JOptionPane.showMessageDialog(null,
        				MediaRes.getString("VideoIO.Dialog.BadVideo.Message") + //$NON-NLS-1$
        				file.getAbsolutePath());
      }
    }
    else { // load data
      XMLControlElement control = new XMLControlElement();
      control.read(file.getAbsolutePath());
      Class<?> type = control.getObjectClass();
      if (VideoPanel.class.isAssignableFrom(type)) {
        vidPanel.setDataFile(file);
        control.loadObject(vidPanel);
      }
      else if (!control.failedToRead()) {
        JOptionPane.showMessageDialog(
            null,
            "\"" + file.getName() + "\" " + //$NON-NLS-1$ //$NON-NLS-2$
            MediaRes.getString("VideoIO.Dialog.XMLMismatch.Message"), //$NON-NLS-1$
            MediaRes.getString("VideoIO.Dialog.XMLMismatch.Title"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
        return null;
      }
      else {
        JOptionPane.showMessageDialog(null,
        				MediaRes.getString("VideoIO.Dialog.BadFile.Message") + //$NON-NLS-1$
        				file.getAbsolutePath());
      }
      vidPanel.changed = false;
    }
    return file;
  }

  /**
   * Writes VideoPanel data to the specified file. If the file is null
   * it brings up a chooser.
   *
   * @param file the file to write to
   * @param vidPanel the video panel
   * @return the file written to, or null if not written
   */
  public static File save(File file, VideoPanel vidPanel) {
    if (file == null) {
      Video video = vidPanel.getVideo();
      JFileChooser chooser = getChooser();
      chooser.removeChoosableFileFilter(videoFileFilter);
      chooser.removeChoosableFileFilter(imageFileFilter);
      chooser.removeChoosableFileFilter(qtFileFilter);
      chooser.setDialogTitle(MediaRes.getString("VideoIO.Dialog.SaveAs.Title")); //$NON-NLS-1$
      String filename = MediaRes.getString("VideoIO.FileName.Untitled"); //$NON-NLS-1$
      if (vidPanel.getFilePath() != null) {
      	filename = XML.stripExtension(vidPanel.getFilePath());
      }
      else if (video != null && video.getProperty("name") != null) { //$NON-NLS-1$
        filename = (String)video.getProperty("name"); //$NON-NLS-1$
        int i = filename.lastIndexOf("."); //$NON-NLS-1$
        if (i > 0) {
          filename = filename.substring(0, i);
        }
      }
      file = new File(filename + "." + defaultXMLExt); //$NON-NLS-1$
      String parent = XML.getDirectoryPath(filename);
      if (!parent.equals("")) { //$NON-NLS-1$
      	XML.createFolders(parent);
      	chooser.setCurrentDirectory(new File(parent));
      }
      chooser.setSelectedFile(file);
      int result = chooser.showSaveDialog(null);
      if (result == JFileChooser.APPROVE_OPTION) {
        file = chooser.getSelectedFile();
        if (!defaultXMLExt.equals(getExtension(file))) {
        	filename = XML.stripExtension(file.getPath());
          file = new File(filename + "." + defaultXMLExt); //$NON-NLS-1$
        }
        if (file.exists()) {
          int selected = JOptionPane.showConfirmDialog(null,
              " \"" + file.getName() + "\" " //$NON-NLS-1$ //$NON-NLS-2$
              + MediaRes.getString("VideoIO.Dialog.FileExists.Message"), //$NON-NLS-1$
              MediaRes.getString("VideoIO.Dialog.FileExists.Title"),  //$NON-NLS-1$
              JOptionPane.YES_NO_CANCEL_OPTION);
          if (selected != JOptionPane.YES_OPTION) {
            return null;
          }
        }
        vidPanel.setDataFile(file);
        if (video instanceof ImageVideo) {
      		ImageVideo vid = (ImageVideo)video;
      		vid.saveInvalidImages();
        }
      }
      else return null;
    }
    XMLControl xmlControl = new XMLControlElement(vidPanel);
    xmlControl.write(file.getAbsolutePath());
    vidPanel.changed = false;
    return file;
  }

  static {
    // add image video type
    addVideoType(new GifVideoType());
    addVideoType(new ImageVideoType());
  }
}

