package org.opensourcephysics.display;

/**
 * Tags stand alone programs so that the main frame does not exit the VM whne the frame is close.
 * Used by Launcher and LaunchBuilder.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public interface AppFrame {

  /**
   * Returns true if this frame wishes to exit.
   * Launcher uses this to identify control frames.
   *
   * @return true if this frame wishes to exit
   */
  public boolean wishesToExit();

  /**
   * Returns the operation that occurs when the user
   * initiates a "close" on this frame.
   *
   * @return an integer indicating the window-close operation
   * @see #setDefaultCloseOperation
   */
  public int getDefaultCloseOperation();

  /**
   * Sets the operation that occurs when the user
   * initiates a "close" on this frame.
   *
   * @see #getDefaultCloseOperation
   */
  public void setDefaultCloseOperation(int operation);
}
