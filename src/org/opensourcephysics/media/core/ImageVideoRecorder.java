/*
 * The org.opensourcephysics.media.gif package provides GIF services
 * including implementations of the Video and VideoRecorder interfaces.
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

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import org.opensourcephysics.controls.XML;

/**
 * This is an image video recorder that uses scratch files.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class ImageVideoRecorder extends ScratchVideoRecorder {

  // instance fields
  protected ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

  /**
   * Constructs a ImageVideoRecorder object.
   */
  public ImageVideoRecorder() {
    super(new ImageVideoType());
  }

  /**
   * Gets the video.
   *
   * @return the video
   * @throws IOException
   */
  public Video getVideo() throws IOException {
  	if (isSaved && saveFile != null) {
  		return videoType.getVideo(saveFile.getAbsolutePath());
  	}
  	BufferedImage[] imageArray = images.toArray(new BufferedImage[0]); 
    return new ImageVideo(imageArray);
  }

  /**
   * Saves all video images to a numbered sequence of files.
   *
   * @param fileName the file name basis for images
   * @return the full path of the first image in the sequence
   * @throws IOException
   */
  public String saveVideo(String fileName) throws IOException {
    if (fileName == null) return saveVideoAs();
    setFileName(fileName);
    if (saveFile == null) throw new IOException("Read-only file"); //$NON-NLS-1$
    isSaved = true;
    // save images
    BufferedImage[] imageArray = images.toArray(new BufferedImage[0]); 
    String[] paths = saveImages(fileName, imageArray);
    return paths.length == 0? null: paths[0];
  }
  
//________________________________ protected methods _________________________________

  /**
   * Required by ScratchVideoRecorder, but unused.
   */
  protected void saveScratch() {
  }

  /**
   * Starts the video recording process.
   *
   * @return true if video recording successfully started
   */
  protected boolean startRecording() {
  	images.clear();
    if (dim == null) {
      if (frameImage != null) {
        dim = new Dimension(frameImage.getWidth(null),
                            frameImage.getHeight(null));
      }
      else return false;
    }
    return true;
  }

  /**
   * Appends a frame to the current video. Note: this creates a new 
   * BufferedImage each time a frame is appended and can use lots of
   * memory in a hurry.
   *
   * @param image the image to append
   * @return true if image successfully appended
   */
  protected boolean append(Image image) {
  	int w = image.getWidth(null);
  	int h = image.getHeight(null);
  	BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = bi.createGraphics();
    g.drawImage(image, 0, 0, null);
  	images.add(bi);
    return true;
  }

  /**
   * Saves images to a numbered sequence of jpg files.
   *
   * @param fileName the file name basis for images
   * @param images the images to save
   * @return the paths of the saved images
   * @throws IOException
   */
  protected String[] saveImages(String fileName, BufferedImage[] images) throws IOException {
    // determine number of digits to append to file names
    int k = getAppendedNumber(fileName);
    int n = images.length;
    if (n == 1) {
      javax.imageio.ImageIO.write(images[0], "jpg", new BufferedOutputStream( //$NON-NLS-1$
					new FileOutputStream(fileName)));
      return new String[] {fileName};
    }
    ArrayList<String> paths = new ArrayList<String>();
    int digits = n+k<10? 1: n+k<100? 2: n+k<1000? 3: 4;
    // get base
  	String base = getBase(fileName);
		for (int i = 0; i < images.length; i++) {
			// append numbers and save images
			String num = String.valueOf(i+k);
			if (digits == 2 && i+k < 10) num = "0"+num; //$NON-NLS-1$
			else if (digits == 3 && i+k < 10) num = "00"+num; //$NON-NLS-1$
			else if (digits == 3 && i+k < 100) num = "0"+num; //$NON-NLS-1$
			else if (digits == 4 && i+k < 10) num = "000"+num; //$NON-NLS-1$
			else if (digits == 4 && i+k < 100) num = "00"+num; //$NON-NLS-1$
			else if (digits == 4 && i+k < 1000) num = "0"+num; //$NON-NLS-1$
	  	fileName = base+num+"."+ext; //$NON-NLS-1$
	  	paths.add(fileName);
      javax.imageio.ImageIO.write(images[i], ext, new BufferedOutputStream(
					new FileOutputStream(fileName)));
		}
		// return paths
    return paths.toArray(new String[0]);
  }
  
  /**
   * Return the file that will be saved if the specified file is selected.
   * This is needed by ImageVideoRecorder since it strips and/or appends digits
   * to the selected file name.
   *
   * @param file the file selected with the chooser
   * @return the file (or first file) to be saved
   */
  protected File getFileToBeSaved(File file) {
    // determine number of digits to append to file names
    int n = images.size();
    // if single or no image, return file itself
    if (n <= 1) return file;
  	String fileName = file.getAbsolutePath();
    // get appended number
    int i = getAppendedNumber(fileName);
    // get base
  	String base = getBase(fileName);
  	if (i > 0) {
    	fileName = base 
			+ (n+i<10? String.valueOf(i):
			n+i<100&&i<10? "0"+i:  //$NON-NLS-1$
			n+i<100? String.valueOf(i):
			n+i<1000&&i<10? "00"+i:  //$NON-NLS-1$
			n+i<1000&&i<100? "0"+i:  //$NON-NLS-1$
			n+i<1000? String.valueOf(i):
			i<10? "000"+i:  //$NON-NLS-1$
			i<100? "00"+i:  //$NON-NLS-1$
			i<1000? "0"+i:  //$NON-NLS-1$
			String.valueOf(i));  		
  	}
  	else fileName = base 
  			+ (n<10? "0":  //$NON-NLS-1$
  			n<100? "00":  //$NON-NLS-1$
  			n<1000? "000":  //$NON-NLS-1$
  			"0000"); //$NON-NLS-1$
  	return new File(fileName+"."+ext); //$NON-NLS-1$
  }

  protected String getBase(String path) {
  	String base = XML.stripExtension(path);
    // strip off digits at end, if any
    int len = base.length();
    int digits = 1;
    for (; digits < len; digits++) {
      try {
        Integer.parseInt(base.substring(len-digits));
      }
      catch (NumberFormatException ex) {
         break;
      }
    }
    digits--; // failed at digits, so go back one
    if (digits == 0) { // no number found
       return base;
     }
    return base.substring(0, len-digits);
  }

  protected int getAppendedNumber(String path) {
  	String base = XML.stripExtension(path);
    // look for appended number at end, if any
    int len = base.length();
    int digits = 1;
    int n = 0;
    for (; digits < len; digits++) {
      try {
        n = Integer.parseInt(base.substring(len-digits));
      }
      catch (NumberFormatException ex) {
         break;
      }
    }
    return n;
  }

}
