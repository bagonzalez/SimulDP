/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.beans.*;
import java.rmi.*;
import java.util.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import javax.swing.*;
import javax.swing.event.*;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.tools.DataToolTable.TableEdit;

/**
 * This provides a GUI for analyzing OSP Data objects.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class DataTool extends OSPFrame implements Tool, PropertyChangeListener {
  // static fields
  public static boolean loadClass = false;
  protected static JFileChooser chooser;
  protected static Dimension dim = new Dimension(720, 500);
  protected static final int defaultButtonHeight = 28;
  protected static int buttonHeight = defaultButtonHeight;
  protected static String[] delimiters = new String[] {" ", "\t",                    //$NON-NLS-1$ //$NON-NLS-2$
    ",", ";"};                                                                       //$NON-NLS-1$ //$NON-NLS-2$
  // instance fields
  protected JTabbedPane tabbedPane;
  protected boolean useChooser = true;
  protected JPanel contentPane = new JPanel(new BorderLayout());
  protected PropertyChangeSupport support;
  protected XMLControl control = new XMLControlElement();
  protected JobManager jobManager = new JobManager(this);
  protected DatasetManager addableData = null;
  protected JMenuBar emptyMenubar;
  protected JMenu emptyFileMenu;
  protected JMenuItem emptyNewTabItem;
  protected JMenuItem emptyOpenItem;
  protected JMenuItem emptyExitItem;
  protected JMenu emptyEditMenu;
  protected JMenuItem emptyPasteItem;
  protected JMenuBar menubar;
  protected JMenu fileMenu;
  protected JMenuItem newTabItem;
  protected JMenuItem openItem;
  protected JMenuItem exportItem;
  protected JMenuItem saveItem;
  protected JMenuItem saveAsItem;
  protected JMenuItem closeItem;
  protected JMenuItem closeAllItem;
  protected JMenuItem printItem;
  protected JMenuItem exitItem;
  protected JMenu editMenu;
  protected JMenuItem undoItem;
  protected JMenuItem redoItem;
  protected JMenu copyMenu;
  protected JMenuItem copyImageItem;
  protected JMenuItem copyTabItem;
  //  protected JMenuItem copyColumnsItem;
  protected JMenuItem copyDataItem;
  protected JMenu pasteMenu;
  protected JMenuItem pasteNewTabItem;
  protected JMenuItem pasteColumnsItem;
  protected JMenu displayMenu;
  protected JMenu languageMenu;
  protected JMenuItem[] languageItems;
  protected JMenu fontSizeMenu;
  protected JMenuItem defaultFontSizeItem;
  protected JMenu helpMenu;
  protected JMenuItem helpItem;
  protected JMenuItem logItem;
  protected JMenuItem aboutItem;
  protected FunctionTool dataFunctionTool;
  protected JLabel helpLabel;
  protected TextFrame helpFrame;
  protected String helpPath = "data_tool_help.html";                                 //$NON-NLS-1$
  protected String helpBase = "http://www.opensourcephysics.org/online_help/tools/"; //$NON-NLS-1$
  protected int fontLevel = FontSizer.getLevel();
  protected boolean exitOnClose = false;
  protected boolean userEditable = false;
  protected boolean saveChangesOnClose = false;

  /**
   * A shared data tool.
   */
  final static DataTool DATATOOL = new DataTool();

  /**
   * Gets the shared DataTool.
   *
   * @return the shared DataTool
   */
  public static DataTool getTool() {
    return DATATOOL;
  }

  /**
   * Main entry point when used as application.
   *
   * @param args args[0] may be a data or xml file name
   */
  public static void main(String[] args) {
    DATATOOL.userEditable = true;
    DATATOOL.exitOnClose = true;
    DATATOOL.saveChangesOnClose = true;
    if((args!=null)&&(args.length>0)&&(args[0]!=null)) {
      DATATOOL.open(args[0]);
    } else {
      DATATOOL.addTab((Data) null);
    }
    DATATOOL.setVisible(true);
  }

  /**
   * Constructs a blank DataTool.
   */
  public DataTool() {
    this(ToolsRes.getString("DataTool.Frame.Title"), "DataTool"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Constructs a DataTool and opens the specified xml file.
   *
   * @param fileName the name of the xml file
   */
  public DataTool(String fileName) {
    this();
    open(fileName);
  }

  /**
   * Constructs a DataTool and loads data from an xml control.
   *
   * @param control the xml control
   */
  public DataTool(XMLControl control) {
    this();
    addTab(control);
  }

  /**
   * Constructs a DataTool and loads the specified data object.
   *
   * @param data the data
   */
  public DataTool(Data data) {
    this();
    addTab(data);
  }

  /**
   * Constructs a DataTool and loads a data object into a named tab.
   *
   * @param data the data
   * @param name the tab name
   */
  public DataTool(Data data, String name) {
    this();
    addTab(data, name);
  }

  /**
   * Sets the userEditable flag. If true, all tabs are editable by default.
   * Note: even when false, user-pasted tabs are still editable.
   *
   * @param editable true to enable user editing of all tabs
   */
  public void setUserEditable(boolean editable) {
    userEditable = editable;
	  int n = tabbedPane.getTabCount();
	  for(int i = 0; i<n; i++) {
	    DataToolTab tab = (DataToolTab) tabbedPane.getComponentAt(i);
	    tab.userEditable = editable;
	    tab.refreshGUI();
	  }
  }

  /**
   * Sets the saveChangesOnClose flag.
   *
   * @param save true to save changes when exiting
   */
  public void setSaveChangesOnClose(boolean save) {
    saveChangesOnClose = save&&!OSPRuntime.appletMode;
  }

  /**
   * Adds a tab and loads data from an xml control.
   *
   * @param control the xml control
   * @return the newly added tab, or null if failed
   */
  public DataToolTab addTab(XMLControl control) {
    // if control is for DataToolTab class, load tab from control and add it
    if(DataToolTab.class.isAssignableFrom(control.getObjectClass())) {
      DataToolTab tab = (DataToolTab) control.loadObject(null);
      tab.dataTool = this;
      addTab(tab);
  		tab.userEditable = true;
  		tab.refreshGUI();
      return tab;
    }
    // otherwise load data from control into a null tab (new one created)
    return loadData(null, control, useChooser);
  }

  /**
   * Adds a tab for the specified Data object. The tab name will be
   * that of the Data object if it defines a getName() method.
   *
   * @param data the Data
   * @return the newly added tab
   */
  public DataToolTab addTab(Data data) {
    // try to get name of data from getName() method
    String name = ""; //$NON-NLS-1$
    try {
      Method m = data.getClass().getMethod("getName", new Class[0]); //$NON-NLS-1$
      name = (String) m.invoke(data, new Object[0]);
    } catch(Exception ex) {

    /** empty block */
    }
    return addTab(data, name);
  }

  /**
   * Adds a tab for the specified Data object and proposes a name
   * for the tab. The name will be modified if not unique.
   *
   * @param data the Data
   * @param name a proposed tab name
   * @return the newly added tab
   */
  public DataToolTab addTab(Data data, String name) {
    DataToolTab tab = new DataToolTab(data, this);
    tab.setName(name);
    addTab(tab);
    return tab;
  }

  /**
   * Removes the tab at the specified index.
   *
   * @param index the tab number
   * @return true if removed
   */
  public boolean removeTab(int index) {
    if((index>=0)&&(index<tabbedPane.getTabCount())) {
      if(!saveChangesAt(index)) {
        return false;
      }
      String title = tabbedPane.getTitleAt(index);
      OSPLog.finer("removing tab "+title); //$NON-NLS-1$
      tabbedPane.removeTabAt(index);
      refreshTabTitles();
      refreshMenubar();
      refreshFunctionTool();
      return true;
    }
    return false;
  }

  /**
   * Updates the data.
   *
   * @param data the Data
   */
  public void update(Data data) {
    DataToolTab tab = getTab(data); // tab may be null
    if(tab!=null) {
      tab.reloadData(data);
    }
  }

  /**
   * Returns the tab containing the specified Data object. May return null.
   *
   * @param data the Dataset
   * @return the tab
   */
  public DataToolTab getTab(Data data) {
    int i = getTabIndex(data);
    return (i>-1)
           ? (DataToolTab) tabbedPane.getComponentAt(i)
           : null;
  }

  /**
   * Returns the tab at the specified index. May return null.
   *
   * @param index the tab index
   * @return the tab
   */
  public DataToolTab getTab(int index) {
    return ((index>-1)&&(index<tabbedPane.getTabCount()))
           ? (DataToolTab) tabbedPane.getComponentAt(index)
           : null;
  }

  /**
   * Returns the tab count.
   *
   * @return the number of tabs
   */
  public int getTabCount() {
    return tabbedPane.getTabCount();
  }

  /**
   * Opens an xml file specified by name.
   *
   * @param fileName the file name
   * @return the file name, if successfully opened (datasets loaded)
   */
  public String open(String fileName) {
    OSPLog.fine("opening "+fileName); //$NON-NLS-1$
    Resource res = ResourceLoader.getResource(fileName);
    if(res!=null) {
      Reader in = res.openReader();
      String firstLine = readFirstLine(in);
      // if xml, read the file into an XML control and add tab
      if(firstLine.startsWith("<?xml")) { //$NON-NLS-1$
        XMLControlElement control = new XMLControlElement(fileName);
        DataToolTab tab = addTab(control);
        if(tab!=null) {
          refreshFunctionTool();
          tab.fileName = fileName;
          tab.tabChanged(false);
          return fileName;
        }
      }
      // if not xml, attempt to import data and add tab
      else if(res.getString()!=null) {
        Data data = importCharDelimitedData(res.getString(), fileName);
        if(data!=null) {
          DataToolTab tab = addTab(data);
          if(tab!=null) {
            refreshFunctionTool();
            tab.fileName = fileName;
            tab.tabChanged(false);
            return fileName;
          }
        }
      }
    }
    OSPLog.finest("no data found"); //$NON-NLS-1$
    return null;
  }

  /**
   * Sends a job to this tool and specifies a tool to reply to.
   *
   * @param job the Job
   * @param replyTo the tool to notify when the job is complete (may be null)
   * @throws RemoteException
   */
  public void send(Job job, Tool replyTo) throws RemoteException {
    XMLControlElement control = new XMLControlElement(job.getXML());
    if(control.failedToRead()||(control.getObjectClass()==Object.class)) {
      return;
    }
    // log the job in
    jobManager.log(job, replyTo);
    // if control is for a Data object, load it into new or existing tab
    if(Data.class.isAssignableFrom(control.getObjectClass())) {
      Data data = (Data) control.loadObject(null, true, true);
      DataToolTab tab = getTab(data); // tab may be null
      tab = loadData(data, tab); // will update tab or make new tab if null
      refreshTabTitles();
      jobManager.associate(job, tab.dataManager);
      tab.refreshGUI();
    } else {
      addTab(control);                // adds Data objects found in XMLControl
    }
  }

  /**
   * Sets the useChooser flag.
   *
   * @param useChooser true to load datasets with a chooser
   */
  public void setUseChooser(boolean useChooser) {
    this.useChooser = useChooser;
  }

  /**
   * Gets the useChooser flag.
   *
   * @return true if loading datasets with a chooser
   */
  public boolean isUseChooser() {
    return useChooser;
  }

  /**
   * Listens for property changes "function" and "visible"
   *
   * @param e the event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if(name.equals("function")) {                     //$NON-NLS-1$
      DataToolTab tab = getSelectedTab();
      if(tab!=null) {
        tab.tabChanged(true);
        tab.dataTable.refreshTable();
        tab.statsTable.refreshStatistics();
        if(e.getNewValue() instanceof DataFunction) { // new function has been created
          String funcName = e.getNewValue().toString();
          tab.dataTable.getWorkingData(funcName);
        }
        if(e.getOldValue() instanceof DataFunction) { // function has been deleted
          String funcName = e.getOldValue().toString();
          tab.dataTable.removeWorkingData(funcName);
        }
        if(e.getNewValue() instanceof String) {
          String funcName = e.getNewValue().toString();
          if(e.getOldValue() instanceof String) {     // function name has changed
            String prevName = e.getOldValue().toString();
            tab.columnNameChanged(prevName, funcName);
          } else {
            tab.dataTable.getWorkingData(funcName);
          }
        }
        tab.refreshPlot();
      }
    }
  }

  /**
   * Determines if an array contains any duplicate values.
   *
   * @param values the array
   * @return true if at least one duplicate is found
   */
  public static boolean containsDuplicateValues(double[] values) {
    for(int i = 0; i<values.length; i++) {
      int n = getIndex(values[i], values, i);
      if(n>-1) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the array index at which the specified value is found.
   *
   * @param value the value to find
   * @param array the array to search
   * @param ignoreIndex an array index to ignore
   * @return the index, or -1 if not found
   */
  public static int getIndex(double value, double[] array, int ignoreIndex) {
    for(int i = 0; i<array.length; i++) {
      if(i==ignoreIndex) {
        continue;
      }
      if(array[i]==value) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns an array of row numbers.
   *
   * @param rowCount length of the array
   * @return the array
   */
  public static double[] getRowArray(int rowCount) {
    double[] rows = new double[rowCount];
    for(int i = 0; i<rowCount; i++) {
      rows[i] = i;
    }
    return rows;
  }

  /**
   * Parses a String into tokens separated by a specified delimiter.
   * A token may be "".
   *
   * @param text the text to parse
   * @param delimiter the delimiter
   * @return an array of String tokens
   */
  public static String[] parseStrings(String text, String delimiter) {
    Collection<String> tokens = new ArrayList<String>();
    if(text!=null) {
      // get the first token
      String next = text;
      int i = text.indexOf(delimiter);
      if(i==-1) {   // no delimiter
        tokens.add(stripQuotes(next));
        text = null;
      } else {
        next = text.substring(0, i);
        text = text.substring(i+1);
      }
      // iterate thru the tokens and add to token list
      while(text!=null) {
        tokens.add(stripQuotes(next));
        i = text.indexOf(delimiter);
        if(i==-1) { // no delimiter
          next = text;
          tokens.add(stripQuotes(next));
          text = null;
        } else {
          next = text.substring(0, i).trim();
          text = text.substring(i+1);
        }
      }
    }
    return tokens.toArray(new String[0]);
  }

  /**
   * Strips quotation marks around a string.
   *
   * @param text the text to strip
   * @return the stripped string
   */
  public static String stripQuotes(String text) {
    if(text.startsWith("\"")) {       //$NON-NLS-1$
      String stripped = text.substring(1);
      int n = stripped.indexOf("\""); //$NON-NLS-1$
      if(n==stripped.length()-1) {
        return stripped.substring(0, n);
      }
    }
    return text;
  }

  /**
   * Parses a String into doubles separated by a specified delimiter.
   * Unparsable strings are set to Double.NaN.
   *
   * @param text the text to parse
   * @param delimiter the delimiter
   * @return an array of doubles
   */
  public static double[] parseDoubles(String text, String delimiter) {
    String[] strings = parseStrings(text, delimiter);
    return parseDoubles(strings);
  }

  /**
   * Parses a String array into doubles.
   * Unparsable strings are set to Double.NaN.
   *
   * @param strings the String array to parse
   * @return an array of doubles
   */
  public static double[] parseDoubles(String[] strings) {
    double[] doubles = new double[strings.length];
    for(int i = 0; i<strings.length; i++) {
      if(strings[i].indexOf("\t")>-1) { //$NON-NLS-1$
        doubles[i] = Double.NaN;
      } else {
        try {
          doubles[i] = Double.parseDouble(strings[i]);
        } catch(NumberFormatException e) {
          doubles[i] = Double.NaN;
        }
      }
    }
    return doubles;
  }

  /**
   * Parses a String into tokens separated by specified row and column delimiters.
   *
   * @param text the text to parse
   * @param rowDelimiter the column delimiter
   * @param colDelimiter the column delimiter
   * @return a 2D array of String tokens
   */
  public static String[][] parseStrings(String text, String rowDelimiter, String colDelimiter) {
    String[] rows = parseStrings(text, rowDelimiter);
    String[][] tokens = new String[rows.length][0];
    for(int i = 0; i<rows.length; i++) {
      tokens[i] = parseStrings(rows[i], colDelimiter);
    }
    return tokens;
  }

  /**
   * Parses a String into doubles separated by specified row and column delimiters.
   *
   * @param text the text to parse
   * @param rowDelimiter the column delimiter
   * @param colDelimiter the column delimiter
   * @return a 2D array of doubles
   */
  public static double[][] parseDoubles(String text, String rowDelimiter, String colDelimiter) {
    String[][] strings = parseStrings(text, rowDelimiter, colDelimiter);
    double[][] doubles = new double[strings.length][0];
    for(int i = 0; i<strings.length; i++) {
      double[] row = new double[strings[i].length];
      for(int j = 0; j<row.length; j++) {
        try {
          row[j] = Double.parseDouble(strings[i][j]);
        } catch(NumberFormatException e) {
          row[j] = Double.NaN;
        }
      }
      doubles[i] = row;
    }
    return doubles;
  }

  //______________________________ protected methods ________________________
  protected String readFirstLine(Reader in) {
    BufferedReader input = null;
    if(in instanceof BufferedReader) {
      input = (BufferedReader) in;
    } else {
      input = new BufferedReader(in);
    }
    String openingLine;
    try {
      openingLine = input.readLine();
      while((openingLine==null)||openingLine.equals("")) { //$NON-NLS-1$
        openingLine = input.readLine();
      }
    } catch(IOException e) {
      e.printStackTrace();
      return null;
    }
    try {
      input.close();
    } catch(IOException ex) {
      ex.printStackTrace();
    }
    return openingLine;
  }

  /**
   * Imports character-delimited data.
   *
   * @param dataString the data
   * @param fileName name of file being imported (may be null)
   * @return DatasetManager with imported data, or null if none found
   */
  protected DatasetManager importCharDelimitedData(String dataString, String fileName) {
    BufferedReader input = new BufferedReader(new StringReader(dataString));
    String gnuPlotComment = "#"; //$NON-NLS-1$
    try {
      String textLine = input.readLine();
      for(int i = 0; i<delimiters.length; i++) {
        ArrayList<double[]> rows = new ArrayList<double[]>();
        int columns = Integer.MAX_VALUE;
        String[] columnNames = null;
        String title = null;
        int lineCount = 0;
        while(textLine!=null) {                                                       // process each line of text
          // look for gnuPlot-commented name and/or columnNames
          if(textLine.startsWith(gnuPlotComment)) {
            int k = textLine.indexOf("name:");                                        //$NON-NLS-1$
            if(k>-1) {
              title = textLine.substring(k+5).trim();
            }
            k = textLine.indexOf("columnNames:");                                     //$NON-NLS-1$
            if(k>-1) {
              textLine = textLine.substring(k+12).trim();
            } else {
              textLine = input.readLine();
              continue;
            }
          }
          String[] strings = parseStrings(textLine, delimiters[i]);
          double[] rowData = parseDoubles(strings);
          // set null title if String[] length > 0, all entries
          // are NaN and only one entry is not ""
          if(rows.isEmpty()&&(strings.length>0)&&(title==null)) {
            String s = "";                                                            //$NON-NLS-1$
            for(int k = 0; k<strings.length; k++) {
              if(Double.isNaN(rowData[k])&&!strings[k].equals("")) {                  //$NON-NLS-1$
                if(s.equals("")) { //$NON-NLS-1$
                  s = strings[k]; 
                } else {
                  s = "";                                                             //$NON-NLS-1$
                  break;
                }
              }
            }
            if(!s.equals("")) {                                                       //$NON-NLS-1$
              title = s;
              textLine = input.readLine();
              continue;
            }
          }
          // set null column names if String[] length > 0,
          // all entries are NaN and none is ""
          if(rows.isEmpty()&&(strings.length>0)&&(columnNames==null)) {
            boolean valid = true;
            for(int k = 0; k<strings.length; k++) {
              if(!Double.isNaN(rowData[k])||strings[k].equals("")) {                  //$NON-NLS-1$
                valid = false;
                break;
              }
            }
            if(valid) {
              columnNames = strings;
              textLine = input.readLine();
              continue;
            }
          }
          // add double[] of length 1 or longer to rows
          if(strings.length>0) {
            lineCount++;
            boolean validData = true;
            boolean emptyData = true;
            for(int k = 0; k<strings.length; k++) {
              // invalid if any NaN entries other than ""
              if(Double.isNaN(rowData[k])&&!strings[k].equals("")) {                  //$NON-NLS-1$
                validData = false;
              }
              // look for empty row--every entry is ""
              if(!strings[k].equals("")) {                                            //$NON-NLS-1$
                emptyData = false;
              }
            }
            if(rows.isEmpty()&&emptyData) {
              validData = false;
            }
            // add valid data, but ignore blank lines
            // that may precede real data
            if(validData) {
              rows.add(rowData);
              columns = Math.min(rowData.length, columns);
            }
          }
          // abort processing if no data found in first several lines
          if(rows.isEmpty()&&(lineCount>10)) {
            break;
          }
          textLine = input.readLine();
        }
        // create datasets if data found
        if(!rows.isEmpty()&&(columns>0)) {
          // first reassemble data from rows into columns
          double[][] dataArray = new double[columns][rows.size()];
          for(int row = 0; row<rows.size(); row++) {
            double[] next = rows.get(row);
            for(int j = 0; j<columns; j++) {
              dataArray[j][row] = next[j];
            }
          }
          // then append data to datasets
          DatasetManager data = new DatasetManager();
          data.setName((title==null)
                       ? XML.getName(fileName)
                       : title);
          double[] rowColumn = getRowArray(rows.size());
          for(int j = 0; j<columns; j++) {
            Dataset dataset = data.getDataset(j);
            String yColName = ((columnNames!=null)&&(columnNames.length>=j))
                              ? columnNames[j]
                              : ((j==0)&&(title!=null))
                                ? title
                                : "?";                                                //$NON-NLS-1$
            dataset.setXYColumnNames(DataTable.rowName, yColName);
            dataset.append(rowColumn, dataArray[j]);
          }
          OSPLog.finest("imported data found using delimiter \""+delimiters[i]+"\""); //$NON-NLS-1$ //$NON-NLS-2$
          return data;
        }
        // close the reader and open a new one
        input.close();
        input = new BufferedReader(new StringReader(dataString));
        textLine = input.readLine();
      }
    } catch(IOException e) {
      e.printStackTrace();
    }
    try {
      input.close();
    } catch(IOException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * Gets a unique name.
   *
   * @param proposed the proposed name
   * @return the unique name
   */
  protected String getUniqueName(String proposed) {
    if((proposed==null)||proposed.equals("")) {                 //$NON-NLS-1$
      proposed = ToolsRes.getString("DataToolTab.DefaultName"); //$NON-NLS-1$
    }
    // construct a unique name from proposed by adding trailing digit if nec
    ArrayList<String> taken = new ArrayList<String>();
    for(int i = 0; i<getTabCount(); i++) {
      DataToolTab tab = getTab(i);
      taken.add(tab.getName());
    }
    if(!taken.contains(proposed)) {
      return proposed;
    }
    // strip existing numbered subscript if any
    int n = proposed.lastIndexOf("_"); //$NON-NLS-1$
    if(n>-1) {
      String end = proposed.substring(n+1);
      try {
        Integer.parseInt(end);
        proposed = proposed.substring(0, n);
      } catch(Exception ex) {

      /** empty block */
      }
    }
    proposed += "_"; //$NON-NLS-1$
    int i = 1;
    String name = proposed+i;
    while(taken.contains(name)) {
      i++;
      name = proposed+i;
    }
    return name;
  }

  /**
   * Loads data from an xml control into a specified tab. If no tab is
   * specified, then a new one is created.
   *
   * @param tab the tab to load (may be null)
   * @param control the xml control describing the data
   * @param useChooser true to present data choices to user
   *
   * @return the loaded tab
   */
  protected DataToolTab loadData(DataToolTab tab, XMLControl control, boolean useChooser) {
    java.util.List<XMLProperty> xmlControls;
    // first populate the list with Data XMLControls
    if(useChooser) {
      // get user-selected data objects from an xml tree chooser
      XMLTreeChooser chooser = new XMLTreeChooser(ToolsRes.getString("Chooser.Title"), //$NON-NLS-1$
        ToolsRes.getString("Chooser.Label"), this); //$NON-NLS-1$
      xmlControls = chooser.choose(control, Data.class);
    } else {
      // find all Data objects in the control
      XMLTree tree = new XMLTree(control);
      tree.setHighlightedClass(Data.class);
      tree.selectHighlightedProperties();
      xmlControls = tree.getSelectedProperties();
      if(xmlControls.isEmpty()) {
        JOptionPane.showMessageDialog(null, ToolsRes.getString("Dialog.NoDatasets.Message")); //$NON-NLS-1$
      }
    }
    // load the list of Data XMLControls
    if(!xmlControls.isEmpty()) {
      Iterator<XMLProperty> it = xmlControls.iterator();
      while(it.hasNext()) {
        XMLControl next = (XMLControl) it.next();
        Data data = null;
        if(next instanceof XMLControlElement) {
          XMLControlElement element = (XMLControlElement) next;
          data = (Data) element.loadObject(null, true, true);
        } else {
          data = (Data) next.loadObject(null);
        }
        if(data==null) {
          continue;
        }
        tab = loadData(data, tab);
      }
    }
    return tab;
  }

  /**
   * Loads data into a specified tab. If tab is null, a new one is created.
   *
   * @param tab the tab to load (may be null)
   *
   * @return the loaded tab
   */
  protected DataToolTab loadData(Data data, DataToolTab tab) {
    // try to get name of data from getName() method
    String name = ""; //$NON-NLS-1$
    try {
      Method m = data.getClass().getMethod("getName", new Class[0]); //$NON-NLS-1$
      name = (String) m.invoke(data, new Object[0]);
    } catch(Exception ex) {}
    // if tab is null, create and add new one, otherwise reload
    if(tab==null) tab = addTab(data, name);
    else tab.reloadData(data);
    return tab;
  }

  /**
   * Adds a tab. The tab should be named before calling this method.
   *
   * @param tab a DataToolTab
   */
  protected void addTab(final DataToolTab tab) {
    // remove single empty tab, if any
    if(getTabCount()==1) {
      DataToolTab prev = getTab(0);
      if(prev.owner==null) {
        prev.tabChanged(false);
        removeTab(0);
      }
    }
    // assign a unique name (also traps for null name)
    tab.setName(getUniqueName(tab.getName()));
    tab.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        if((tab.bottomPaneCheckbox!=null)&&!tab.bottomPaneCheckbox.isSelected()) {
          tab.splitPanes[1].setDividerLocation(1.0);
        }
      }

    });
    OSPLog.finer("adding tab "+tab.getName()); //$NON-NLS-1$
    tabbedPane.addTab("", tab);                //$NON-NLS-1$
    tabbedPane.setSelectedComponent(tab);
    validate();
    tab.init();
    tab.refreshPlot();
    tab.userEditable = this.userEditable;
    tab.refreshGUI();
    refreshTabTitles();
    refreshMenubar();
    setFontLevel(fontLevel);
  }

  /**
   * Offers to save changes to the tab at the specified index.
   *
   * @param i the tab index
   * @return true unless canceled by the user
   */
  protected boolean saveChangesAt(int i) {
    if(OSPRuntime.appletMode) {
      return true;
    }
    DataToolTab tab = getTab(i);
    if(!tab.tabChanged) {
      return true;
    }
    String name = tab.getName();
    if(ToolsRes.getString("DataToolTab.DefaultName").equals(name) //$NON-NLS-1$
      &&(tab.owner==null)) {
      return true;
    }
    int selected = JOptionPane.showConfirmDialog(this, ToolsRes.getString("DataTool.Dialog.SaveChanges.Message1")+ //$NON-NLS-1$
      " \""+name+"\" "+                                             //$NON-NLS-1$ //$NON-NLS-2$
        ToolsRes.getString("DataTool.Dialog.SaveChanges.Message2"), //$NON-NLS-1$
        ToolsRes.getString("DataTool.Dialog.SaveChanges.Title"),    //$NON-NLS-1$
        JOptionPane.YES_NO_CANCEL_OPTION);
    if(selected==JOptionPane.CANCEL_OPTION) {
      return false;
    }
    if(selected==JOptionPane.YES_OPTION) {
      // save root and all owned nodes
      if(save(tab, tab.fileName)==null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the currently selected DataToolTab, if any.
   *
   * @return the selected tab
   */
  protected DataToolTab getSelectedTab() {
    return(DataToolTab) tabbedPane.getSelectedComponent();
  }

  /**
   * Selects a DataToolTab.
   *
   * @param tab the tab to select
   */
  protected void setSelectedTab(DataToolTab tab) {
    tabbedPane.setSelectedComponent(tab);
  }

  /**
   * Sets the font level.
   *
   * @param level the level
   */
  public void setFontLevel(int level) {
    fontLevel = Math.max(level, 0);
    super.setFontLevel(fontLevel);
    double factor = FontSizer.getFactor(fontLevel);
    buttonHeight = (int) (factor*defaultButtonHeight);
    if(tabbedPane!=null) {
      for(int i = 0; i<getTabCount(); i++) {
        getTab(i).setFontLevel(fontLevel);
      }
    }
    if(dataFunctionTool!=null) {
      dataFunctionTool.setFontLevel(fontLevel);
    }
  }

  /**
   * Writes text to a file selected with a chooser.
   *
   * @param text the text
   * @return the path of the saved document or null if failed
   */
  public String write(String text) {
    String tabName = getSelectedTab().getName();
    OSPRuntime.getChooser().setSelectedFile(new File(tabName+".txt")); //$NON-NLS-1$
    int result = OSPRuntime.getChooser().showSaveDialog(this);
    if(result==JFileChooser.APPROVE_OPTION) {
      OSPRuntime.chooserDir = OSPRuntime.getChooser().getCurrentDirectory().toString();
      String fileName = OSPRuntime.getChooser().getSelectedFile().getAbsolutePath();
      fileName = XML.getRelativePath(fileName);
      return write(text, fileName);
    }
    return null;
  }

  /**
   * Writes text to a file with the specified name.
   *
   * @param text the text
   * @param fileName the file name
   * @return the path of the saved document or null if failed
   */
  public String write(String text, String fileName) {
    int n = fileName.lastIndexOf("/"); //$NON-NLS-1$
    if(n<0) {
      n = fileName.lastIndexOf("\\"); //$NON-NLS-1$
    }
    if(n>0) {
      String dir = fileName.substring(0, n+1);
      File file = new File(dir);
      if(!file.exists()&&!file.mkdir()) {
        return null;
      }
    }
    try {
      File file = new File(fileName);
      // check to see if file already exists
      if(file.exists()) {
        if(!file.canWrite()) {
          JOptionPane.showMessageDialog(null, "File is read-only.");                    //$NON-NLS-1$
          return null;
        }
        int selected = JOptionPane.showConfirmDialog(null,
          ToolsRes.getString("Tool.Dialog.ReplaceFile.Message")+" "+file.getName()+"?", //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
          ToolsRes.getString("Tool.Dialog.ReplaceFile.Title"),                      //$NON-NLS-1$
          JOptionPane.YES_NO_CANCEL_OPTION);
        if(selected!=JOptionPane.YES_OPTION) {
          return null;
        }
      }
      FileOutputStream stream = new FileOutputStream(file);
      java.nio.charset.Charset charset = java.nio.charset.Charset.forName("UTF-8"); //$NON-NLS-1$
      write(text, new OutputStreamWriter(stream, charset));
      if(file.exists()) {
        return file.getAbsolutePath();
      }
    } catch(IOException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * Writes text to a Writer.
   *
   * @param text the text
   * @param out the Writer
   */
  public void write(String text, Writer out) {
    try {
      Writer output = new BufferedWriter(out);
      output.write(text);
      output.flush();
      output.close();
    } catch(IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Opens an xml file selected with a chooser.
   *
   * @return the name of the opened file
   */
  protected String open() {
    int result = OSPRuntime.getChooser().showOpenDialog(null);
    if(result==JFileChooser.APPROVE_OPTION) {
      OSPRuntime.chooserDir = OSPRuntime.getChooser().getCurrentDirectory().toString();
      String fileName = OSPRuntime.getChooser().getSelectedFile().getAbsolutePath();
      fileName = XML.getRelativePath(fileName);
      return open(fileName);
    }
    return null;
  }

  /**
   * Saves the current tab to the specified file.
   *
   * @param fileName the file name
   * @return the name of the saved file, or null if not saved
   */
  protected String save(String fileName) {
    return save(getSelectedTab(), fileName);
  }

  /**
   * Saves a tab to the specified file.
   *
   * @param tab the tab
   * @param fileName the file name
   * @return the name of the saved file, or null if not saved
   */
  protected String save(DataToolTab tab, String fileName) {
    if((fileName==null)||fileName.equals("")) { //$NON-NLS-1$
      return saveAs();
    }
    XMLControl control = new XMLControlElement(tab);
    if(control.write(fileName)==null) {
      return null;
    }
    tab.fileName = fileName;
    tab.tabChanged(false);
    return fileName;
  }

  /**
   * Saves the current tab to a file selected with a chooser.
   *
   * @return the name of the saved file, or null if not saved
   */
  protected String saveAs() {
    int result = OSPRuntime.getChooser().showSaveDialog(this);
    if(result==JFileChooser.APPROVE_OPTION) {
      OSPRuntime.chooserDir = OSPRuntime.getChooser().getCurrentDirectory().toString();
      File file = OSPRuntime.getChooser().getSelectedFile();
      // check to see if file already exists
      if(file.exists()) {
        int selected = JOptionPane.showConfirmDialog(null,
          ToolsRes.getString("Tool.Dialog.ReplaceFile.Message")+" "+file.getName()+"?", //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
          ToolsRes.getString("Tool.Dialog.ReplaceFile.Title"), //$NON-NLS-1$
          JOptionPane.YES_NO_CANCEL_OPTION);
        if(selected!=JOptionPane.YES_OPTION) {
          return null;
        }
      }
      String fileName = file.getAbsolutePath();
      if((fileName==null)||fileName.trim().equals("")) {       //$NON-NLS-1$
        return null;
      }
      // add .xml extension if none but don't require it
      if(XML.getExtension(fileName)==null) {
        fileName += ".xml";                                    //$NON-NLS-1$
      }
      return save(XML.getRelativePath(fileName));
    }
    return null;
  }

  /**
   * Returns the index of the tab containing the specified Data object.
   *
   * @param data the Dataset
   * @return the name of the opened file
   */
  protected int getTabIndex(Data data) {
    for(int i = 0; i<tabbedPane.getTabCount(); i++) {
      DataToolTab tab = (DataToolTab) tabbedPane.getComponentAt(i);
      if(tab.isOwnedBy(data)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Constructs a DataTool with title and name.
   */
  protected DataTool(String title, String name) {
    super(title);
    setName(name);
    createGUI();
    Toolbox.addTool(name, this);
    ToolsRes.addPropertyChangeListener("locale", new PropertyChangeListener() { //$NON-NLS-1$
      public void propertyChange(PropertyChangeEvent e) {
        refreshGUI();
      }

    });
  }

  /**
   * Removes all tabs except the specified index.
   *
   * @param index the tab number
   * @return true if tabs removed
   */
  protected boolean removeAllButTab(int index) {
    for(int i = tabbedPane.getTabCount()-1; i>=0; i--) {
      if(i==index) {
        continue;
      }
      if(!saveChangesAt(i)) {
        return false;
      }
      String title = tabbedPane.getTitleAt(i);
      OSPLog.finer("removing tab "+title); //$NON-NLS-1$
      tabbedPane.removeTabAt(i);
    }
    refreshTabTitles();
    refreshFunctionTool();
    return true;
  }

  /**
   * Removes all tabs.
   *
   * @return true if all tabs removed
   */
  protected boolean removeAllTabs() {
    for(int i = tabbedPane.getTabCount()-1; i>=0; i--) {
      if(!saveChangesAt(i)) {
        return false;
      }
      String title = tabbedPane.getTitleAt(i);
      OSPLog.finer("removing tab "+title); //$NON-NLS-1$
      tabbedPane.removeTabAt(i);
    }
    refreshMenubar();
    refreshFunctionTool();
    return true;
  }

  protected void refreshTabTitles() {
    // show variables being plotted
    String[] tabTitles = new String[tabbedPane.getTabCount()];
    for(int i = 0; i<tabTitles.length; i++) {
      DataToolTab tab = (DataToolTab) tabbedPane.getComponentAt(i);
      String dataName = tab.getName();
      Dataset dataset = tab.getWorkingData();
      if(dataset!=null) {
        String col0 = TeXParser.removeSubscripting(dataset.getColumnName(0));
        String col1 = TeXParser.removeSubscripting(dataset.getColumnName(1));
        String s = " ("+col0+", " //$NON-NLS-1$ //$NON-NLS-2$
                   +col1+")";     //$NON-NLS-1$
        tabTitles[i] = dataName+s;
      } else {
        tabTitles[i] = dataName;
      }
    }
    // set tab titles
    for(int i = 0; i<tabTitles.length; i++) {
      tabbedPane.setTitleAt(i, tabTitles[i]);
    }
  }

  protected void refreshMenubar() {
    if(getTabCount()==0) {
      emptyMenubar.add(displayMenu);
      emptyMenubar.add(helpMenu);
      setJMenuBar(emptyMenubar);
      helpLabel.setText(null);
    } else {
      menubar.add(displayMenu);
      menubar.add(helpMenu);
      setJMenuBar(menubar);
    }
  }

  /**
   * Gets the function tool for defining custom data functions.
   */
  protected FunctionTool getDataFunctionTool() {
    if(dataFunctionTool==null) {                                                   // create new tool if none exists
      dataFunctionTool = new FunctionTool(this);
      dataFunctionTool.setFontLevel(fontLevel);
      dataFunctionTool.setHelpPath("data_builder_help.html");                      //$NON-NLS-1$
      dataFunctionTool.addPropertyChangeListener("function", this);                //$NON-NLS-1$
      dataFunctionTool.setTitle(ToolsRes.getString("DataTool.DataBuilder.Title")); //$NON-NLS-1$
    }
    refreshFunctionTool();
    return dataFunctionTool;
  }

  /**
   * Refreshes the function tool.
   */
  protected void refreshFunctionTool() {
    if(dataFunctionTool==null) {
      return;
    }
    // add and remove DataFunctionPanels based on current tabs
    ArrayList<String> tabNames = new ArrayList<String>();
    for(int i = 0; i<tabbedPane.getTabCount(); i++) {
      DataToolTab tab = getTab(i);
      tabNames.add(tab.getName());
      if(dataFunctionTool.getPanel(tab.getName())==null) {
        FunctionPanel panel = new DataFunctionPanel(tab.dataManager);
        dataFunctionTool.addPanel(tab.getName(), panel);
      }
    }
    ArrayList<String> remove = new ArrayList<String>();
    for(Iterator<String> it = dataFunctionTool.panels.keySet().iterator(); it.hasNext(); ) {
      String name = it.next().toString();
      if(!tabNames.contains(name)) {
        remove.add(name);
      }
    }
    for(Iterator<String> it = remove.iterator(); it.hasNext(); ) {
      String name = it.next().toString();
      dataFunctionTool.removePanel(name);
    }
  }

  /**
   * Gets the data cells selected by the user in a datatable. This method is modified
   * from the org.opensourcephysics.display.DataTableFrame getSelectedData method.
   * A descriptive name (optional) and the selected column names precede the data.
   * Data rows are delimited by new lines ("\n"), columns by tabs.
   *
   * @param name a name for the data (may be null)
   * @param table the datatable containing the data
   * @return a StringBuffer containing the data.
   */
  protected StringBuffer getSelectedTableData(String name, DataTable table) {
    StringBuffer buf = new StringBuffer();
    if(name!=null) {
      buf.append(name+"\n"); //$NON-NLS-1$
    }
    if((table.getColumnCount()==1)||(table.getRowCount()==0)) {
      return buf;
    }
    // get selected rows and columns
    int[] rows = table.getSelectedRows();
    // if no rows selected, select all
    if(rows.length==0) {
      table.selectAll();
      rows = table.getSelectedRows();
    }
    int[] columns = table.getSelectedColumns();
    // copy column headings
    for(int j = 0; j<columns.length; j++) {
      int col = columns[j];
      // ignore row heading
      int modelCol = table.convertColumnIndexToModel(col);
      if(table.isRowNumberVisible()&&(modelCol==0)) {
        continue;
      }
      buf.append(table.getColumnName(col));
      buf.append("\t"); // tab after each column //$NON-NLS-1$
    }
    buf.setLength(buf.length()-1); // remove last tab
    buf.append("\n");              //$NON-NLS-1$
    java.text.DateFormat df = java.text.DateFormat.getInstance();
    for(int i = 0; i<rows.length; i++) {
      for(int j = 0; j<columns.length; j++) {
        int col = columns[j];
        int modelCol = table.convertColumnIndexToModel(col);
        // don't copy row numbers
        if(table.isRowNumberVisible()&&(modelCol==0)) {
          continue;
        }
        Object value = table.getValueAt(rows[i], col);
        if(value!=null) {
          if(value instanceof java.util.Date) {
            value = df.format(value);
          }
          buf.append(value);
        }
        buf.append("\t");            // tab after each column //$NON-NLS-1$
      }
      buf.setLength(buf.length()-1); // remove last tab
      buf.append("\n");              // new line after each row //$NON-NLS-1$
    }
    return buf;
  }

  /**
   * Copies to the clipboard.
   *
   * @param text the string to copy
   */
  protected void copy(String text) {
    StringSelection data = new StringSelection(text);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(data, data);
  }

  /**
   * Pastes from the clipboard and returns the pasted string.
   *
   * @return the pasted string, or null if none
   */
  protected String paste() {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable data = clipboard.getContents(null);
    if((data!=null)&&data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      try {
        String text = (String) data.getTransferData(DataFlavor.stringFlavor);
        return text;
      } catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    return null;
  }

  /**
   * Overrides OSPFrame method. This converts EXIT_ON_CLOSE to
   * DO_NOTHING_ON_CLOSE and sets the exitOnClose flag.
   *
   * @param operation the operation
   */
  public void setDefaultCloseOperation(int operation) {
    if((operation==JFrame.EXIT_ON_CLOSE)) {
      exitOnClose = true;
      operation = WindowConstants.DO_NOTHING_ON_CLOSE;
    }
    if((operation!=WindowConstants.DO_NOTHING_ON_CLOSE)) {
      saveChangesOnClose = false;
    }
    super.setDefaultCloseOperation(operation);
  }

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    // configure the frame
    contentPane.setPreferredSize(dim);
    setContentPane(contentPane);
    JPanel centerPanel = new JPanel(new BorderLayout());
    contentPane.add(centerPanel, BorderLayout.CENTER);
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    // add window listener to exit
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        exitItem.doClick();
      }

    });
    this.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        DataToolTab tab = getSelectedTab();
        if(tab==null) {
          return;
        }
        if(!tab.propsCheckbox.isSelected()&&!tab.statsCheckbox.isSelected()) {
          tab.splitPanes[2].setDividerLocation(0);
        }
      }

    });
    // create tabbed pane
    tabbedPane = new JTabbedPane(SwingConstants.TOP);
    centerPanel.add(tabbedPane, BorderLayout.CENTER);
    tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        DataToolTab tab = getSelectedTab();
        // hide fit builders of unselected tabs
        for(int i = 0; i<getTabCount(); i++) {
          DataToolTab next = getTab(i);
          if(next==tab) {
            continue;
          }
          FunctionTool fitter = (next.curveFitter==null)
                                ? null
                                : next.curveFitter.fitBuilder;
          if((fitter!=null)&&fitter.isVisible()) {
            fitter.wasVisible = true;
            fitter.setVisible(false);
          }
        }
        if(tab!=null) {
          // show fit builder of selected tab if previously visible
          FunctionTool fitter = (tab.curveFitter==null)
                                ? null
                                : tab.curveFitter.fitBuilder;
          if((fitter!=null)&&fitter.wasVisible) {
            fitter.setVisible(true);
          }
          tab.dataTable.refreshTable();
          tab.statsTable.refreshStatistics();
          tab.propsTable.refreshTable();
          tab.refreshPlot();
          refreshGUI();
          tab.dataTable.requestFocusInWindow();
        }
      }

    });
    tabbedPane.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(OSPRuntime.isPopupTrigger(e)) {
          final int index = tabbedPane.getSelectedIndex();
          // make popup with name change and close items
          JPopupMenu popup = new JPopupMenu();
          JMenuItem item = new JMenuItem(ToolsRes.getString("DataTool.MenuItem.Name")); //$NON-NLS-1$
          popup.add(item);
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              DataToolTab tab = getTab(index);
              String name = tab.getName();
              Object input = JOptionPane.showInputDialog(DataTool.this,
                ToolsRes.getString("DataTool.Dialog.Name.Message"),                     //$NON-NLS-1$
                ToolsRes.getString("DataTool.Dialog.Name.Title"),     //$NON-NLS-1$
                JOptionPane.QUESTION_MESSAGE, null, null, name);
              if(input==null) {
                return;
              }
              // hide tab name so getUniqueName not confused
              tab.setName("");                                        //$NON-NLS-1$
              tab.setName(getUniqueName(input.toString()));
              tab.tabChanged(true);
              refreshTabTitles();
              refreshFunctionTool();
            }

          });
          popup.addSeparator();
          item = new JMenuItem(ToolsRes.getString("MenuItem.Close")); //$NON-NLS-1$
          popup.add(item);
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              removeTab(index);
            }

          });
          item = new JMenuItem(ToolsRes.getString("MenuItem.CloseOthers")); //$NON-NLS-1$
          popup.add(item);
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              removeAllButTab(index);
            }

          });
          item = new JMenuItem(ToolsRes.getString("MenuItem.CloseAll")); //$NON-NLS-1$
          popup.add(item);
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              removeAllTabs();
            }

          });
          FontSizer.setFonts(popup, fontLevel);
          popup.show(tabbedPane, e.getX(), e.getY()+8);
        }
      }

    });
    int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // create regular menubar
    menubar = new JMenuBar();
    fileMenu = new JMenu();
    menubar.add(fileMenu);
    MouseAdapter fileMenuChecker = new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        mousePressed(e);
      }
      public void mousePressed(MouseEvent e) {
        boolean empty = getSelectedTab().owner==null;
        exportItem.setEnabled(!empty);
        saveItem.setEnabled(!empty);
        saveAsItem.setEnabled(!empty);
        int[] selectedRows = getSelectedTab().dataTable.getSelectedRows();
        int endRow = getSelectedTab().dataTable.getRowCount()-1;
        if((selectedRows.length==0)
          ||((selectedRows.length==1)&&(selectedRows[0]==endRow)&&getSelectedTab().dataTable.isEmptyRow(endRow))) {
          exportItem.setText(ToolsRes.getString("DataTool.MenuItem.Export"));          //$NON-NLS-1$
        } else {
          exportItem.setText(ToolsRes.getString("DataTool.MenuItem.ExportSelection")); //$NON-NLS-1$
        }
      }

    };
    fileMenu.addMouseListener(fileMenuChecker);
    newTabItem = new JMenuItem();
    newTabItem.setAccelerator(KeyStroke.getKeyStroke('N', keyMask));
    newTabItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        DataToolTab tab = addTab((Data) null);
        tab.userEditable = true;
        tab.refreshGUI();
      }
    });
    fileMenu.add(newTabItem);
    if(!OSPRuntime.appletMode) {
      openItem = new JMenuItem();
      openItem.setAccelerator(KeyStroke.getKeyStroke('O', keyMask));
      openItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          open();
        }

      });
      fileMenu.add(openItem);
    }
    fileMenu.addSeparator();
    closeItem = new JMenuItem();
    closeItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = tabbedPane.getSelectedIndex();
        removeTab(index);
      }

    });
    fileMenu.add(closeItem);
    closeAllItem = new JMenuItem();
    closeAllItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeAllTabs();
      }

    });
    fileMenu.add(closeAllItem);
    fileMenu.addSeparator();
    if(!OSPRuntime.appletMode) {
      exportItem = new JMenuItem();
      exportItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int[] selectedRows = getSelectedTab().dataTable.getSelectedRows();
          int endRow = getSelectedTab().dataTable.getRowCount()-1;
          if((selectedRows.length==1)&&(selectedRows[0]==endRow)&&getSelectedTab().dataTable.isEmptyRow(endRow)) {
            getSelectedTab().dataTable.clearSelection();
          }
          String tabName = getSelectedTab().getName();
          String data = getSelectedTableData(tabName, getSelectedTab().dataTable).toString();
          write(data);
        }

      });
      fileMenu.add(exportItem);
      fileMenu.addSeparator();
      // save item
      saveItem = new JMenuItem();
      saveItem.setAccelerator(KeyStroke.getKeyStroke('S', keyMask));
      saveItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          DataToolTab tab = getSelectedTab();
          save(tab.fileName);
        }

      });
      fileMenu.add(saveItem);
      // save as item
      saveAsItem = new JMenuItem();
      saveAsItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveAs();
        }

      });
      fileMenu.add(saveAsItem);
      fileMenu.addSeparator();
    }
    printItem = new JMenuItem();
    printItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SnapshotTool.getTool().printImage(DataTool.this);
      }

    });
    printItem.setAccelerator(KeyStroke.getKeyStroke('P', keyMask));
    fileMenu.add(printItem);
    fileMenu.addSeparator();
    exitItem = new JMenuItem();
    exitItem.setAccelerator(KeyStroke.getKeyStroke('Q', keyMask));
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(!saveChangesOnClose||removeAllTabs()) {
          if(exitOnClose) {
            System.exit(0);
          } else {
            setVisible(false);
          }
        }
      }

    });
    fileMenu.add(exitItem);
    editMenu = new JMenu();
    // create mouse listener to prepare edit menu
    MouseAdapter editMenuChecker = new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        mousePressed(e);
      }
      public void mousePressed(MouseEvent e) {
        // ignore until menu is displayed
        if(!editMenu.isPopupMenuVisible()&&!emptyEditMenu.isPopupMenuVisible()) {
          return;
        }
        DataToolTab tab = getSelectedTab();
        // undo and redo items
        if(tab!=null) {
          undoItem.setEnabled(tab.undoManager.canUndo());
          redoItem.setEnabled(tab.undoManager.canRedo());
        }
        // enable paste menu if clipboard contains addable data
        String dataString = paste();
        boolean enabled = dataString!=null;
        if(enabled) {
          if(!dataString.startsWith("<?xml")) {                   //$NON-NLS-1$
            addableData = importCharDelimitedData(dataString, null);
            enabled = addableData!=null;
          } else {
            control = new XMLControlElement();
            control.readXML(dataString);
            Class<?> type = control.getObjectClass();
            if(Data.class.isAssignableFrom(type)) {
              addableData = (DatasetManager) control.loadObject(null);
            }
            enabled = Data.class.isAssignableFrom(type)||DataToolTab.class.isAssignableFrom(type)
                      ||control.getBoolean("data_tool_transfer"); //$NON-NLS-1$;
          }
        }
        emptyPasteItem.setEnabled(enabled);
        pasteMenu.setEnabled(enabled);
        // prepare copy menu
        copyMenu.removeAll();
        if(tab!=null) {
          ArrayList<Dataset> list = tab.dataManager.getDatasets();
          copyDataItem.setEnabled(!list.isEmpty());
          if(!list.isEmpty()) {
            copyTabItem.setText(ToolsRes.getString("DataTool.MenuItem.CopyTab")); //$NON-NLS-1$
            copyMenu.add(copyTabItem);
            //            int[] cols = tab.dataTable.getSelectedColumns();
            //            copyColumnsItem.setText(cols.length == 0?
            //                ToolsRes.getString("DataTool.MenuItem.CopyAllColumns"):
            //                ToolsRes.getString("DataTool.MenuItem.CopySelectedColumns")); //$NON-NLS-1$
            //            copyMenu.add(copyColumnsItem);
            copyMenu.addSeparator();
            String s = ToolsRes.getString("DataTool.MenuItem.CopyData");          //$NON-NLS-1$
            int[] selectedRows = getSelectedTab().dataTable.getSelectedRows();
            int endRow = getSelectedTab().dataTable.getRowCount()-1;
            boolean emptySelection = (selectedRows.length==1)&&(selectedRows[0]==endRow)
                                     &&getSelectedTab().dataTable.isEmptyRow(endRow);
            if((selectedRows.length>0)&&!emptySelection) {
              s = ToolsRes.getString("DataTool.MenuItem.CopySelectedData"); //$NON-NLS-1$
            }
            copyDataItem.setText(s);
            copyMenu.add(copyDataItem);
            copyMenu.addSeparator();
          }
        }
        copyMenu.add(copyImageItem);
        FontSizer.setFonts(copyMenu, fontLevel);
      }

    };
    editMenu.addMouseListener(editMenuChecker);
    menubar.add(editMenu);
    // undo and redo items
    undoItem = new JMenuItem();
    undoItem.setEnabled(false);
    undoItem.setAccelerator(KeyStroke.getKeyStroke('Z', keyMask));
    undoItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getSelectedTab().undoManager.undo();
      }

    });
    editMenu.add(undoItem);
    redoItem = new JMenuItem();
    redoItem.setEnabled(false);
    redoItem.setAccelerator(KeyStroke.getKeyStroke('Y', keyMask));
    redoItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getSelectedTab().undoManager.redo();
      }

    });
    editMenu.add(redoItem);
    editMenu.addSeparator();
    // copy menu
    copyMenu = new JMenu();
    editMenu.add(copyMenu);
    copyTabItem = new JMenuItem();
    copyTabItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int i = tabbedPane.getSelectedIndex();
        String title = tabbedPane.getTitleAt(i);
        OSPLog.finest("copying tab "+title); //$NON-NLS-1$
        XMLControl control = new XMLControlElement(getSelectedTab());
        copy(control.toXML());
      }

    });
    //    copyColumnsItem = new JMenuItem();
    //    copyColumnsItem.addActionListener(new ActionListener() {
    //      public void actionPerformed(ActionEvent e) {
    //        DataToolTab tab = getSelectedTab();
    //        OSPLog.finest("copying columns from "+tab.getName()); //$NON-NLS-1$
    //        int[] cols = tab.dataTable.getSelectedColumns();
    //        DatasetManager data = new DatasetManager();
    //        ArrayList list = tab.dataManager.getDatasets();
    //        if (cols.length == 0) {
    //          // copy all datasets
    //          for (int i = 0; i < list.size(); i++) {
    //            Dataset next = (Dataset)list.get(i);
    //            Dataset dataset = tab.copyDataset(next, null);
    //            data.addDataset(dataset);
    //          }
    //          tab.dataTable.setColumnSelectionInterval(list.size(), 0);
    //        }
    //        else {
    //          // copy selected datasets
    //          for (int i = 0; i < cols.length; i++) {
    //            int n = tab.dataTable.convertColumnIndexToModel(cols[i]);
    //            Dataset next = (Dataset)list.get(n-1);
    //            Dataset dataset = tab.copyDataset(next, null);
    //            data.addDataset(dataset);
    //          }
    //        }
    //        control = new XMLControlElement(data);
    //        control.setValue("data_tool_transfer", true); //$NON-NLS-1$
    //        control.setValue("data_tool_tab", getSelectedTab().getName()); //$NON-NLS-1$
    //        copy(control.toXML());
    //        tab.dataTable.setRowSelectionInterval(
    //            tab.dataTable.getRowCount()-1, 0);
    //        tab.dataTable.repaint();
    //      }
    //    });
    copyDataItem = new JMenuItem();
    copyDataItem.setAccelerator(KeyStroke.getKeyStroke('C', keyMask));
    copyDataItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int[] selectedRows = getSelectedTab().dataTable.getSelectedRows();
        int endRow = getSelectedTab().dataTable.getRowCount()-1;
        if((selectedRows.length==1) && (selectedRows[0]==endRow)
        		&& getSelectedTab().dataTable.isEmptyRow(endRow)) {
          getSelectedTab().dataTable.clearSelection();
        }
        String tabName = getSelectedTab().getName();
        OSPLog.finest("copying cells from "+tabName); //$NON-NLS-1$
        String data = getSelectedTableData(tabName, getSelectedTab().dataTable).toString();
        copy(data);
      }

    });
    copyImageItem = new JMenuItem();
    copyImageItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String tabName = getSelectedTab().getName();
        OSPLog.finest("copying image of "+tabName); //$NON-NLS-1$
        SnapshotTool.getTool().copyImage(DataTool.this);
      }

    });
    MouseAdapter pasteMenuChecker = new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        // ignore if menu is disabled or already displayed
        if(!pasteMenu.isEnabled()||pasteMenu.isPopupMenuVisible()) {
          return;
        }
        // enable pasteColumnsItem if clipboard contains pastable columns
        String addableColumns = ""; //$NON-NLS-1$
        DataToolTab tab = getSelectedTab();
        boolean pastable = false;
        if(addableData!=null) {
          // pastable if the tab name is different 
          if(!addableData.getName().equals(tab.getName())) {
            pastable = true;
            // or same tab but single unique column name
          } else if(addableData.getDatasets().size()==1) {
            Dataset d = addableData.getDataset(0);
            int i = tab.dataManager.getDatasetIndex(d.getYColumnName());
            pastable = i==-1;
          }
        }
        if(pastable) {
          Iterator<Dataset> it = addableData.getDatasets().iterator();
          while(it.hasNext()) {
            Dataset next = it.next();
            if(tab.isDuplicateColumn(next.getXColumnName(), next.getXPoints())
              &&!tab.isDuplicateColumn(next.getYColumnName(), next.getYPoints())) {
              if(!addableColumns.equals("")) { //$NON-NLS-1$
                addableColumns += ",";                                                       //$NON-NLS-1$
              }
              addableColumns += " \""+next.getYColumnName()+"\"";                            //$NON-NLS-1$ //$NON-NLS-2$
            }
          }
        }
        if(addableColumns.equals("")) {                                                      //$NON-NLS-1$
          addableData = null;
          pasteMenu.remove(pasteColumnsItem);
        } else {
          pasteColumnsItem.setText(ToolsRes.getString("DataTool.MenuItem.PasteNewColumns")); //$NON-NLS-1$
          pasteMenu.add(pasteColumnsItem);
        }
        FontSizer.setFonts(pasteMenu, fontLevel);
      }

    };
    pasteMenu = new JMenu();
    pasteMenu.addMouseListener(pasteMenuChecker);
    editMenu.add(pasteMenu);
    pasteNewTabItem = new JMenuItem();
    pasteNewTabItem.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        boolean failed = false;
        String dataString = paste();
        if(dataString!=null) {
          if(!dataString.startsWith("<?xml")) {                                                                      //$NON-NLS-1$
            Data importedData = importCharDelimitedData(dataString, null);
            OSPLog.finest("pasting clipboard data into new tab");                                                    //$NON-NLS-1$
            if(importedData!=null) {
            	DataToolTab tab = addTab(importedData);
            	if (tab != null) {
            		tab.userEditable = true;
            		tab.refreshGUI();
	              refreshFunctionTool();
	              return;
            	}
            }
            failed = true;
          }
          if(!failed) {
            control = new XMLControlElement();
            control.readXML(dataString);
            if(control.failedToRead()) {
              failed = true;
            }
          }
          if(!failed) {
            OSPLog.finest("pasting clipboard XML into new tab");                                                     //$NON-NLS-1$
            if(Dataset.class.isAssignableFrom(control.getObjectClass())
            		||control.getBoolean("data_tool_transfer")) { //$NON-NLS-1$
            	DataToolTab tab = loadData(null, control, false);
            	if (tab != null) {
            		tab.userEditable = control.getBoolean("data_tool_transfer"); //$NON-NLS-1$
            		tab.refreshGUI();
                int i = getTabCount()-1;
                tabbedPane.setSelectedIndex(i);
              } else {
                failed = true;
              }
            } else if(addTab(control)==null) {
              failed = true;
              OSPLog.finest("no clipboard data found");                                                  //$NON-NLS-1$
            }
          }
          if(!failed) {
            refreshFunctionTool();
          }
        }
        if(failed) {
          JOptionPane.showMessageDialog(DataTool.this, ToolsRes.getString("Tool.Dialog.NoData.Message"), //$NON-NLS-1$
                                        ToolsRes.getString("Tool.Dialog.NoData.Title"),                  //$NON-NLS-1$
                                        JOptionPane.WARNING_MESSAGE);
        }
      }

    });
    pasteMenu.add(pasteNewTabItem);
    pasteColumnsItem = new JMenuItem();
    pasteColumnsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(addableData!=null) {
          DataToolTab tab = getSelectedTab();
          OSPLog.finest("pasting columns into "+tab.getName()); //$NON-NLS-1$
          // look for y-column with same name as "horz" column,
          int labelCol = tab.dataTable.convertColumnIndexToView(0);
          int horzCol = tab.dataTable.convertColumnIndexToModel((labelCol==0)
            ? 1
            : 0);
          Dataset match = null;
          if(horzCol>0) {
            Dataset horzData = tab.dataManager.getDataset(horzCol-1);
            String horzName = horzData.getYColumnName();
            Iterator<Dataset> it = addableData.getDatasets().iterator();
            while(it.hasNext()) {
              Dataset next = it.next();
              if(next.getYColumnName().equals(horzName)) {
                // found matching column name, now compare their points
                double[] matchPts = next.getValidYPoints();
                double[] horzPts = horzData.getYPoints();
                // do either contain duplicate values?
                if(containsDuplicateValues(matchPts)||containsDuplicateValues(horzPts)) {
                  break;
                }
                // are matchPts a subset of horzPts?
                boolean isSubset = true;
                for(int i = 0; i<matchPts.length; i++) {
                  isSubset = isSubset&&(getIndex(matchPts[i], horzPts, -1)>-1);
                }
                if(isSubset) {
                  match = next;
                  // don't add match but use it to reorder
                  // other datasets before adding them 
                  double[] rows = horzData.getXPoints();
                  Iterator<Dataset> it2 = addableData.getDatasets().iterator();
                  while(it2.hasNext()) {
                    Dataset d = it2.next();
                    if(d==match) {
                      continue;
                    }
                    double[] input = d.getYPoints();
                    double[] y = new double[rows.length];
                    for(int i = 0; i<y.length; i++) {
                      y[i] = Double.NaN;
                    }
                    for(int i = 0; i<matchPts.length; i++) {
                      int index = getIndex(matchPts[i], horzPts, -1);
                      if(index>-1) {
                        y[index] = input[i];
                      }
                    }
                    d.clear();
                    d.append(rows, y);
                  }
                } else {
                  // are horzPts a subset of matchPts?
                  isSubset = true;
                  for(int i = 0; i<horzPts.length; i++) {
                    isSubset = isSubset&&(getIndex(horzPts[i], matchPts, -1)>-1);
                  }
                  if(isSubset) {
                    match = next;
                    // use horzPts to reorder
                    // existing datasets before adding new ones 
                    double[] rows = match.getXPoints();
                    double[] newHorzPts = match.getYPoints();
                    Iterator<Dataset> it2 = tab.dataManager.getDatasets().iterator();
                    while(it2.hasNext()) {
                      Dataset d = it2.next();
                      double[] input = d.getYPoints();
                      double[] y = new double[rows.length];
                      for(int i = 0; i<y.length; i++) {
                        if(d==horzData) {
                          y[i] = newHorzPts[i];
                        } else {
                          y[i] = Double.NaN;
                        }
                      }
                      for(int i = 0; i<horzPts.length; i++) {
                        int index = getIndex(horzPts[i], matchPts, -1);
                        if(index>-1) {
                          y[index] = input[i];
                        }
                      }
                      d.clear();
                      d.append(rows, y);
                    }
                  }
                }
              }
            }
          }
          Iterator<Dataset> it = addableData.getDatasets().iterator();
          while(it.hasNext()) {
            Dataset next = it.next();
            if(next==match) {
              continue;
            }
            if(tab.addData(next)) {
              int col = tab.dataTable.getColumnCount()-1;
              // post edit: target is column, value is dataset
              TableEdit edit = tab.dataTable.new TableEdit(DataToolTable.INSERT_COLUMN_EDIT, next.getYColumnName(),
                new Integer(col), next);
              tab.undoSupport.postEdit(edit);
              tab.dataTable.refreshUndoItems();
              refreshFunctionTool();
              OSPLog.finest("pasted column "+next.getYColumnName() //$NON-NLS-1$
                            +" into tab "+tab.getName());          //$NON-NLS-1$
            }
          }
        }
      }

    });
    pasteMenu.add(pasteColumnsItem);
    displayMenu = new JMenu();
    menubar.add(displayMenu);
    languageMenu = new JMenu();
    // get resource before installed locales so that launch jar is not null
    String base = "/org/opensourcephysics/resources/tools/html/"; //$NON-NLS-1$
    String help = XML.getResolvedPath(helpPath, base);
    ResourceLoader.getResource(help);
    final Locale[] locales = OSPRuntime.getInstalledLocales();
    Action languageAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        String language = e.getActionCommand();
        OSPLog.finest("setting language to "+language); //$NON-NLS-1$ 
        for(int i = 0; i<locales.length; i++) {
          if(language.equals(locales[i].getDisplayName())) {
            ToolsRes.setLocale(locales[i]);
            return;
          }
        }
      }

    };
    ButtonGroup languageGroup = new ButtonGroup();
    languageItems = new JMenuItem[locales.length];
    for(int i = 0; i<locales.length; i++) {
      languageItems[i] = new JRadioButtonMenuItem(locales[i].getDisplayName(locales[i]));
      languageItems[i].setActionCommand(locales[i].getDisplayName());
      languageItems[i].addActionListener(languageAction);
      languageMenu.add(languageItems[i]);
      languageGroup.add(languageItems[i]);
    }
    displayMenu.add(languageMenu);
    fontSizeMenu = new JMenu();
    displayMenu.add(fontSizeMenu);
    ButtonGroup group = new ButtonGroup();
    Action fontSizeAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        int i = Integer.parseInt(e.getActionCommand());
        setFontLevel(i);
      }

    };
    for(int i = 0; i<4; i++) {
      JMenuItem item = new JRadioButtonMenuItem("+"+i); //$NON-NLS-1$
      if(i==0) {
        defaultFontSizeItem = item;
      }
      item.addActionListener(fontSizeAction);
      item.setActionCommand(""+i);                      //$NON-NLS-1$
      fontSizeMenu.add(item);
      group.add(item);
      if(i==FontSizer.getLevel()) {
        item.setSelected(true);
      }
    }
    helpMenu = new JMenu();
    menubar.add(helpMenu);
    helpItem = new JMenuItem();
    helpItem.setAccelerator(KeyStroke.getKeyStroke('H', keyMask));
    helpItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(helpFrame==null) {
          String help = XML.getResolvedPath(helpPath, helpBase);
          if(ResourceLoader.getResource(help)!=null) {
            helpFrame = new TextFrame(help);
          } else {
            String classBase = "/org/opensourcephysics/resources/tools/html/"; //$NON-NLS-1$
            help = XML.getResolvedPath(helpPath, classBase);
            helpFrame = new TextFrame(help);
          }
          helpFrame.setSize(760, 560);
          // center on the screen
          Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
          int x = (dim.width-helpFrame.getBounds().width)/2;
          int y = (dim.height-helpFrame.getBounds().height)/2;
          helpFrame.setLocation(x, y);
        }
        helpFrame.setVisible(true);
      }

    });
    helpMenu.add(helpItem);
    helpMenu.addSeparator();
    logItem = new JMenuItem();
    logItem.setAccelerator(KeyStroke.getKeyStroke('L', keyMask));
    logItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFrame frame = OSPLog.getOSPLog();
        if((frame.getLocation().x==0)&&(frame.getLocation().y==0)) {
          Point p = getLocation();
          frame.setLocation(p.x+28, p.y+28);
        }
        frame.setVisible(true);
      }

    });
    helpMenu.add(logItem);
    helpMenu.addSeparator();
    aboutItem = new JMenuItem();
    aboutItem.setAccelerator(KeyStroke.getKeyStroke('A', keyMask));
    aboutItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showAboutDialog();
      }

    });
    helpMenu.add(aboutItem);
    setJMenuBar(menubar);
    // create the empty menu bar for use when no tabs are open
    emptyMenubar = new JMenuBar();
    emptyFileMenu = new JMenu();
    emptyMenubar.add(emptyFileMenu);
    emptyNewTabItem = new JMenuItem();
    emptyNewTabItem.setAccelerator(KeyStroke.getKeyStroke('N', keyMask));
    emptyNewTabItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        DataToolTab tab = addTab((Data) null);
        tab.userEditable = true;
        tab.refreshGUI();
      }
    });
    emptyFileMenu.add(emptyNewTabItem);
    emptyOpenItem = new JMenuItem();
    emptyOpenItem.setAccelerator(KeyStroke.getKeyStroke('O', keyMask));
    emptyOpenItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        open();
      }

    });
    emptyFileMenu.add(emptyOpenItem);
    emptyFileMenu.addSeparator();
    emptyExitItem = new JMenuItem();
    emptyExitItem.setAccelerator(KeyStroke.getKeyStroke('Q', keyMask));
    emptyExitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }

    });
    emptyFileMenu.add(emptyExitItem);
    emptyEditMenu = new JMenu();
    emptyEditMenu.addMouseListener(editMenuChecker);
    emptyMenubar.add(emptyEditMenu);
    emptyPasteItem = new JMenuItem();
    emptyPasteItem.addActionListener(pasteNewTabItem.getAction());
    emptyEditMenu.add(emptyPasteItem);
    // create help label for status bar
    helpLabel = new JLabel("", SwingConstants.LEADING); //$NON-NLS-1$
    helpLabel.setFont(new JTextField().getFont());
    helpLabel.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
    centerPanel.add(helpLabel, BorderLayout.SOUTH);
    refreshGUI();
    refreshMenubar();
    pack();
    // center this on the screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width-getBounds().width)/2;
    int y = (dim.height-getBounds().height)/2;
    setLocation(x, y);
  }

  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
    setTitle(ToolsRes.getString("DataTool.Frame.Title"));                              //$NON-NLS-1$
    emptyFileMenu.setText(ToolsRes.getString("Menu.File"));                            //$NON-NLS-1$
    emptyNewTabItem.setText(ToolsRes.getString("DataTool.MenuItem.NewTab"));           //$NON-NLS-1$
    emptyOpenItem.setText(ToolsRes.getString("MenuItem.Open"));                        //$NON-NLS-1$
    emptyExitItem.setText(ToolsRes.getString("MenuItem.Exit"));                        //$NON-NLS-1$
    emptyEditMenu.setText(ToolsRes.getString("Menu.Edit"));                            //$NON-NLS-1$
    emptyPasteItem.setText(ToolsRes.getString("MenuItem.Paste"));                      //$NON-NLS-1$
    fileMenu.setText(ToolsRes.getString("Menu.File"));                                 //$NON-NLS-1$
    newTabItem.setText(ToolsRes.getString("DataTool.MenuItem.NewTab"));                //$NON-NLS-1$
    openItem.setText(ToolsRes.getString("MenuItem.Open"));                             //$NON-NLS-1$
    closeItem.setText(ToolsRes.getString("MenuItem.Close"));                           //$NON-NLS-1$
    closeAllItem.setText(ToolsRes.getString("MenuItem.CloseAll"));                     //$NON-NLS-1$
    saveItem.setText(ToolsRes.getString("DataTool.MenuItem.Save"));                    //$NON-NLS-1$
    saveAsItem.setText(ToolsRes.getString("DataTool.MenuItem.SaveAs"));                //$NON-NLS-1$
    printItem.setText(ToolsRes.getString("DataTool.MenuItem.Print"));                  //$NON-NLS-1$
    exitItem.setText(ToolsRes.getString("MenuItem.Exit"));                             //$NON-NLS-1$
    editMenu.setText(ToolsRes.getString("Menu.Edit"));                                 //$NON-NLS-1$
    undoItem.setText(ToolsRes.getString("DataTool.MenuItem.Undo"));                    //$NON-NLS-1$
    redoItem.setText(ToolsRes.getString("DataTool.MenuItem.Redo"));                    //$NON-NLS-1$
    copyMenu.setText(ToolsRes.getString("DataTool.Menu.Copy"));                        //$NON-NLS-1$
    copyImageItem.setText(ToolsRes.getString("DataTool.MenuItem.CopyImage"));          //$NON-NLS-1$
    pasteMenu.setText(ToolsRes.getString("MenuItem.Paste"));                           //$NON-NLS-1$
    pasteNewTabItem.setText(ToolsRes.getString("DataTool.MenuItem.PasteNewTab"));      //$NON-NLS-1$
    pasteColumnsItem.setText(ToolsRes.getString("DataTool.MenuItem.PasteNewColumns")); //$NON-NLS-1$
    displayMenu.setText(ToolsRes.getString("Tool.Menu.Display"));                      //$NON-NLS-1$
    languageMenu.setText(ToolsRes.getString("Tool.Menu.Language"));                    //$NON-NLS-1$
    fontSizeMenu.setText(ToolsRes.getString("Tool.Menu.FontSize"));                    //$NON-NLS-1$
    defaultFontSizeItem.setText(ToolsRes.getString("Tool.MenuItem.DefaultFontSize"));  //$NON-NLS-1$
    helpMenu.setText(ToolsRes.getString("Menu.Help"));                                 //$NON-NLS-1$
    helpItem.setText(ToolsRes.getString("DataTool.MenuItem.Help"));                    //$NON-NLS-1$
    logItem.setText(ToolsRes.getString("MenuItem.Log"));                               //$NON-NLS-1$
    aboutItem.setText(ToolsRes.getString("MenuItem.About"));                           //$NON-NLS-1$
    Locale[] locales = OSPRuntime.getInstalledLocales();
    for(int i = 0; i<locales.length; i++) {
      if(locales[i].getLanguage().equals(ToolsRes.resourceLocale.getLanguage())) {
        languageItems[i].setSelected(true);
      }
    }
  }

  /**
   * Shows the about dialog.
   */
  protected void showAboutDialog() {
    String aboutString = getName()+" 1.4  November 2008\n" //$NON-NLS-1$
                         +"Code Author: Douglas Brown\n"   //$NON-NLS-1$
                         +"Open Source Physics Project\n"  //$NON-NLS-1$
                         +"www.opensourcephysics.org";     //$NON-NLS-1$
    JOptionPane.showMessageDialog(this, aboutString, ToolsRes.getString("Dialog.About.Title")+" "+getName(), //$NON-NLS-1$ //$NON-NLS-2$
                                  JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Creates a button with a specified maximum height.
   *
   * @param text the button text
   * @param h the button height
   * @return the button
   */
  protected static JButton createButton(String text) {
    JButton button = new JButton(text) {
      public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        dim.height = buttonHeight;
        return dim;
      }

    };
    return button;
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
