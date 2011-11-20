package org.opensourcephysics.tools;

import java.awt.image.BufferedImage;


public interface VideoTool {

   /**
    * Adds a frame to the video if it is recording.
    *
    * @param image the frame to be added
    * @return true if frame was added
    */
   public boolean addFrame(BufferedImage image);

   /**
    * Gets the recording flag.
    *
    * @return true if recording rendered images
    */
   public boolean isRecording();

   /**
    * Sets the visibility.
    *
    * @param visible true to set this visible
    */
   public void setVisible(boolean visible);

   /**
    * Clear the video from the tool in preparation for a new video.
    */
   public void clear();


}
