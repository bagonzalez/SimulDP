package org.opensourcephysics.media.core;

import java.awt.event.MouseEvent;
import java.awt.geom.*;

import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.axes.*;

/**
 * A coordinate string builder for a video panel.
 */
public class VidCartesianCoordinateStringBuilder
    extends CartesianCoordinateStringBuilder {

  protected VidCartesianCoordinateStringBuilder() {
    super();
  }

  protected VidCartesianCoordinateStringBuilder(String xLabel, String yLabel) {
    super(xLabel, yLabel);
  }

  /**
   * Converts the pixel coordinates in a mouse event into world coordinates and
   * return these coordinates in a string.
   *
   * @param panel the drawing panel
   * @param e the mouse event
   * @return the coordinate string
   */
  public String getCoordinateString(DrawingPanel panel, MouseEvent e) {
    if (!(panel instanceof VideoPanel))
      return super.getCoordinateString(panel, e);
    VideoPanel vidPanel = (VideoPanel)panel;
    Point2D pt = vidPanel.getWorldMousePoint();
    return getCoordinateString(pt.getX(), pt.getY());
  }

  /**
   * Returns the specified xy coordinates in a string.
   *
   * @param x the x
   * @param y the y
   * @return the coordinate string
   */
  public String getCoordinateString(double x, double y) {
    String msg;
    if ((Math.abs(x) > 100) || (Math.abs(x) < 0.01) || (Math.abs(y) > 100) ||
        (Math.abs(y) < 0.01)) {
      msg = xLabel + scientificFormat.format((float)x) + yLabel +
            scientificFormat.format((float)y);
    }
    else {
      msg = xLabel + decimalFormat.format((float)x) + yLabel +
            decimalFormat.format((float)y);
    }
    return msg;
  }

}
