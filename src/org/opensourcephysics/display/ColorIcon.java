/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2005  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.display;

import java.awt.*;
import javax.swing.*;

/**
 * This Icon fills itself with the color specified in its constructor.
 *
 * @author Douglas Brown
 */
public class ColorIcon implements Icon {

  // instance fields
  private int w;
  private int h;
  private Color color;

  /**
   * Constructs a ColorIcon.
   *
   * @param color color of the icon
   * @param width width of the icon
   * @param height height of the icon
   */
  public ColorIcon(Color color, int width, int height) {
    w = width;
    h = height;
    setColor(color);
  }

  /**
   * Sets the color.
   *
   * @param color the desired color
   */
  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Gets the icon width.
   *
   * @return the icon width
   */
  public int getIconWidth() {
    return w;
  }

  /**
   * Gets the icon height.
   *
   * @return the icon height
   */
  public int getIconHeight() {
    return h;
  }

  /**
   * Paints the icon.
   *
   * @param c the component on which it is painted
   * @param _g the graphics context
   * @param x the x coordinate of the icon
   * @param y the y coordinate of the icon
   */
  public void paintIcon(Component c, Graphics _g, int x, int y) {
    Graphics2D g = (Graphics2D)_g;
    Rectangle rect = new Rectangle(x, y, w, h);
    Paint gPaint = g.getPaint();
    g.setPaint(color);
    g.fill(rect);
    // restore graphics paint
    g.setPaint(gPaint);
  }
}
