/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.*;

import org.opensourcephysics.analysis.FourierSinCosAnalysis;
import org.opensourcephysics.display.*;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLProperty;

/**
 * This tab displays a Dataset and its Fourier spectra in a FourierTool.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class FourierToolTab extends DataToolTab {

  // instance fields
  protected Dataset source;
  protected PlottingPanel sourcePlot;
  protected DataTable sourceTable;
  JSplitPane sourceSplitPane;
  
  /**
   * Constructs a DataToolTab for the specified Data and DataTool.
   *
   * @param data the Data object
   * @param tool the DataTool
   */
  public FourierToolTab(Dataset data, FourierTool tool) {
  	super(createFourierData(data), tool);
  	XMLControlElement xml = new XMLControlElement(data);
  	source = new Dataset();
  	xml.loadObject(source);
  	source.setMarkerColor(Color.red.darker());
  	source.setConnected(true);
  	sourcePlot = new PlottingPanel(
  			source.getXColumnName(), 
  			source.getYColumnName(), 
  			ToolsRes.getString("FourierToolTab.SourcePlot.Title")); //$NON-NLS-1$
  	sourceSplitPane.setLeftComponent(sourcePlot);
  	sourceTable.add(source);
    sourceSplitPane.setDividerLocation(0.7);
  }

  protected static Data createFourierData(Dataset dataset){
    double[] x = dataset.getXPoints();
    double[] y = dataset.getYPoints();
	  if (y.length%2 == 1) { // odd number of points
	    double[] xnew = new double[y.length - 1];
	    double[] ynew = new double[xnew.length];
      System.arraycopy(x, 0, xnew, 0, xnew.length);
      System.arraycopy(y, 0, ynew, 0, ynew.length);
	  	dataset.clear();
	  	dataset.append(xnew, ynew);
	  	x = xnew; y = ynew;
	  }
    FourierSinCosAnalysis fft = new FourierSinCosAnalysis();
    fft.doAnalysis(x,y,0);
    return fft;
  }
  
  /**
   * Initializes this panel.
   */
  protected void init() {
    super.init();
  }

  /**
  /**
   * Creates the GUI.
   */
  protected void createGUI() {
  	super.createGUI();
  	sourceTable = new DataTable();
    // replace bottom pane action
  	bottomPaneCheckbox.removeActionListener(bottomPaneAction);
  	bottomPaneAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        // hide/remove source panel
        splitPanes[1].setDividerSize(splitPanes[2].getDividerSize());
        splitPanes[1].setDividerLocation(1.0);
        // restore if checked
        boolean vis = bottomPaneCheckbox.isSelected();
        splitPanes[1].setEnabled(vis);
        if (vis) {
          int max = splitPanes[1].getDividerLocation();
          int h = 150;
          splitPanes[1].setDividerSize(splitPanes[0].getDividerSize());
          splitPanes[1].setDividerLocation(max-h-10);
        	sourceTable.refreshTable();
          sourcePlot.addDrawable(source);
        }
        refreshPlot();
      }
    };
  	bottomPaneCheckbox.addActionListener(bottomPaneAction);
    // assemble components
    sourceSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
  	sourceSplitPane.setResizeWeight(1);
    JScrollPane scroller = new JScrollPane(sourceTable);
    sourceSplitPane.setRightComponent(scroller);
    splitPanes[1].setBottomComponent(sourceSplitPane);
  }

  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
  	super.refreshGUI();
    bottomPaneCheckbox.setText(ToolsRes.getString("FourierToolTab.Checkbox.Source.Text")); //$NON-NLS-1$
    bottomPaneCheckbox.setToolTipText(ToolsRes.getString("FourierToolTab.Checkbox.Source.Tooltip")); //$NON-NLS-1$
  }

  /**
   * Overrides DataToolTab method.
   */
  protected void refreshPlot() {
  	super.refreshPlot();
    if (dataTool != null) dataTool.helpLabel.setText(null);
  }
  
  /**
   * Sets the font level. Overrides DataToolTab method.
   *
   * @param level the level
   */
  protected void setFontLevel(int level) {
		double factor = FontSizer.getFactor(level);
    sourcePlot.getAxes().resizeFonts(factor, sourcePlot);
    FontSizer.setFonts(sourceTable, level);
    Font font = sourceTable.getFont();
    sourceTable.setRowHeight(font.getSize()+4);
    super.setFontLevel(level);
  }
  
  /**
   * Returns an ObjectLoader to save and load data for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load data for this class.
   */
  static class Loader implements XML.ObjectLoader {

    public void saveObject(XMLControl control, Object obj) {
      FourierToolTab tab = (FourierToolTab)obj;
      // save name
      control.setValue("name", tab.getName()); //$NON-NLS-1$
      // save source dataset
      control.setValue("source_data", tab.source); //$NON-NLS-1$
      // save column properties but leave out data functions
      DatasetManager data = new DatasetManager();
      ArrayList<Dataset> functions = new ArrayList<Dataset>();
      for (Iterator<Dataset> it = tab.dataManager.getDatasets().iterator(); it.hasNext();) {
      	Dataset next = it.next();
      	if (next instanceof DataFunction) functions.add(next);
      	else data.addDataset(tab.copy(next, null, false));
      }
      control.setValue("columns", data); //$NON-NLS-1$
      if (!functions.isEmpty()) {
    		DataFunction[] f = functions.toArray(new DataFunction[0]);
        control.setValue("data_functions", f); //$NON-NLS-1$
      }
      // save source visibility
      control.setValue("source_visible", tab.bottomPaneCheckbox.isSelected()); //$NON-NLS-1$
      // save props visibility
      control.setValue("props_visible", tab.propsCheckbox.isSelected()); //$NON-NLS-1$
      // save statistics visibility
      control.setValue("stats_visible", tab.statsCheckbox.isSelected()); //$NON-NLS-1$
      // save splitPane locations
      int loc = tab.splitPanes[0].getDividerLocation();
      control.setValue("split_pane", loc); //$NON-NLS-1$
      loc = tab.sourceSplitPane.getDividerLocation();
      control.setValue("source_split_pane", loc); //$NON-NLS-1$
      // save model column order
      int[] cols = tab.dataTable.getModelColumnOrder();
      control.setValue("column_order", cols); //$NON-NLS-1$
      // save hidden markers
      String[] hidden = tab.dataTable.getHiddenMarkers();
      control.setValue("hidden_markers", hidden); //$NON-NLS-1$
      // save column format patterns, if any
      Collection<String> patternColumns = tab.dataTable.getPatternColumns();
      if (!patternColumns.isEmpty()) {
      	ArrayList<String[]> patterns = new ArrayList<String[]>();
        for (Iterator<String> it = patternColumns.iterator(); it.hasNext();) {
        	String colName = it.next();
        	String pattern = tab.dataTable.getFormatPattern(colName);
        	patterns.add(new String[] {colName, pattern});
        }
	      control.setValue("format_patterns", patterns); //$NON-NLS-1$
      }
    }

    public Object createObject(XMLControl control){
      // load data
      Dataset data = (Dataset)control.getObject("source_data"); //$NON-NLS-1$
      return new FourierToolTab(data, null);
    }

    public Object loadObject(XMLControl control, Object obj) {
      final FourierToolTab tab = (FourierToolTab)obj;      
      // load tab name
      tab.setName(control.getString("name")); //$NON-NLS-1$
      // load data functions
      Iterator<?> it = control.getPropertyContent().iterator();
      while (it.hasNext()) {
      	XMLProperty prop = (XMLProperty)it.next();
      	if (prop.getPropertyName().equals("data_functions")) { //$NON-NLS-1$
        	XMLControl[] children = prop.getChildControls();
        	for (int i = 0; i < children.length; i++) {
        		DataFunction f = new DataFunction(tab.dataManager);
        		children[i].loadObject(f);
          	f.setXColumnVisible(false);
          	tab.dataManager.addDataset(f);
        	}
          // refresh dataFunctions
          ArrayList<Dataset> datasets = tab.dataManager.getDatasets();
          for (int i = 0; i < datasets.size(); i++) {
            if (datasets.get(i) instanceof DataFunction) {
            	((DataFunction)datasets.get(i)).refreshFunctionData();
            }    	
          }
          tab.dataTable.refreshTable();
          break;
      	}
      }
      // load source visibility
      boolean vis = control.getBoolean("source_visible"); //$NON-NLS-1$
      tab.bottomPaneCheckbox.setSelected(vis);
      // load props visibility
      vis = control.getBoolean("props_visible"); //$NON-NLS-1$
      tab.propsCheckbox.setSelected(vis);
      // load stats visibility
      vis = control.getBoolean("stats_visible"); //$NON-NLS-1$
      tab.statsCheckbox.setSelected(vis);
      // load splitPane locations
      final int loc = control.getInt("split_pane"); //$NON-NLS-1$
      final int sourceLoc = control.getInt("source_split_pane"); //$NON-NLS-1$
      // load model column order
      int[] cols = (int[])control.getObject("column_order"); //$NON-NLS-1$
      tab.dataTable.setModelColumnOrder(cols);
      if (cols == null) { // for legacy files: load working columns
	      String[] names = (String[])control.getObject("working_columns"); //$NON-NLS-1$
	      if (names != null) {
	      	tab.dataTable.setWorkingColumns(names[0], names[1]);
	      }
      }
      // load hidden markers
      String[] hidden = (String[])control.getObject("hidden_markers"); //$NON-NLS-1$
      tab.dataTable.hideMarkers(hidden);
      // load format patterns
      ArrayList<?> patterns = (ArrayList<?>)control.getObject("format_patterns"); //$NON-NLS-1$
      if (patterns != null) {
      	for (it = patterns.iterator(); it.hasNext();) {
      		String[] next = (String[])it.next();
      		tab.dataTable.setFormatPattern(next[0], next[1]);
      	}
      }
      // load column properties
      DatasetManager columns = (DatasetManager)control.getObject("columns"); //$NON-NLS-1$
      int n = columns.getDatasets().size();
      for (int i = 0; i < n; i++) {
      	Dataset next = columns.getDataset(i);
      	Dataset target = tab.dataTable.getDataset(next.getYColumnName());
      	tab.copy(next, target, false);
      }
      Runnable runner = new Runnable() {
        public synchronized void run() {
          tab.bottomPaneAction.actionPerformed(null);
          tab.propsAndStatsAction.actionPerformed(null);
          tab.splitPanes[0].setDividerLocation(loc);
          tab.sourceSplitPane.setDividerLocation(sourceLoc);
          tab.dataTable.refreshTable();
          tab.propsTable.refreshTable();
          tab.tabChanged(false);
        }
      };
      SwingUtilities.invokeLater(runner);
      return obj;
    }
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
