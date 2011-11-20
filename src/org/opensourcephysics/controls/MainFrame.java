package org.opensourcephysics.controls;

import org.opensourcephysics.display.OSPFrame;
import javax.swing.JFrame;
import java.util.Collection;

/**
 * A MainFrame contains the primary user interface for a program.
 *
 * The main frame closes all child windows when closed and will usually exit when it is closed.
 * An OSP program should have only one main frame.
 *
 * @author W. Christian
 * @version 1.0
 */
public interface MainFrame {

  /**
   * Gets the main OSPFrame.  The main frame will usually exit program when it is closed.
   * @return OSPFrame
   */
  public OSPFrame getMainFrame();
  
  /**
   * Gets the OSP Application that is controlled by this frame.
   * @return
   */
  public OSPApplication getOSPApp();

  /**
   * Adds a child frame that depends on the main frame.
   * Child frames are closed when this frame is closed.
   *
   * @param frame JFrame
   */
  public void addChildFrame(JFrame frame);

  /**
   * Clears the child frames from the main frame.
   */
   public void clearChildFrames();

   /**
    * Gets a copy of the ChildFrames collection.
    * @return Collection
    */
   public Collection<JFrame> getChildFrames();


}
