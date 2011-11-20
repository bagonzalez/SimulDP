package org.opensourcephysics.display;

import org.opensourcephysics.display.axes.AxisFactory;
import org.opensourcephysics.display.axes.PolarAxes;
import org.opensourcephysics.display.axes.PolarType1;
import org.opensourcephysics.display.axes.PolarType2;
public class PlottingPanelFactory{

   private PlottingPanelFactory(){}

   /**
    *  Constructs a new PlottingPanel with cartesian type 1 axes using the given X axis label, Y axis
    *  label, and plot title.
    *
    * @param  xlabel      The X axis label.
    * @param  ylabel      The Y axis label.
    * @param  plotTitle   The plot title.
    */
   public static PlottingPanel createType1(String xlabel, String ylabel,
                                           String plotTitle){
      PlottingPanel panel = new PlottingPanel(xlabel, ylabel, plotTitle);
      panel.axes = AxisFactory.createAxesType1(panel);
      panel.axes.setXLabel(xlabel, null);
      panel.axes.setYLabel(ylabel, null);
      panel.axes.setTitle(plotTitle, null);
      return panel;
   }

   /**
 *  Constructs a new PlottingPanel with cartesian type 2 axes using the given X axis label, Y axis
 *  label, and plot title.
 *
 * @param  xlabel      The X axis label.
 * @param  ylabel      The Y axis label.
 * @param  plotTitle   The plot title.
 */
public static PlottingPanel createType2(String xlabel, String ylabel, String plotTitle) {
  PlottingPanel panel = new PlottingPanel(xlabel, ylabel, plotTitle);
  panel.axes = AxisFactory.createAxesType2(panel);
  panel.axes.setXLabel(xlabel, null);
  panel.axes.setYLabel(ylabel, null);
  panel.axes.setTitle(plotTitle, null);
  return panel;
}

/**
 *  Constructs a new PlottingPanel with polar type 1 axes using the given title.
 *
 * @param  plotTitle   the plot title.
 */
public static PlottingPanel createPolarType1(String plotTitle, double deltaR) {
  PlottingPanel panel = new PlottingPanel(null, null, plotTitle);
  PolarAxes axes = new PolarType1(panel);
  axes.setDeltaR(deltaR);
  axes.setDeltaTheta(Math.PI/8); // spokes are separate by PI/8
  panel.setTitle(plotTitle);
  panel.setSquareAspect(true);
  return panel;
}

   /**
 *  Constructs a new PlottingPanel with polar type 2 axes using the given title.
 *
 * @param  plotTitle   the plot title.
 */
public static PlottingPanel createPolarType2(String plotTitle, double deltaR) {
  PlottingPanel panel = new PlottingPanel(null, null, plotTitle);
  PolarAxes axes = new PolarType2(panel);
  axes.setDeltaR(deltaR);        // circles are separated by one unit
  axes.setDeltaTheta(Math.PI/8); // spokes are separate by PI/8
  panel.setTitle(plotTitle);
  panel.setSquareAspect(true);
  return panel;
}

/**
 *  Constructs a new PlottingPanel with cartesian type 3 axes using the given X axis label, Y axis
 *  label, and plot title.
 *
 * @param  xlabel      The X axis label.
 * @param  ylabel      The Y axis label.
 * @param  plotTitle   The plot title.
 */
public static PlottingPanel createType3(String xlabel, String ylabel, String plotTitle) {
  PlottingPanel panel = new PlottingPanel(xlabel, ylabel, plotTitle);
  panel.axes = AxisFactory.createAxesType3(panel);
  panel.axes.setXLabel(xlabel, null);
  panel.axes.setYLabel(ylabel, null);
  panel.axes.setTitle(plotTitle, null);
  return panel;
}



}
