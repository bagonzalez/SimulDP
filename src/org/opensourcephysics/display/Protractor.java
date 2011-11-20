package org.opensourcephysics.display;

import java.awt.geom.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.awt.Stroke;

/**
 * A Protractor with an arrow that can be used to measure angles.
 *
 * @author W. Christian
 * @version 1.0
 */
public class Protractor  extends InteractiveCircle implements Drawable{
  static final double PI2=Math.PI*2;
  static final String thetaStr = "$\\theta$="; //$NON-NLS-1$
  int protractorRadius, protractorRadius2, arrowLengthPix;
  protected Tip tip = new Tip();
  protected double arrowTheta = 0, orientation = 0;
  protected DecimalFormat f = new DecimalFormat("000"); //$NON-NLS-1$
  protected boolean showTheta = false;
  protected InteractiveLabel tauBox = new InteractiveLabel(thetaStr+f.format(getTheta()));


  /**
   * Constructs a protractor with the given pixel size.
   *
   * @param protractorRadius int
   */
  public Protractor(int protractorRadius) {
    this.protractorRadius=protractorRadius;
    protractorRadius2=protractorRadius*2;
    arrowLengthPix = protractorRadius;
    tip.color=Color.BLUE;
    tauBox.setOffsetX(-20);
    tauBox.setOffsetY(5);
  }

  /**
   * Constructs a protractor with a default radius of 40 pixels.
   */
  public Protractor() {
    this(40);
  }

  /**
   * Sets the angle of the arrow on the protractor.
   * @param theta double
   */
  public void setTheta(double angle){
    this.arrowTheta=angle+orientation;
  }
  /**
   * Gets the angle of the arrow on the protractor.
   * @return double
   */
  public double getTheta(){
    // return PBC.separation(arrowTheta-orientation,Math.PI*2);
    // inline the method for efficiency and to decouple from the numerics package
    double dr=arrowTheta-orientation;
    return dr-PI2*Math.floor(dr/PI2+0.5);
  }

  /**
   * Sets the orientation of the protractor.
   * @param angle double
   */
  public void setOrientation(double angle){
    this.orientation=angle;
  }
  /**
   * Gets the orientation of the protractor.
   * @return double
   */
  public double getOrientation(){
    return orientation;
  }

  /**
   * Shows theta when the protractor is drawn when true.
   * @param show boolean
   */
  public void setShowTheta(boolean show) {
     showTheta = show;
  }
  /**
   * Gets the show theta property.
   * @return boolean
   */
  public boolean isShowTheta(){
    return showTheta;
  }

  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    Interactive interactive = super.findInteractive(panel, xpix, ypix);
    if (interactive != null)return interactive;
    interactive = tip.findInteractive(panel,xpix,ypix);
    if (interactive != null)return interactive;
    return tauBox.findInteractive(panel, xpix, ypix);
  }


  /**
   * Draws the protractor on the given drawing panel.
   *
   * @param panel DrawingPanel
   * @param g Graphics
   */
  public void draw(DrawingPanel panel, Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    double i1 = panel.xToPix(x);
    double j1 = panel.yToPix(y);
    //draw the vector with angle ticks
    g2.setColor(new Color(240,40,40,40));
    // replace the circle with two half circles to indicate angle
    //g2.fill(new Double(i1-pixRadius,j1-pixRadius,pixRadius2,pixRadius2));  // draws circle
    double start = Math.toDegrees(orientation);
    g2.fill(new Arc2D.Double(i1-protractorRadius,j1-protractorRadius,protractorRadius2,protractorRadius2,start,180,Arc2D.PIE));
    g2.setColor(new Color(40,40,240,40));
    g2.fill(new Arc2D.Double(i1-protractorRadius,j1-protractorRadius,protractorRadius2,protractorRadius2,start+180,180,Arc2D.PIE));
    g2.setColor(Color.gray);
    g2.setStroke(new BasicStroke(0.5f));
    for(int i=0; i<36;i++){
      AffineTransform at = g2.getTransform();
      at.rotate(-i*Math.PI/18, i1, j1);
      g2.setTransform(at);
      g2.draw(new Line2D.Double(i1+protractorRadius-5, j1, i1 + protractorRadius, j1));
      at.rotate(+i*Math.PI/18, i1, j1);
      g2.setTransform(at);
    }
    tauBox.setText(thetaStr+f.format(Math.toDegrees(getTheta())), x, y);
    if(showTheta)tauBox.draw(panel, g);
    //tip.draw(panel,g);  // draws a small circle at the tip
    //drawing the arrow with the red head
    AffineTransform at = g2.getTransform();
    at.rotate(-arrowTheta, i1, j1);
    g2.setTransform(at);
    g2.setColor(Color.RED);
    Stroke currentStroke= g2.getStroke();
    g2.setStroke(new BasicStroke(1.5f));
    g2.draw(new Line2D.Double(i1, j1, i1 + arrowLengthPix, j1));
    GeneralPath arrowHead = new GeneralPath();
    g2.draw(new Line2D.Double());
    arrowHead.moveTo((float)(i1 + arrowLengthPix), (float)j1);
    arrowHead.lineTo((float)(i1 + arrowLengthPix - 15), (float)(j1 - 5));
    arrowHead.lineTo((float)(i1 + arrowLengthPix - 15), (float)(j1 + 5));
    arrowHead.closePath();
    g2.fill(arrowHead);
    g2.draw(arrowHead);
    at.rotate(+arrowTheta, i1, j1);
    g2.setTransform(at);
    g2.setStroke(currentStroke);
    double length=arrowLengthPix/panel.getXPixPerUnit();
    tip.setXY(x+length*Math.cos(arrowTheta),y+length*Math.sin(arrowTheta));
  }

  public class Tip extends InteractiveCircle{
    public void setXY(double x, double y){
      this.x=x;
      this.y=y;
      arrowTheta=Math.atan2(y-Protractor.this.y,x-Protractor.this.x);
    }
  }
}
