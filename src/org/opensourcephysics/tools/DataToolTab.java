/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.beans.*;
import java.lang.reflect.Method;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.display.dialogs.ScaleInspector;
import org.opensourcephysics.tools.DataToolTable.TableEdit;
import org.opensourcephysics.tools.DataToolTable.WorkingDataset;

/**
 * This tab displays and analyzes a single Data object in a DataTool.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class DataToolTab extends JPanel {
  // static fields
  static Color[] colors = {Color.green.darker(), Color.red, Color.cyan.darker(), Color.yellow.darker(), Color.blue};
  // instance fields
  protected DataTool dataTool;                                 // the tool that creates this tab
  protected Data owner;                                        // the first Data object added to this tab
  protected DatasetManager dataManager = new DatasetManager(); // local datasets
  protected JSplitPane[] splitPanes;
  protected DataToolPlotter plot;
  protected DataToolTable dataTable;
  protected DataToolStatsTable statsTable;
  protected DataToolPropsTable propsTable;
  protected JScrollPane statsScroller, propsScroller;
  protected JToolBar toolbar;
  protected JCheckBox statsCheckbox, bottomPaneCheckbox, propsCheckbox;
  protected DatasetCurveFitter curveFitter;
  protected JButton dataBuilderButton, newColumnButton, refreshDataButton;
  protected SelectionBox selectionBox = new SelectionBox();
  protected Point zoomPoint;
  protected Action bottomPaneAction, propsAndStatsAction;
  protected String fileName;
  protected JButton helpButton;
  protected int colorIndex;
  protected boolean tabChanged;
  protected boolean userEditable;
  protected UndoableEditSupport undoSupport;
  protected UndoManager undoManager;

  /**
   * Constructs a DataToolTab for the specified Data and DataTool.
   *
   * @param data the Data object
   * @param tool the DataTool
   */
  public DataToolTab(Data data, DataTool tool) {
    dataTool = tool;
    dataTable = new DataToolTable(this);
    createGUI();
    addData(data);
    refreshGUI();
    tabChanged(false);
  }

  /**
   * Adds new data to this tab.
   *
   * @param data the data to add
   * @return true if added
   */
  public boolean addData(Data data) {
    if(data==null) {
      return false;
    }
    boolean added = false;
    ArrayList<Dataset> list = data.getDatasets();
    if((list!=null)&&(list.size()>0)) {
      Dataset first = list.get(0);
      int rowCount = first.getXPoints().length;
      double[] rows = DataTool.getRowArray(rowCount);
      // case 1: tab contains no data 
      if(dataManager.getDatasets().isEmpty()) {
        owner = data;
        // create a dataset for every variable except rowName
        for(Iterator<Dataset> it = list.iterator(); it.hasNext(); ) {
          final Dataset next = it.next();
          if(next==null) {
            continue;
          }
          double[] x = next.getXPoints();
          double[] y = next.getYPoints();
          XMLControlElement xml = new XMLControlElement(next);
          // first dataset
          if(dataManager.getDatasets().isEmpty()) {
            Dataset local = new HighlightableDataset();
            xml.loadObject(local, true, true);
            local.clear();
            // load x-column if not row numbers
            if(!next.getXColumnName().equals(DataTable.rowName)) {
              // copy x-column into y and rows into x
              local.setXYColumnNames(DataTable.rowName, next.getXColumnName());
              local.append(rows, x);
              // increase ID by 1 to distinguish from y-column
              local.setID(local.getID()+1);
            }
            // else load y-column
            else {
              // copy y-column into y and rows into x
              local.setXYColumnNames(DataTable.rowName, next.getYColumnName());
              local.append(rows, y);
            }
            local.setXColumnVisible(false);
            dataManager.addDataset(local);
          }
          // additional datasets
          // load y-column if x is a match and y is not
          if(isDuplicateColumn(next.getXColumnName(), x)&&!isDuplicateColumn(next.getYColumnName(), y)) {
            Dataset local = new HighlightableDataset();
            xml.loadObject(local, true, true);
            // copy y-column into y and rows into x
            local.setXYColumnNames(DataTable.rowName, next.getYColumnName());
            local.clear();
            local.append(rows, y);
            local.setXColumnVisible(false);
            dataManager.addDataset(local);
          }
          // if neither x nor y is a match, then load new tab
          else if(!isDuplicateColumn(next.getXColumnName(), x)
                  &&!isDuplicateColumn(next.getYColumnName(), next.getYPoints())) {
            Runnable runner = new Runnable() {
              public synchronized void run() {
                dataTool.addTab(next, next.getName());
              }

            };
            SwingUtilities.invokeLater(runner);
          }
        }
        added = true;
      }
      // case 2: tab already contains data 
      // add only Datasets with a column that matches an existing column
      else {
        for(Iterator<Dataset> it = list.iterator(); it.hasNext(); ) {
          Dataset next = it.next();
          String col = null;
          if(isDuplicateColumn(next.getXColumnName(), next.getXPoints())
            &&!isDuplicateColumn(next.getYColumnName(), next.getYPoints())) {
            col = "y"; //$NON-NLS-1$
          } else if(isDuplicateColumn(next.getYColumnName(), next.getYPoints())
                    &&!isDuplicateColumn(next.getXColumnName(), next.getXPoints())) {
            col = "x";                             //$NON-NLS-1$
          }
          // load column if col not null
          if(col!=null) {
            // create local dataset and load properties
            Dataset local = new HighlightableDataset();
            XMLControlElement xml = new XMLControlElement(next);
            xml.loadObject(local, true, true);
            double[] pts = col.equals("x") //$NON-NLS-1$
                           ? next.getXPoints()
                           : next.getYPoints();
            local.setXColumnVisible(false);
            local.clear();
            local.append(rows, pts);
            // assign a unique column name
            String name = col.equals("x") //$NON-NLS-1$
                          ? next.getXColumnName()
                          : next.getYColumnName();
            name = getUniqueYColumnName(local, name, false);
            local.setXYColumnNames(DataTable.rowName, name);
            // if x, increase ID by 1 to distinguish from y-column
            if(col.equals("x")) { //$NON-NLS-1$
              local.setID(local.getID()+1);
            }
            dataManager.addDataset(local);
            dataTable.getWorkingData(name);
            added = true;
          }
        }
      }
      dataTable.refreshTable();
      statsTable.refreshStatistics();
      refreshGUI();
      refreshPlot();
    }
    if(added) {
      tabChanged(true);
    }
    return added;
  }

  /**
   * Sets the x and y columns by name.
   *
   * @param xColName the name of the horizontal axis variable
   * @param yColName the name of the vertical axis variable
   */
  public void setXYColumns(String xColName, String yColName) {
    dataTable.setWorkingColumns(xColName, yColName);
  }

  /**
   * Sets the connected property for a given column.
   *
   * @param colName the name of the column
   * @param connected true to connect points with lines
   */
  public void setConnected(String colName, boolean connected) {
    WorkingDataset working = dataTable.workingMap.get(colName);
    if(working!=null) {
      working.setConnected(connected);
    }
  }

  /**
   * Sets the markers visible property for a given a column.
   *
   * @param colName the name of the column
   * @param visible true to show markers
   */
  public void setMarkersVisible(String colName, boolean visible) {
    WorkingDataset working = dataTable.workingMap.get(colName);
    if(working!=null) {
      working.setMarkersVisible(visible);
    }
  }

  /**
   * Overrides Component.getName();
   *
   * @param name the name
   */
  public void setName(String name) {
    super.setName(name);
    curveFitter.setDataSourceName(name);
  }

  // _______________________ protected & private methods __________________________

  /**
   * Sets the font level.
   *
   * @param level the level
   */
  protected void setFontLevel(int level) {
    FontSizer.setFonts(statsTable, level);
    FontSizer.setFonts(propsTable, level);
    curveFitter.setFontLevel(level);
    double factor = FontSizer.getFactor(level);
    plot.getAxes().resizeFonts(factor, plot);
    FontSizer.setFonts(plot.getPopupMenu(), level);
    if(propsTable.styleDialog!=null) {
      FontSizer.setFonts(propsTable.styleDialog, level);
      propsTable.styleDialog.pack();
    }
    Runnable runner = new Runnable() {
      public void run() {
        bottomPaneAction.actionPerformed(null);
        propsAndStatsAction.actionPerformed(null);
        propsTable.refreshTable();
      }

    };
    SwingUtilities.invokeLater(runner);
  }

  protected void tabChanged(boolean changed) {
    tabChanged = changed;
  }

  /**
   * Gets the working dataset.
   *
   * @return the first two data columns in the datatable (x-y order)
   */
  protected WorkingDataset getWorkingData() {
    dataTable.getSelectedData();
    return dataTable.workingData;
  }

  /**
   * Returns the data object that owns this tab.
   *
   * @return the owner Data
   */
  protected Data getOwner() {
    return owner;
  }

  /**
   * Returns a column name that is unique to this tab, contains
   * no spaces, and is not reserved by the OSP parser.
   *
   * @param d the dataset
   * @param proposed the proposed name for the column
   * @param askUser true to ask user to approve changes
   * @return unique name
   */
  protected String getUniqueYColumnName(Dataset d, String proposed, boolean askUser) {
    if(proposed==null) {
      return null;
    }
    // remove all spaces
    proposed = proposed.replaceAll(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
    // check for duplicate or reserved names
    if(askUser) {
      int tries = 0, maxTries = 2;
      while(tries<maxTries) {
        tries++;
        if(isDuplicateName(d, proposed)) {
          proposed = JOptionPane.showInputDialog(this, "\""+proposed+"\" "+       //$NON-NLS-1$ //$NON-NLS-2$
            ToolsRes.getString("DataFunctionPanel.Dialog.DuplicateName.Message"), //$NON-NLS-1$
            ToolsRes.getString("DataFunctionPanel.Dialog.DuplicateName.Title"),   //$NON-NLS-1$
            JOptionPane.WARNING_MESSAGE);
        }
        if((proposed==null)||proposed.equals("")) { //$NON-NLS-1$
          return null;  
        }
        if(isReservedName(proposed)) {
          proposed = JOptionPane.showInputDialog(this, "\""+proposed+"\" "+       //$NON-NLS-1$ //$NON-NLS-2$
            ToolsRes.getString("DataToolTab.Dialog.ReservedName.Message"),        //$NON-NLS-1$
            ToolsRes.getString("DataToolTab.Dialog.ReservedName.Title"),          //$NON-NLS-1$
            JOptionPane.WARNING_MESSAGE);
        }
        if((proposed==null)||proposed.equals("")) { //$NON-NLS-1$
          return null;
        }
      }
    }
    int i = 0;
    // trap for names that are numbers
    try {
      Double.parseDouble(proposed);
      proposed = ToolsRes.getString("DataToolTab.NewColumn.Name"); //$NON-NLS-1$
    } catch(NumberFormatException ex) {}
    String name = proposed;
    while(isDuplicateName(d, name)||isReservedName(name)) {
      i++;
      name = proposed+"_"+i; //$NON-NLS-1$
    }
    return name;
  }

  /**
   * Returns true if name is a duplicate of an existing dataset.
   *
   * @param d the dataset
   * @param name the proposed name for the dataset
   * @return true if duplicate
   */
  protected boolean isDuplicateName(Dataset d, String name) {
    if(dataManager.getDatasets().isEmpty()) {
      return false;
    }
    if(dataManager.getDataset(0).getXColumnName().equals(name)) {
      return true;
    }
    name = TeXParser.removeSubscripting(name);
    Iterator<Dataset> it = dataManager.getDatasets().iterator();
    while(it.hasNext()) {
      Dataset next = it.next();
      if(next==d) {
        continue;
      }
      String s = TeXParser.removeSubscripting(next.getYColumnName());
      if(s.equals(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if name is reserved by the OSP parser.
   *
   * @param name the proposed name
   * @return true if reserved
   */
  protected boolean isReservedName(String name) {
    String[] s = FunctionTool.parserNames;
    for(int i = 0; i<s.length; i++) {
      if(s[i].equals(name)) {
        return true;
      }
    }
    s = UserFunction.dummyVars;
    for(int i = 0; i<s.length; i++) {
      if(s[i].equals(name)) {
        return true;
      }
    }
    try {
      Double.parseDouble(name);
      return true;
    } catch(NumberFormatException ex) {}
    return false;
  }

  /**
   * Responds to a changed column name.
   *
   * @param oldName the previous name
   * @param newName the new name
   */
  protected void columnNameChanged(String oldName, String newName) {
    tabChanged(true);
    String pattern = dataTable.getFormatPattern(oldName);
    dataTable.removeWorkingData(oldName);
    dataTable.getWorkingData(newName);
    dataTable.setFormatPattern(newName, pattern);
    if((propsTable.styleDialog!=null)&&propsTable.styleDialog.isVisible()
      &&propsTable.styleDialog.getName().equals(oldName)) {
      propsTable.styleDialog.setName(newName);
      String title = ToolsRes.getString("DataToolPropsTable.Dialog.Title"); //$NON-NLS-1$
      String var = TeXParser.removeSubscripting(newName);
      propsTable.styleDialog.setTitle(title+" \""+var+"\"");                //$NON-NLS-1$ //$NON-NLS-2$
    }
    Dataset working = getWorkingData();
    if(working==null) {
      return;
    }
    refreshPlot();
  }

  /**
   * Reloads data from a Data source.
   */
  protected boolean reloadData(Data data) {
    // update existing datasets in data manager
    ArrayList<Dataset> list = dataManager.getDatasets();
    if((list!=null)&&!list.isEmpty()) {
      for(Iterator<Dataset> it = list.iterator(); it.hasNext(); ) {
        Dataset local = it.next();
        Dataset match = getMatchingID(local, data);
        if((match!=null)&&(match!=local)) {
          local.clear();
          local.setName(match.getName());
          double[] rows = DataTool.getRowArray(match.getIndex());
          // case 1: x-column name matches local
          if(match.getXColumnName().equals(local.getYColumnName())) {
            local.append(rows, match.getXPoints());
          }
          // case 2: y-column name matches local
          else {
            local.append(rows, match.getYPoints());
          }
        }
      }
    }
    // add new datasets to table
    list = data.getDatasets();
    boolean added = false;
    if(list!=null) {
      for(Iterator<Dataset> it = list.iterator(); it.hasNext(); ) {
        Dataset next = it.next();
        Dataset match = getMatchingID(next, dataManager);
        if(match==null) { // next is not in this tab
          // if next is owner of another tab, update that tab
          DataToolTab tab = dataTool.getTab(next);
          if((tab!=null)&&(tab!=this)) {
            dataTool.update(next);
            // else add dataset to this tab
          } else {
            added = addData(next)||added;
          }
        }
      }
    }
    // refresh dataFunctions and pad rows if nec
    for (Dataset next: dataManager.getDatasets()) {
      if(next instanceof DataFunction) {
        ((DataFunction)next).refreshFunctionData();
      }
      else pad(next);
    }
    dataTable.trimEmptyRows(0);
    refreshPlot();
    dataTable.refreshTable();
    statsTable.refreshStatistics();
    return added;
  }

  /**
   * Pads missing rows with NaN.
   * 
   * @param dataset the dataset to pad
   */
  protected void pad(Dataset dataset) {
  	int n = dataTable.getRowCount();
    if(dataset instanceof DataFunction) return;
    double[] y = dataset.getYPoints();
    if(n > y.length) {
      double[] yNew = new double[n];
      System.arraycopy(y, 0, yNew, 0, y.length);
      for (int i = y.length; i < n; i++) {
      	yNew[i] = Double.NaN;
      }
      double[] rows = DataTool.getRowArray(n);
      dataset.clear();
      dataset.append(rows, yNew);
    }
  }

  /**
   * Copies a dataset.
   *
   * @param original the source dataset
   * @param target the target dataset (may be null)
   * @return the copy
   */
  protected Dataset createDataset() {
    colorIndex = colorIndex%colors.length;
    Color markerColor = colors[colorIndex++];
    Color lineColor = markerColor.darker();
    Dataset newData = new HighlightableDataset(markerColor, lineColor, false);
    int rowCount = Math.max(1, dataTable.getRowCount());
    double[] rows = new double[rowCount];
    double[] y = new double[rowCount];
    for(int i = 0; i<rowCount; i++) {
      rows[i] = i;
      y[i] = Double.NaN;
    }
    newData.append(rows, y);
    newData.setXColumnVisible(false);
    return newData;
  }

  /**
   * Clones a dataset.
   *
   * @param source the source dataset
   * @param target the target dataset (may be null)
   * @return the clone
   */
  protected Dataset copy(Dataset source, Dataset target, boolean includeDataAndID) {
    if(target==null) {
      target = new Dataset();
    }
    if(includeDataAndID) {
      target.clear();
      double[] x = source.getXPoints();
      double[] y = source.getYPoints();
      target.append(x, y);
      target.setID(source.getID());
    }
    target.setName(source.getName());
    target.setXYColumnNames(source.getXColumnName(), source.getYColumnName());
    target.setMarkerShape(source.getMarkerShape());
    target.setMarkerSize(source.getMarkerSize());
    Color fill = source.getFillColor();
    Color edge = source.getEdgeColor();
    target.setMarkerColor(fill, edge);
    target.setLineColor(source.getLineColor());
    target.setConnected(source.isConnected());
    return target;
  }

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    setLayout(new BorderLayout());
    splitPanes = new JSplitPane[3];
    // splitPanes[0] is plot/fitter on left, tables on right
    splitPanes[0] = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPanes[0].setResizeWeight(1);
    splitPanes[0].setOneTouchExpandable(true);
    // splitPanes[1] is plot on top, fitter on bottom
    splitPanes[1] = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPanes[1].setResizeWeight(1);
    splitPanes[1].setDividerSize(0);
    // splitPanes[2] is stats/props tables on top, data table on bottom
    splitPanes[2] = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPanes[2].setDividerSize(0);
    splitPanes[2].setEnabled(false);
    // create data table
    dataTable.setRowNumberVisible(true);
    JScrollPane dataScroller = new JScrollPane(dataTable);
    dataTable.refreshTable();
    dataTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
      public void columnAdded(TableColumnModelEvent e) {

      /** empty block */
      }
      public void columnRemoved(TableColumnModelEvent e) {

      /** empty block */
      }
      public void columnSelectionChanged(ListSelectionEvent e) {

      /** empty block */
      }
      public void columnMarginChanged(ChangeEvent e) {

      /** empty block */
      }
      public void columnMoved(TableColumnModelEvent e) {
        Dataset prev = dataTable.workingData;
        Dataset working = getWorkingData();
        if(working!=prev) {
          tabChanged(true);
          // set default variable of all fit editors for this tab
          String var = working.getXColumnName();
          if(curveFitter.fitBuilder!=null) {
            Iterator<String> it = curveFitter.fitBuilder.getPanelNames().iterator();
            while(it.hasNext()) {
              FunctionPanel panel = curveFitter.fitBuilder.getPanel(it.next().toString());
              UserFunctionEditor editor = (UserFunctionEditor) panel.getFunctionEditor();
              editor.setDefaultVariables(new String[] {var});
              editor.repaint();
            }
          }
        }
        if((working==null)||(working==prev)) {
          return;
        }
        selectionBox.setSize(0, 0);
        refreshPlot();
      }

    });
    ListSelectionModel selectionModel = dataTable.getSelectionModel();
    selectionModel.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        try {
          curveFitter.setData(dataTable.getSelectedData());
        } catch(Exception ex) {}
      }

    });
    // create fit action and checkbox
    bottomPaneAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if(bottomPaneCheckbox==null) {
          return;
        }
        // hide/remove curveFit
        splitPanes[1].setDividerSize(splitPanes[2].getDividerSize());
        splitPanes[1].setDividerLocation(1.0);
        plot.removeDrawables(FunctionDrawer.class);
        // restore if checked
        boolean vis = bottomPaneCheckbox.isSelected();
        splitPanes[1].setEnabled(vis);
        if(vis) {
          int max = splitPanes[1].getDividerLocation();
          int h = curveFitter.getPreferredSize().height;
          splitPanes[1].setDividerSize(splitPanes[0].getDividerSize());
          splitPanes[1].setDividerLocation(max-h-10);
          plot.addDrawable(curveFitter.getDrawer());
        }
        refreshPlot();
      }

    };
    bottomPaneCheckbox = new JCheckBox();
    bottomPaneCheckbox.setSelected(false);
    bottomPaneCheckbox.setOpaque(false);
    bottomPaneCheckbox.addActionListener(bottomPaneAction);
    // create dataEntryButton button
    newColumnButton = DataTool.createButton(""); //$NON-NLS-1$
    newColumnButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        Dataset newData = createDataset();
        String proposed = ToolsRes.getString("DataToolTab.NewColumn.Name"); //$NON-NLS-1$
        proposed = getUniqueYColumnName(newData, proposed, false);
        Object input = JOptionPane.showInputDialog(dataTool,
          ToolsRes.getString("DataToolTab.Dialog.NameColumn.Message"),      //$NON-NLS-1$
          ToolsRes.getString("DataToolTab.Dialog.NameColumn.Title"),         //$NON-NLS-1$
          JOptionPane.QUESTION_MESSAGE, null, null, proposed);
        if(input==null) {
          return;
        }
        String newName = getUniqueYColumnName(newData, input.toString(), true);
        if(newName==null) {
          return;
        }
        if(newName.equals("")) {                                             //$NON-NLS-1$
          String colName = ToolsRes.getString("DataToolTab.NewColumn.Name"); //$NON-NLS-1$
          newName = getUniqueYColumnName(newData, colName, false);
        }
        OSPLog.finer("adding new column \""+newName+"\"");                   //$NON-NLS-1$ //$NON-NLS-2$
        newData.setXYColumnNames(DataTable.rowName, newName);
        addData(newData);
        dataTable.refreshTable();
        refreshPlot();
        refreshGUI();
        int col = dataTable.getColumnCount()-1;
        // post edit: target is column, value is dataset
        TableEdit edit = dataTable.new TableEdit(DataToolTable.INSERT_COLUMN_EDIT, newName, new Integer(col), newData);
        undoSupport.postEdit(edit);
        dataTable.refreshUndoItems();
        Runnable runner = new Runnable() {
          public synchronized void run() {
            int col = dataTable.getColumnCount()-1;
            dataTable.changeSelection(0, col, false, false);
            dataTable.editCellAt(0, col, e);
            dataTable.editor.field.requestFocus();
          }

        };
        SwingUtilities.invokeLater(runner);
      }

    });
    // create dataBuilderButton
    dataBuilderButton = DataTool.createButton(ToolsRes.getString("DataToolTab.Button.DataBuilder.Text")); //$NON-NLS-1$
    dataBuilderButton.setToolTipText(ToolsRes.getString("DataToolTab.Button.DataBuilder.Tooltip")); //$NON-NLS-1$
    dataBuilderButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(dataTool!=null) {
          dataTool.getDataFunctionTool().setSelectedPanel(getName());
          dataTool.getDataFunctionTool().setVisible(true);
        }
      }

    });
    // create refreshDataButton
    refreshDataButton = DataTool.createButton(ToolsRes.getString("DataToolTab.Button.Refresh.Text")); //$NON-NLS-1$
    refreshDataButton.setToolTipText(ToolsRes.getString("DataToolTab.Button.Refresh.Tooltip")); //$NON-NLS-1$
    refreshDataButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(dataTool!=null) {
        	// set dataManager name to tab name so reply will be recognized 
        	dataManager.setName(getName());
          dataTool.jobManager.sendReplies(dataManager);
        }
      }
    });
    // create help button
    helpButton = DataTool.createButton(ToolsRes.getString("Tool.Button.Help")); //$NON-NLS-1$
    helpButton.setToolTipText(ToolsRes.getString("Tool.Button.Help.ToolTip")); //$NON-NLS-1$
    helpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(dataTool!=null) {
          dataTool.helpItem.doClick();
        }
      }
    });
    // create propsAndStatsAction
    propsAndStatsAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        boolean statsVis = statsCheckbox.isSelected();
        boolean propsVis = propsCheckbox.isSelected();
        if(statsVis) {
          statsTable.refreshStatistics();
        }
        int statsHeight = statsTable.getPreferredSize().height;
        int propsHeight = propsTable.getPreferredSize().height;
        LookAndFeel currentLF = UIManager.getLookAndFeel();
        int h = (currentLF.getClass().getName().indexOf("Nimbus")>-1) //$NON-NLS-1$
                ? 8
                : 4;
        if(statsVis&&propsVis) {
          Box box = Box.createVerticalBox();
          box.add(statsScroller);
          box.add(propsScroller);
          splitPanes[2].setTopComponent(box);
          splitPanes[2].setDividerLocation(statsHeight+propsHeight+2*h);
        } else if(statsVis) {
          splitPanes[2].setTopComponent(statsScroller);
          splitPanes[2].setDividerLocation(statsHeight+h);
        } else if(propsVis) {
          splitPanes[2].setTopComponent(propsScroller);
          splitPanes[2].setDividerLocation(propsHeight+h);
        } else {
          splitPanes[2].setDividerLocation(0);
        }
      }

    };
    // create stats checkbox
    statsCheckbox = new JCheckBox(ToolsRes.getString("Checkbox.Statistics.Label"), false); //$NON-NLS-1$
    statsCheckbox.setOpaque(false);
    statsCheckbox.setToolTipText(ToolsRes.getString("Checkbox.Statistics.ToolTip")); //$NON-NLS-1$
    statsCheckbox.addActionListener(propsAndStatsAction);
    // create properties checkbox
    propsCheckbox = new JCheckBox(ToolsRes.getString("DataToolTab.Checkbox.Properties.Text"), true); //$NON-NLS-1$
    propsCheckbox.setToolTipText(ToolsRes.getString("DataToolTab.Checkbox.Properties.Tooltip")); //$NON-NLS-1$
    propsCheckbox.setOpaque(false);
    propsCheckbox.addActionListener(propsAndStatsAction);
    // create plotting panel
    plot = new DataToolPlotter(getWorkingData());
    if(getWorkingData()!=null) {
      plot.addDrawable(getWorkingData());
      plot.setTitle(getWorkingData().getName());
    }
    plot.addDrawable(selectionBox);
    MouseInputListener mouseSelector = new MouseInputAdapter() {
      Set<Integer> rowsInside = new HashSet<Integer>();                             // points inside selectionBox
      public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        selectionBox.xstart = p.x;
        selectionBox.ystart = p.y;
        rowsInside.clear();
        if(OSPRuntime.isPopupTrigger(e)) {
          if(selectionBox.isZoomable()) {
            plot.getZoomInItem().setText(ToolsRes.getString("MenuItem.ZoomToBox")); //$NON-NLS-1$
          } else {
            zoomPoint = e.getPoint();
            plot.getZoomInItem().setText(ToolsRes.getString("MenuItem.ZoomIn"));    //$NON-NLS-1$
          }
        } else {
          selectionBox.setSize(0, 0);
        }
        if(!(e.isControlDown()||e.isShiftDown()||OSPRuntime.isPopupTrigger(e))) {
          dataTable.clearSelection();
        }
      }
      public void mouseDragged(MouseEvent e) {
        boolean rightButton = (e.getButton()==MouseEvent.BUTTON3)||(e.isControlDown()&&OSPRuntime.isMac());
        if(rightButton) {
          return;
        }
        Dataset data = dataTable.getWorkingData();
        if(data==null) {
          return;
        }
        Point mouse = e.getPoint();
        selectionBox.visible = true;
        selectionBox.setSize(mouse.x-selectionBox.xstart, mouse.y-selectionBox.ystart);
        double[] xpoints = data.getXPoints();
        double[] ypoints = data.getYPoints();
        for(int i = 0; i<xpoints.length; i++) {
          double xp = plot.xToPix(xpoints[i]);
          double yp = plot.yToPix(ypoints[i]);
          Integer index = dataTable.workingRows.get(new Integer(i));
          int row = index.intValue();
          if(selectionBox.contains(xp, yp)) {
            if(!rowsInside.contains(index)) {     // needs to be added
              rowsInside.add(index);
              dataTable.getSelectionModel().addSelectionInterval(row, row);
              int col = dataTable.getXColumn();
              dataTable.getColumnModel().getSelectionModel().addSelectionInterval(col, col);
              col = dataTable.getYColumn();
              dataTable.getColumnModel().getSelectionModel().addSelectionInterval(col, col);
            }
          } else if(rowsInside.contains(index)) { // needs to be removed
            dataTable.getSelectionModel().removeSelectionInterval(row, row);
            rowsInside.remove(index);
            if(rowsInside.isEmpty()) {
              dataTable.getColumnModel().getSelectionModel().removeSelectionInterval(0, dataTable.getColumnCount()-1);
            }
          }
        }
        dataTable.getSelectedData();
        plot.repaint();
      }
      public void mouseReleased(MouseEvent e) {
        plot.repaint();
      }

    };
    plot.addMouseListener(mouseSelector);
    plot.addMouseMotionListener(mouseSelector);
    // create toolbar
    toolbar = new JToolBar();
    toolbar.setFloatable(false);
    toolbar.setBorder(BorderFactory.createEtchedBorder());
    toolbar.add(propsCheckbox);
    toolbar.add(bottomPaneCheckbox);
    toolbar.add(statsCheckbox);
    toolbar.add(Box.createGlue());
    toolbar.add(newColumnButton);
    toolbar.add(dataBuilderButton);
    toolbar.add(refreshDataButton);
    toolbar.add(helpButton);
    // create curve fitter
    curveFitter = new DatasetCurveFitter(getWorkingData());
    curveFitter.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        if(e.getPropertyName().equals("changed")) { //$NON-NLS-1$
          tabChanged(true);
          return;
        }
        if(e.getPropertyName().equals("drawer")     //$NON-NLS-1$
          &&(bottomPaneCheckbox!=null)&&bottomPaneCheckbox.isSelected()) {
          plot.removeDrawables(FunctionDrawer.class);
          plot.addDrawable((FunctionDrawer) e.getNewValue());
        }
        plot.repaint();
      }

    });
    // create statistics table
    statsTable = new DataToolStatsTable(dataTable);
    statsScroller = new JScrollPane(statsTable) {
      public Dimension getPreferredSize() {
        Dimension dim = statsTable.getPreferredSize();
        return dim;
      }

    };
    // create properties table
    propsTable = new DataToolPropsTable(dataTable);
    propsTable.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        if(e.getPropertyName().equals("display")) { //$NON-NLS-1$
          refreshPlot();
        }
      }

    });
    propsScroller = new JScrollPane(propsTable) {
      public Dimension getPreferredSize() {
        Dimension dim = propsTable.getPreferredSize();
        return dim;
      }

    };
    // assemble components
    add(toolbar, BorderLayout.NORTH);
    add(splitPanes[0], BorderLayout.CENTER);
    splitPanes[0].setLeftComponent(splitPanes[1]);
    splitPanes[0].setRightComponent(splitPanes[2]);
    splitPanes[1].setTopComponent(plot);
    splitPanes[1].setBottomComponent(curveFitter);
    splitPanes[2].setBottomComponent(dataScroller);
    // set up the undo system
    undoManager = new UndoManager();
    undoSupport = new UndoableEditSupport();
    undoSupport.addUndoableEditListener(undoManager);
  }

  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
    boolean changed = tabChanged;
    newColumnButton.setText(ToolsRes.getString("DataToolTab.Button.NewColumn.Text"));               //$NON-NLS-1$
    newColumnButton.setToolTipText(ToolsRes.getString("DataToolTab.Button.NewColumn.Tooltip"));     //$NON-NLS-1$
    dataBuilderButton.setText(ToolsRes.getString("DataToolTab.Button.DataBuilder.Text"));           //$NON-NLS-1$
    dataBuilderButton.setToolTipText(ToolsRes.getString("DataToolTab.Button.DataBuilder.Tooltip")); //$NON-NLS-1$
    dataBuilderButton.setEnabled(owner!=null);
    refreshDataButton.setText(ToolsRes.getString("DataToolTab.Button.Refresh.Text")); 							//$NON-NLS-1$
    refreshDataButton.setToolTipText(ToolsRes.getString("DataToolTab.Button.Refresh.Tooltip")); 		//$NON-NLS-1$
    statsCheckbox.setText(ToolsRes.getString("Checkbox.Statistics.Label"));                         //$NON-NLS-1$
    statsCheckbox.setToolTipText(ToolsRes.getString("Checkbox.Statistics.ToolTip"));                //$NON-NLS-1$
    bottomPaneCheckbox.setText(ToolsRes.getString("Checkbox.Fits.Label"));                          //$NON-NLS-1$
    bottomPaneCheckbox.setToolTipText(ToolsRes.getString("Checkbox.Fits.ToolTip"));                 //$NON-NLS-1$
    propsCheckbox.setText(ToolsRes.getString("DataToolTab.Checkbox.Properties.Text"));              //$NON-NLS-1$
    propsCheckbox.setToolTipText(ToolsRes.getString("DataToolTab.Checkbox.Properties.Tooltip"));    //$NON-NLS-1$
    helpButton.setText(ToolsRes.getString("Tool.Button.Help"));                                     //$NON-NLS-1$
    helpButton.setToolTipText(ToolsRes.getString("Tool.Button.Help.ToolTip"));                      //$NON-NLS-1$
    toolbar.remove(newColumnButton);
    if(userEditable) {
      int n = toolbar.getComponentIndex(helpButton);
      toolbar.add(newColumnButton, n);
      toolbar.validate();
    }
    toolbar.remove(refreshDataButton);
    if(dataTool != null) {
    	Collection<Tool> tools = dataTool.jobManager.getTools(dataManager);
    	for (Tool tool: tools) {
    		if (tool instanceof DataRefreshTool) {
          int n = toolbar.getComponentIndex(helpButton);
          toolbar.add(refreshDataButton, n);
          toolbar.validate();
          break;
    		}
    	}
    }
    curveFitter.refreshGUI();
    statsTable.refreshGUI();
    propsTable.refreshGUI();
    tabChanged = changed;
  }

  /**
   * Initializes this panel.
   */
  protected void init() {
    splitPanes[0].setDividerLocation(0.7);
    splitPanes[1].setDividerLocation(1.0);
    curveFitter.splitPane.setDividerLocation(0.4);
    propsAndStatsAction.actionPerformed(null);
    for(int i = 0; i<dataTable.getColumnCount(); i++) {
      String colName = dataTable.getColumnName(i);
      dataTable.getWorkingData(colName);
    }
  }

  /**
   * Returns the dataset with matching ID in the specified Data object.
   * May return null.
   *
   * @param dataset the Dataset to match
   * @param data the Data object to search
   * @return the matching Dataset, if any
   */
  private Dataset getMatchingID(Dataset dataset, Data data) {
    ArrayList<Dataset> list = data.getDatasets();
    if(list==null) {
      return null;
    }
    for(Iterator<Dataset> it = list.iterator(); it.hasNext(); ) {
      Dataset next = it.next();
      // next is match if same y-column name and ID
      // or same x-column name and ID+1
      int id = next.getID();
      if(next.getYColumnName().equals(dataset.getYColumnName())&&(dataset.getID()==id)) {
        return next;
      }
      if(next.getXColumnName().equals(dataset.getYColumnName())&&(dataset.getID()==id+1)) {
        return next;
      }
    }
    return null;
  }

  /**
   * Returns true if the name and data match an existing column.
   *
   * @param name the name
   * @param data the data array
   * @return true if data is a duplicate
   */
  protected boolean isDuplicateColumn(String name, double[] data) {
    if(name.equals(dataTable.getColumnName(0))) {
      return true;
    }
    Iterator<Dataset> it = dataManager.getDatasets().iterator();
    while(it.hasNext()) {
      Dataset next = it.next();
      double[] y = next.getYPoints();
      if(name.equals(next.getYColumnName())&&isDuplicate(data, next.getYPoints())) {
        // next is duplicate column: add new points if any
        if(data.length>y.length) {
          next.clear();
          next.append(data, data);
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if two data arrays have identical values.
   *
   * @param data0 data array 0
   * @param data1 data array 1
   * @return true if identical
   */
  private boolean isDuplicate(double[] data0, double[] data1) {
    int len = Math.min(data0.length, data1.length);
    for(int i = 0; i<len; i++) {
      if(Double.isNaN(data0[i])&&Double.isNaN(data1[i])) {
        continue;
      }
      if(data0[i]!=data1[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if this tab is owned by the specified Data object.
   *
   * @param data the Data object
   * @return true if data owns this tab
   */
  protected boolean isOwnedBy(Data data) {
    // try to get name of data from getName() method
    String name = null;
    try {
      Method m = data.getClass().getMethod("getName", new Class[0]); //$NON-NLS-1$
      name = (String) m.invoke(data, new Object[0]);
    } catch(Exception ex) {}
    // return true if data name is the name of this tab
    if((name!=null)&&name.equals(getName())) {
      return true;
    }
    return (owner!=null)&&(data==owner);
  }

  /**
   * Refreshes the plot.
   */
  protected void refreshPlot() {
    // get data for curve fitting and plotting
    curveFitter.setData(dataTable.getSelectedData());
    plot.removeDrawables(Dataset.class);
    WorkingDataset workingData = getWorkingData();
    if(workingData!=null) {
      int labelCol = dataTable.convertColumnIndexToView(0);
      String xName = dataTable.getColumnName((labelCol==0)
                                             ? 1
                                             : 0);
      Map<String, WorkingDataset> datasets = dataTable.workingMap;
      for(Iterator<WorkingDataset> it = datasets.values().iterator(); it.hasNext(); ) {
        DataToolTable.WorkingDataset next = it.next();
        next.setXSource(workingData.getXSource());
        String colName = next.getYColumnName();
        if((next==workingData)||colName.equals(xName)) {
          continue;
        }
        if(next.isMarkersVisible()||next.isConnected()) {
          next.clearHighlights();
          if(!next.isMarkersVisible()) {
            next.setMarkerShape(Dataset.NO_MARKER);
          }
          plot.addDrawable(next);
        }
      }
      plot.addDrawable(workingData);
      if((bottomPaneCheckbox!=null)&&bottomPaneCheckbox.isSelected()) {
        // draw curve fit on top of dataset
        plot.removeDrawable(curveFitter.getDrawer());
        plot.addDrawable(curveFitter.getDrawer());
      }
      plot.setTitle(workingData.getName());
      plot.setXLabel(workingData.getColumnName(0));
      plot.setYLabel(workingData.getColumnName(1));
      // construct equation string
      String depVar = TeXParser.removeSubscripting(workingData.getColumnName(1));
      String indepVar = TeXParser.removeSubscripting(workingData.getColumnName(0));
      curveFitter.eqnField.setText(depVar+" = "+ //$NON-NLS-1$
        curveFitter.fit.getExpression(indepVar));
    } else {
      plot.setTitle("");                         //$NON-NLS-1$
      plot.setXLabel("");                        //$NON-NLS-1$
      plot.setYLabel("");                        //$NON-NLS-1$
    }
    if(dataTool!=null) {
      dataTool.refreshTabTitles();
      dataTool.helpLabel.setText((dataManager.getDatasets().size()<2)
                                 ? null
                                 : ToolsRes.getString("DataTool.StatusBar.Help.DragColumns")); //$NON-NLS-1$
    }
    repaint();
  }

  class SelectionBox extends Rectangle implements Drawable {
    boolean visible = true;
    int xstart, ystart;
    int zoomSize = 10;
    Color color = new Color(0, 255, 0, 127);

    public void setSize(int w, int h) {
      int xoffset = Math.min(0, w);
      int yoffset = Math.min(0, h);
      w = Math.abs(w);
      h = Math.abs(h);
      super.setLocation(xstart+xoffset, ystart+yoffset);
      super.setSize(w, h);
    }

    public void draw(DrawingPanel drawingPanel, Graphics g) {
      if(visible) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        g2.draw(this);
      }
    }

    public boolean isZoomable() {
      return((getBounds().width>zoomSize)&&(getBounds().height>zoomSize));
    }

  }

  /**
   * Class to plot datasets. This overrides DrawingPanel zoom and popup methods.
   */
  class DataToolPlotter extends PlottingPanel {
    DataToolPlotter(Dataset dataset) {
      super((dataset==null)? "x" //$NON-NLS-1$
            : dataset.getColumnName(0), 
            (dataset==null)? "y" //$NON-NLS-1$
            : dataset.getColumnName(1), ""); //$NON-NLS-1$
      setAntialiasShapeOn(true);
    }

    /**
     * Gets the zoomIn menu item.
     */
    protected JMenuItem getZoomInItem() {
      return zoomInItem;
    }

    /**
     * Zooms out by a factor of two.
     */
    protected void zoomOut() {
      double dx = xmax-xmin;
      double dy = ymax-ymin;
      setPreferredMinMax(xmin-dx/2, xmax+dx/2, ymin-dy/2, ymax+dy/2);
      invalidateImage(); // validImage = false;
      selectionBox.setSize(0, 0);
      repaint();
    }

    /**
     * Zooms in to the selection box.
     */
    protected void zoomIn() {
      int w = selectionBox.getBounds().width;
      int h = selectionBox.getBounds().height;
      if(selectionBox.isZoomable()) {
        int x = selectionBox.getBounds().x;
        int y = selectionBox.getBounds().y;
        double xmin = pixToX(x);
        double xmax = pixToX(x+w);
        double ymax = pixToY(y);
        double ymin = pixToY(y+h);
        setPreferredMinMax(xmin, xmax, ymin, ymax); // zoom both axes
        invalidateImage();                          // validImage = false;
        selectionBox.setSize(0, 0);
        repaint();
      } else if(zoomPoint!=null) {
        double dx = xmax-xmin;
        double dy = ymax-ymin;
        double xcenter = pixToX(zoomPoint.x);
        double ycenter = pixToY(zoomPoint.y);
        setPreferredMinMax(xcenter-dx/4, xcenter+dx/4, ycenter-dy/4, ycenter+dy/4);
        invalidateImage();                          // validImage = false;
        selectionBox.setSize(0, 0);
        repaint();
      }
    }

    protected void buildPopupmenu() {
      popupmenu.setEnabled(true);
      // create zoom menu items
      zoomInItem = new JMenuItem(ToolsRes.getString("MenuItem.ZoomIn")); //$NON-NLS-1$
      zoomInItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          plot.zoomIn();
        }

      });
      popupmenu.add(zoomInItem);
      zoomOutItem = new JMenuItem(ToolsRes.getString("MenuItem.ZoomOut")); //$NON-NLS-1$
      zoomOutItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          plot.zoomOut();
        }

      });
      popupmenu.add(zoomOutItem);
      JMenuItem zoomFitItem = new JMenuItem(ToolsRes.getString("MenuItem.ZoomToFit")); //$NON-NLS-1$
      zoomFitItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          plot.setAutoscaleX(true);
          plot.setAutoscaleY(true);
          selectionBox.setSize(0, 0);
          refreshPlot();
        }

      });
      popupmenu.add(zoomFitItem);
      scaleItem = new JMenuItem(ToolsRes.getString("MenuItem.Scale")); //$NON-NLS-1$
      scaleItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ScaleInspector plotInspector = new ScaleInspector(DataToolPlotter.this);
          plotInspector.setLocationRelativeTo(DataToolPlotter.this);
          plotInspector.updateDisplay();
          plotInspector.setVisible(true);
        }

      });
      popupmenu.add(scaleItem);
      popupmenu.addSeparator();
      snapshotItem = new JMenuItem(ToolsRes.getString("MenuItem.Snapshot")); //$NON-NLS-1$
      snapshotItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          snapshot();
        }

      });
      popupmenu.add(snapshotItem);
      popupmenu.addSeparator();
      propertiesItem = new JMenuItem(ToolsRes.getString("MenuItem.Inspect")); //$NON-NLS-1$
      propertiesItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          showInspector();
        }

      });
      popupmenu.add(propertiesItem);
    }

  }

  //__________________________ static methods ___________________________

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
      DataToolTab tab = (DataToolTab) obj;
      // save name
      control.setValue("name", tab.getName()); //$NON-NLS-1$
      // save datasets but leave out data functions
      DatasetManager data = new DatasetManager();
      ArrayList<Dataset> functions = new ArrayList<Dataset>();
      for(Iterator<Dataset> it = tab.dataManager.getDatasets().iterator(); it.hasNext(); ) {
        Dataset next = it.next();
        if(next instanceof DataFunction) {
          functions.add(next);
        } else {
          data.addDataset(next);
        }
      }
      control.setValue("data", data); //$NON-NLS-1$
      // save data functions
      if(!functions.isEmpty()) {
        DataFunction[] f = functions.toArray(new DataFunction[0]);
        control.setValue("data_functions", f); //$NON-NLS-1$
      }
      // save fit function panels
      if(tab.curveFitter.fitBuilder!=null) {
        ArrayList<FunctionPanel> fits = new ArrayList<FunctionPanel>(tab.curveFitter.fitBuilder.panels.values());
        control.setValue("fits", fits); //$NON-NLS-1$
      }
      // save selected fit name
      control.setValue("selected_fit", tab.curveFitter.getSelectedFitName());    //$NON-NLS-1$
      // save autofit status
      control.setValue("autofit", tab.curveFitter.autofitCheckBox.isSelected()); //$NON-NLS-1$
      // save fit parameters
      if(!tab.curveFitter.autofitCheckBox.isSelected()) {
        double[] params = new double[tab.curveFitter.paramModel.getRowCount()];
        for(int i = 0; i<params.length; i++) {
          Double val = (Double) tab.curveFitter.paramModel.getValueAt(i, 1);
          params[i] = val.doubleValue();
        }
        control.setValue("fit_parameters", params); //$NON-NLS-1$
      }
      // save fit color
      control.setValue("fit_color", tab.curveFitter.color);                 //$NON-NLS-1$
      // save fit visibility
      control.setValue("fit_visible", tab.bottomPaneCheckbox.isSelected()); //$NON-NLS-1$
      // save props visibility
      control.setValue("props_visible", tab.propsCheckbox.isSelected());    //$NON-NLS-1$
      // save statistics visibility
      control.setValue("stats_visible", tab.statsCheckbox.isSelected());    //$NON-NLS-1$
      // save splitPane locations
      int loc = tab.splitPanes[0].getDividerLocation();
      control.setValue("split_pane", loc); //$NON-NLS-1$
      loc = tab.curveFitter.splitPane.getDividerLocation();
      control.setValue("fit_split_pane", loc); //$NON-NLS-1$
      // save model column order
      int[] cols = tab.dataTable.getModelColumnOrder();
      control.setValue("column_order", cols); //$NON-NLS-1$
      // save hidden markers
      String[] hidden = tab.dataTable.getHiddenMarkers();
      control.setValue("hidden_markers", hidden); //$NON-NLS-1$
      // save column format patterns, if any
      Collection<String> patternColumns = tab.dataTable.getPatternColumns();
      if(!patternColumns.isEmpty()) {
        ArrayList<String[]> patterns = new ArrayList<String[]>();
        for(Iterator<String> it = patternColumns.iterator(); it.hasNext(); ) {
          String colName = it.next();
          String pattern = tab.dataTable.getFormatPattern(colName);
          patterns.add(new String[] {colName, pattern});
        }
        control.setValue("format_patterns", patterns); //$NON-NLS-1$
      }
    }

    public Object createObject(XMLControl control) {
      // load data
      DatasetManager data = (DatasetManager) control.getObject("data"); //$NON-NLS-1$
      return new DataToolTab(data, null);
    }

    public Object loadObject(XMLControl control, Object obj) {
      final DataToolTab tab = (DataToolTab) obj;
      // load tab name
      tab.setName(control.getString("name")); //$NON-NLS-1$
      // load data functions
      Iterator<?> it = control.getPropertyContent().iterator();
      while(it.hasNext()) {
        XMLProperty prop = (XMLProperty) it.next();
        if(prop.getPropertyName().equals("data_functions")) { //$NON-NLS-1$
          XMLControl[] children = prop.getChildControls();
          for(int i = 0; i<children.length; i++) {
            DataFunction f = new DataFunction(tab.dataManager);
            children[i].loadObject(f);
            f.setXColumnVisible(false);
            tab.dataManager.addDataset(f);
          }
          // refresh dataFunctions
          ArrayList<Dataset> datasets = tab.dataManager.getDatasets();
          for(int i = 0; i<datasets.size(); i++) {
            if(datasets.get(i) instanceof DataFunction) {
              ((DataFunction) datasets.get(i)).refreshFunctionData();
            }
          }
          tab.dataTable.refreshTable();
          break;
        }
      }
      // load user fit function panels
      ArrayList<?> fits = (ArrayList<?>) control.getObject("fits"); //$NON-NLS-1$
      if(fits!=null) {
        for(Iterator<?> it2 = fits.iterator(); it2.hasNext(); ) {
          FitFunctionPanel panel = (FitFunctionPanel) it2.next();
          tab.curveFitter.addUserFit(panel);
        }
      }
      // select fit
      String fitName = control.getString("selected_fit"); //$NON-NLS-1$
      tab.curveFitter.fitDropDown.setSelectedItem(fitName);
      // load autofit
      boolean autofit = control.getBoolean("autofit"); //$NON-NLS-1$
      tab.curveFitter.autofitCheckBox.setSelected(autofit);
      // load fit parameters
      double[] params = (double[]) control.getObject("fit_parameters"); //$NON-NLS-1$
      if(params!=null) {
        for(int i = 0; i<params.length; i++) {
          tab.curveFitter.setParameterValue(i, params[i]);
        }
      }
      // load fit color
      Color color = (Color) control.getObject("fit_color"); //$NON-NLS-1$
      tab.curveFitter.setColor(color);
      // load fit visibility
      boolean vis = control.getBoolean("fit_visible"); //$NON-NLS-1$
      tab.bottomPaneCheckbox.setSelected(vis);
      // load props visibility
      vis = control.getBoolean("props_visible"); //$NON-NLS-1$
      tab.propsCheckbox.setSelected(vis);
      // load stats visibility
      vis = control.getBoolean("stats_visible"); //$NON-NLS-1$
      tab.statsCheckbox.setSelected(vis);
      // load splitPane locations
      final int loc = control.getInt("split_pane");           //$NON-NLS-1$
      final int fitLoc = control.getInt("fit_split_pane");    //$NON-NLS-1$
      // load model column order
      int[] cols = (int[]) control.getObject("column_order"); //$NON-NLS-1$
      tab.dataTable.setModelColumnOrder(cols);
      if(cols==null) {                                                    // for legacy files: load working columns
        String[] names = (String[]) control.getObject("working_columns"); //$NON-NLS-1$
        if(names!=null) {
          tab.dataTable.setWorkingColumns(names[0], names[1]);
        }
      }
      // load hidden markers
      String[] hidden = (String[]) control.getObject("hidden_markers"); //$NON-NLS-1$
      tab.dataTable.hideMarkers(hidden);
      // load format patterns
      ArrayList<?> patterns = (ArrayList<?>) control.getObject("format_patterns"); //$NON-NLS-1$
      if(patterns!=null) {
        for(it = patterns.iterator(); it.hasNext(); ) {
          String[] next = (String[]) it.next();
          tab.dataTable.setFormatPattern(next[0], next[1]);
        }
      }
      Runnable runner = new Runnable() {
        public synchronized void run() {
          tab.bottomPaneAction.actionPerformed(null);
          tab.propsAndStatsAction.actionPerformed(null);
          tab.splitPanes[0].setDividerLocation(loc);
          tab.curveFitter.splitPane.setDividerLocation(fitLoc);
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
