package org.opensourcephysics.display;
import java.awt.Color;

/**
 * Defines  color palette used by OSP components.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class DisplayColors {

   static Color[] phaseColors = null;
   //static Color[] lineColors = {Color.red, Color.green, Color.blue, Color.yellow.darker(), Color.cyan, Color.magenta};
   //static Color[] markerColors = {Color.black, Color.blue, Color.red, Color.green, Color.darkGray, Color.lightGray};
   static java.util.Dictionary<Integer,Color> lineColors = new java.util.Hashtable<Integer,Color>();
   static java.util.Dictionary<Integer,Color> markerColors = new java.util.Hashtable<Integer,Color>();
   
   static{
	   lineColors.put(0, Color.RED);
	   lineColors.put(1, Color.GREEN);
	   lineColors.put(2, Color.BLUE);
	   lineColors.put(3, Color.YELLOW.darker());
	   lineColors.put(4, Color.CYAN);
	   lineColors.put(5, Color.MAGENTA);
	   markerColors.put(0, Color.BLACK);
	   markerColors.put(1, Color.BLUE);
	   markerColors.put(2, Color.RED);
	   markerColors.put(3, Color.GREEN);
	   markerColors.put(4, Color.DARK_GRAY);
	   markerColors.put(5, Color.LIGHT_GRAY);
   }
   
   private DisplayColors() {}

   /**
    * Gets an array of colors.
    *
    * @return the color array
    */
   public static Color[] getPhaseToColorArray() {
      if(phaseColors==null) {
         phaseColors = new Color[256];
         for(int i = 0; i<256; i++) {
            double val = Math.abs(Math.sin(Math.PI*i/255));
            int b = (int) (255*val*val);
            val = Math.abs(Math.sin(Math.PI*i/255+Math.PI/3));
            int g = (int) (255*val*val*Math.sqrt(val));
            val = Math.abs(Math.sin(Math.PI*i/255+2*Math.PI/3));
            int r = (int) (255*val*val);
            phaseColors[i] = new Color(r, g, b);
         }
      }
      return phaseColors;
   }

   /**
    * Converts a phase angle in the range [-Pi,Pi] to a color.
    *
    * @param phi phase angle
    * @return the color
    */
   public static Color phaseToColor(double phi) {
      int index = (int) (127.5*(1+phi/Math.PI));
      index = index%255;
      if(phaseColors==null) {
         return getPhaseToColorArray()[index];
      }
	return phaseColors[index];
   }

   /**
    * Gets a random color.
    *
    * @return random color
    */
   public static Color randomColor() {
      return new Color((int) (Math.random()*255), (int) (Math.random()*255), (int) (Math.random()*255));
   }

   /**
    * Gets a line color that matches the index.
    * @param index int
    * @return Color
    */
   static public Color getLineColor(int index) {
	  Color color = lineColors.get(index);
	  if(color==null){  // create and store a new color
		 float h=((float)(index*Math.PI/12))%1;  // each increment moves slightly more than 1/4 turn around color wheel
		 float s=1.0f;  // maximum saturation for vibrant colors
		 float b=0.5f;  // lines are often thin and should be darker for visibility on light backgrounds
		 color= Color.getHSBColor( h,  s,  b);
		 lineColors.put(index, color);
	  }
	return color;
   }

   /**
    * Gets a marker color that matches the index.
    * @param index int
    * @return Color
    */
   static public Color getMarkerColor(int index) {
	   Color color = markerColors.get(index);
	   if(color==null){  // create and store a new color
		 float h=((float)(index*Math.PI/12))%1;  // each increment moves slightly more than 1/4 turn around color wheel
		 float s=1.0f;  // maximum saturation for vibrant colors
		 float b=1.0f;  // marker fill color should be as bright as possible
		 color= Color.getHSBColor( h,  s,  b);
		 markerColors.put(index, color);
	   }
	return color;
   }
}
