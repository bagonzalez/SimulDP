/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.util.*;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.*;

/**
 * This provides a GUI for Fourier analysis.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class FourierTool extends DataTool {

  /**
   * A shared Fourier tool.
   */
  final static FourierTool FOURIER_TOOL = new FourierTool();

  /**
   * Gets the shared FourierTool.
   *
   * @return the shared FourierTool
   */
  public static DataTool getTool() {
    return FOURIER_TOOL;
  }

  /**
   * Main entry point when used as application.
   *
   * @param args args[0] may be a data or xml file name
   */
  public static void main(String[] args) {
  	FOURIER_TOOL.exitOnClose = true;
  	FOURIER_TOOL.saveChangesOnClose = true;
  	if (args != null && args.length > 0 && args[0] != null)
  		FOURIER_TOOL.open(args[0]);
  	FOURIER_TOOL.setVisible(true);
  }
  
  /**
   * Constructs a blank FourierTool.
   */
  public FourierTool() {
    super(ToolsRes.getString("FourierTool.Frame.Title"), "FourierTool"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Constructs a FourierTool and opens the specified xml file.
   *
   * @param fileName the name of the xml file
   */
  public FourierTool(String fileName) {
    super(fileName);
  }

  /**
   * Constructs a FourierTool and opens data in the specified xml control.
   *
   * @param control the xml control
   */
  public FourierTool(XMLControl control) {
    super(control);
  }

  /**
   * Constructs a FourierTool and loads the specified data object.
   *
   * @param data the data
   */
  public FourierTool(Data data) {
    super(data);
  }

  /**
   * Adds tabs for the specified Data object and proposes a name
   * for the tab. The name will be modified if not unique.
   *
   * @param data the Data
   * @param name a proposed tab name
   * @return the last added tab, if any
   */
  public DataToolTab addTab(Data data, String name) {
  	FourierToolTab tab = null;
	  ArrayList<Dataset> datasets = data.getDatasets();
	  if (datasets != null) {
	  	for (Iterator<Dataset> it = datasets.iterator(); it.hasNext();) {
	  		Dataset next = it.next();
	  		tab = new FourierToolTab(next, this);
	  		tab.setName(next.getName());
	      addTab(tab);
	  	}
	  }
    return tab;
  }

  /**
   * Adds a tab and loads data from an xml control.
   *
   * @param control the xml control
   * @return the newly added tab, or null if failed
   */
  public DataToolTab addTab(XMLControl control) {
  	// if control is for FourierToolTab class, load tab from control and add it
    if (FourierToolTab.class.isAssignableFrom(control.getObjectClass())) {
    	FourierToolTab tab = (FourierToolTab)control.loadObject(null);
    	tab.dataTool = this;
    	addTab(tab);
    	return tab;
    }
    // otherwise load data from control into a null tab (new one created)
    return loadData(null, control, useChooser);
  }

  /**
   * Overrides DataTool method. FourierTool is never editable.
   *
   * @param editable ignored
   */
  public void setUserEditable(boolean editable) {
  }

// ______________________________ protected methods _____________________________
  
  /**
   * Creates the GUI.
   */
  protected void createGUI() {
  	super.createGUI();
  	fileMenu.remove(newTabItem);
  	emptyFileMenu.remove(emptyNewTabItem);
  	helpPath = "fourier_tool_help.html"; //$NON-NLS-1$  
  }
  
  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
  	super.refreshGUI();
    setTitle(ToolsRes.getString("FourierTool.Frame.Title")); //$NON-NLS-1$
  	helpItem.setText(ToolsRes.getString("FourierTool.MenuItem.Help")); //$NON-NLS-1$
  }

  /**
   * Imports character-delimited data. Overrides DataTool method.
   *
   * @param dataString the data
   * @param fileName name of file being imported (may be null)
   * @return DatasetManager with imported data, or null if none found
   */
  protected DatasetManager importCharDelimitedData(String dataString, String fileName) {
    DatasetManager data = super.importCharDelimitedData(dataString, fileName);
    if (data == null || data.getDatasets().size() < 2) 
    	return data;
    // replace row values with y values of first dataset
    // and row name with y column name
    double[] x = data.getYPoints(0);
    String s = data.getDataset(0).getYColumnName();
    for (int i = 1; i < data.getDatasets().size(); i++) {
      Dataset next = data.getDataset(i);
      double[] y = next.getYPoints();
      next.clear();
      next.append(x, y);
      next.setXYColumnNames(s, next.getYColumnName());
    }
    // remove first dataset
    data.removeDataset(0);
    return data;
  }

}
/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
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
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
