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

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.*;

import javax.swing.*;

/**
 * A NumberField is a JTextField that formats and displays numbers.
 * This default implementation displays very small and very large numbers
 * in scientific notation and intermediate-value numbers in decimal form.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class NumberField extends JTextField {

    // instance fields
    protected DecimalFormat format = (DecimalFormat)NumberFormat.getInstance();
    protected double prevValue;
    protected Double maxValue;
    protected Double minValue;
    protected int sigfigs;
    protected boolean fixedPattern = false;
    protected String[] patterns = new String[5];
    protected String units;

    /**
     * Constructs a NumberField with default sigfigs (4)
     *
     * @param columns the number of character columns
     */
    public NumberField(int columns) {
    	this(columns, 4);
    }

    /**
     * Constructs a NumberField with specified significant figures.
     *
     * @param columns the number of character columns
     * @param sigfigs the number of significant figures
     */
    public NumberField(int columns, int sigfigs) {
      super(columns);
      setText("0"); //$NON-NLS-1$
      addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if(e.getKeyCode()==KeyEvent.VK_ENTER) {
          	// delay background change so other listeners can look for yellow
            Runnable runner = new Runnable() {
              public synchronized void run() {
                setBackground(Color.white);
                setValue(getValue());
              }
            };
            SwingUtilities.invokeLater(runner); 
          } else {
            setBackground(Color.yellow);
          }
        }
      });
      addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
        	// delay background change so other listeners can look for yellow
          Runnable runner = new Runnable() {
            public synchronized void run() {
              setBackground(Color.white);
              setValue(getValue());
            }
          };
          SwingUtilities.invokeLater(runner);
        }
      });
      addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
        	if (e.getClickCount() == 2) selectAll();
        }
      });
      setSigFigs(sigfigs);
    }

    /**
     * Gets the value from the text field.
     *
     * @return the value
     */
    public double getValue() {
    	String s = getText().trim();
    	// strip units, if any
    	if (units != null && !units.equals("")) { //$NON-NLS-1$
    		int n = s.indexOf(units);
    		while (n > -1) {
    			s = s.substring(0, n);
      		n = s.indexOf(units);
    		}
    	}
      if (s.equals(format.format(prevValue))) return prevValue;
      double retValue;
      try {
        retValue = format.parse(s).doubleValue();
        if (minValue != null && retValue < minValue.doubleValue()) {
          setValue(minValue.doubleValue());
          return minValue.doubleValue();
        }
        if (maxValue != null && retValue > maxValue.doubleValue()) {
          setValue(maxValue.doubleValue());
          return maxValue.doubleValue();
        }
      } catch (ParseException e) {
        Toolkit.getDefaultToolkit().beep();
        setValue(prevValue);
        return prevValue;
      }
      return retValue;
    }

    /**
     * Formats the specified value and enters it in the text field.
     *
     * @param value the value to be entered
     */
    public void setValue(double value) {
      if (!isVisible()) return;
      if (minValue != null)
        value = Math.max(value, minValue.doubleValue());
      if (maxValue != null)
        value = Math.min(value, maxValue.doubleValue());
      setFormatFor(value);
      String s = format.format(value);
      if (units != null) s +=units;
      if (!s.equals(getText()))
      	setText(s);
      prevValue = value;
    }

    /**
     * Sets the expected range of values for this number field.
     * Note this does not set a firm max or min--only an expectation.
     *
     * @param lower the lower end of the range
     * @param upper the upper end of the range
     */
    public void setExpectedRange(double lower, double upper) {
  		fixedPattern = true;
    	double range = Math.max(Math.abs(lower), Math.abs(upper));
    	char d = format.getDecimalFormatSymbols().getDecimalSeparator();
    	if (range < 0.1 || range >= 1000) { // scientific format
    		String s = ""; //$NON-NLS-1$
    		for (int i = 0; i < sigfigs-1; i++) s += "0"; //$NON-NLS-1$
    		format.applyPattern("0"+d+s+"E0"); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	else { // decimal format
    		int n;
    		if (range < 1) n = sigfigs;
    		else if (range < 10) n = sigfigs-1;
    		else if (range < 100) n = sigfigs-2;
    		else n = sigfigs-3;
    		String s = ""; //$NON-NLS-1$
    		for (int i = 0; i < n; i++) s += "0"; //$NON-NLS-1$
    		if (s.equals("")) format.applyPattern("0"); //$NON-NLS-1$ //$NON-NLS-2$
    		else format.applyPattern("0"+d+s); //$NON-NLS-1$
    	}
    }

    /**
     * Sets the number of significant figures for this number field.
     *
     * @param sigfigs the number of significant figures (between 2 and 6)
     */
    public void setSigFigs(int sigfigs) {
    	if (this.sigfigs == sigfigs) return;
    	sigfigs = Math.max(sigfigs, 2);
    	this.sigfigs = Math.min(sigfigs, 6);
    	char d = format.getDecimalFormatSymbols().getDecimalSeparator();
    	if (sigfigs == 2) {
    		patterns[0] = "0"+d+"0E0"; // value < 1 //$NON-NLS-1$ //$NON-NLS-2$
    		patterns[1] = "0"+d+"0"; // value < 10 //$NON-NLS-1$ //$NON-NLS-2$
    		patterns[2] = "0"; // value < 100 //$NON-NLS-1$
    		patterns[3] = "0"+d+"0E0"; // value < 1000 //$NON-NLS-1$ //$NON-NLS-2$
    		patterns[4] = "0"+d+"0E0"; // value > 1000 //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	else if (sigfigs == 3) {
    		patterns[0] = "0"+d+"00E0"; // value < 1 //$NON-NLS-1$ //$NON-NLS-2$
    		patterns[1] = "0"+d+"00"; // value < 10 //$NON-NLS-1$ //$NON-NLS-2$
    		patterns[2] = "0"+d+"0"; // value < 100 //$NON-NLS-1$ //$NON-NLS-2$
    		patterns[3] = "0"; // value < 1000 //$NON-NLS-1$
    		patterns[4] = "0"+d+"00E0"; // value > 1000 //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	else if (sigfigs >= 4) {
    		patterns[0] = "0"+d+"000E0"; // value < 1 //$NON-NLS-1$ //$NON-NLS-2$
    		patterns[1] = "0"+d+"000"; // value < 10 //$NON-NLS-1$ //$NON-NLS-2$
    		patterns[2] = "0"+d+"00"; // value < 100 //$NON-NLS-1$ //$NON-NLS-2$
    		patterns[3] = "0"+d+"0"; // value < 1000 //$NON-NLS-1$ //$NON-NLS-2$
    		patterns[4] = "0"+d+"000E0"; // value > 1000 //$NON-NLS-1$ //$NON-NLS-2$
    		int n = sigfigs-4;
    		for (int i = 0; i < n; i++) {
    			for (int j = 0; j < patterns.length; j++) {
    				patterns[j]= "0"+d+"0"+patterns[j].substring(2); //$NON-NLS-1$ //$NON-NLS-2$
    			}
    		}
    	}
    }

    /**
     * Sets a minimum value for this field.
     *
     * @param min the minimum allowed value
     */
    public void setMinValue(double min) {
    	if (Double.isNaN(min)) minValue = null;
    	else minValue = new Double(min);
    }

    /**
     * Sets a maximum value for this field.
     *
     * @param max the maximum allowed value
     */
    public void setMaxValue(double max) {
    	if (Double.isNaN(max)) maxValue = null;
    	else maxValue = new Double(max);
    }

    /**
     * Sets the units.
     *
     * @param units the units
     */
    public void setUnits(String units) {
    	// replace old units with new in text
    	double val = getValue();
      this.units = units;
      setValue(val);
    }

    /**
     * Gets the units.
     *
     * @return units the units
     */
    public String getUnits() {
      return units;
    }

    /**
     * Gets the format for this field.
     *
     * @return the format
     */
    public DecimalFormat getFormat() {
      return format;
    }

    /**
     * Sets the format for a specified value.
     *
     * @param value the value to be displayed
     */
    public void setFormatFor(double value) {
    	if (fixedPattern) return;
      value = Math.abs(value);
      if (value < 1) format.applyPattern(patterns[0]);
        else if (value < 10) format.applyPattern(patterns[1]);
        else if (value < 100) format.applyPattern(patterns[2]);
        else if (value < 1000) format.applyPattern(patterns[3]);
        else format.applyPattern(patterns[4]);    	
    }

    /**
     * Sets the patterns for this field. The patterns are applied as follows:
     * value<1: patterns[0]
     * value<10: patterns[1]
     * value<100: patterns[2]
     * value<1000: patterns[3]
     * value>=1000: patterns[4]
     * 
     * @param patterns the desired patterns
     */
    public void setPatterns(String[] patterns) {
    	if (patterns.length > 4) this.patterns = patterns;
    }

}
