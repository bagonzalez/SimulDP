/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import javax.swing.*;

import java.awt.*;
import javax.swing.event.*;
import java.beans.*;

/**
 * A panel that displays an ArrayTable.
 *
 * @author Douglas Brown
 * @author Wolfgang Christian
 * @version 1.0
 */
public class ArrayPanel extends JPanel implements PropertyChangeListener {
  JTabbedPane tabbedPane = new JTabbedPane();
  ArrayTable[] tables;
  JSpinner spinner;
  JScrollPane scrollpane;
  Object array;
  boolean changed;
  // Store previous values: Paco
  String format=null;
  String[] columnNames=null;
  int firstRowIndex=0, firstColIndex=0;
  boolean rowNumberVisible=true, editable=true;
    
  /**
   * Constructor ArrayPanel
   */
  public ArrayPanel() {
    //System.out.println("creating array panel");
  }

  public static ArrayPanel getArrayPanel(Object arrayObj) {
    ArrayPanel arrayPanel = new ArrayPanel();
    arrayPanel.setArray(arrayObj);
    return arrayPanel;
  }

  /**
   * Gets an array panel for the specified array.
   *
   * @param arrayObj the array
   * @param name the display name for the array
   * @return the array panel
   */
  public void setArray(Object arrayObj) {
    if(!canDisplay(arrayObj)) {
      return;
    }
    if(arrayObj instanceof double[]) {
      setArray((double[]) arrayObj);
    } else if(arrayObj instanceof double[][]) {
      setArray((double[][]) arrayObj);
    } else if(arrayObj instanceof double[][][]) {
      setArray((double[][][]) arrayObj);
    } else if(arrayObj instanceof int[]) {
      setArray((int[]) arrayObj);
    } else if(arrayObj instanceof int[][]) {
      setArray((int[][]) arrayObj);
    } else if(arrayObj instanceof int[][][]) {
      setArray((int[][][]) arrayObj);
    } else if(arrayObj instanceof String[]) {
      setArray((String[]) arrayObj);
    } else if(arrayObj instanceof String[][]) {
      setArray((String[][]) arrayObj);
    } else if(arrayObj instanceof String[][][]) {
      setArray((String[][][]) arrayObj);
    } else if(arrayObj instanceof boolean[]) {
      setArray((boolean[]) arrayObj);
    } else if(arrayObj instanceof boolean[][]) {
      setArray((boolean[][]) arrayObj);
    } else if(arrayObj instanceof boolean[][][]) {
      setArray((boolean[][][]) arrayObj);
    }
    this.array = arrayObj;
  }


  /**
   * Determines if an object is an array that can be displayed.
   *
   * @param obj the object
   * @return true if it can be inspected
   */
  public static boolean canDisplay(Object obj) {
    if(obj==null) {
      return false;
    }
    if((obj instanceof double[])||(obj instanceof double[][])||(obj instanceof double[][][])||(obj instanceof int[])
      ||(obj instanceof int[][])||(obj instanceof int[][][])||(obj instanceof boolean[])||(obj instanceof boolean[][])
      ||(obj instanceof boolean[][][])||(obj instanceof String[])||(obj instanceof String[][])
      ||(obj instanceof String[][][])) {
      return true;
    }
    return false;
  }

  /**
   * Gets the object being displayed.
   *
   * @return
   */
  public Object getArray() {
    return array;
  }

  /**
   * Listens for cell events (data changes) from ArrayTable.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    // forward event to listeners
    changed = true;
    firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
  }

  /**
   * Sets the numeric display format.
   *
   * @param defaultFormat
   */
  public void setNumericFormat(String _format) {
    this.format = _format;
    for(int i = 0; i<tables.length; i++) {
      tables[i].setNumericFormat(_format);
    }
  }

  /**
   * Sets the display row number flag. Table displays row number.
   *
   * @param vis <code>true<\code> if table display row number
   */
  public void setRowNumberVisible(boolean vis) {
    this.rowNumberVisible = vis;
    for(int i = 0; i<tables.length; i++) {
      tables[i].setRowNumberVisible(vis); // refreshes the table
    }
  }

  /**
   * Sets the column names in the table models.
   *
   * @param names
   */
  public void setColumnNames(String[] names) {
    //System.out.println ("Setting col names. name[0]="+names[0]);
    this.columnNames = names;
    for(int i = 0; i<tables.length; i++) {
      tables[i].setColumnNames(names);
    }
  }

  // Changes by Willy Gerber
  
  /**
   * Sets this column's preferred width of the given column.
   * The minimum width is set to zero and the maximum width is set to 300. 
   * 
   * @param ncol the column
   * @param nwidth the preferred width
   */
  public void setPreferredColumnWidth(int ncol, int nwidth) {
    for (int table=0; table<tables.length; table++) {
      javax.swing.table.TableColumn column=tables[table].getColumnModel().getColumn(ncol);
      column.setMinWidth(0);
      column.setMaxWidth(300);
      column.setPreferredWidth(nwidth);
    }
  }

  /**
   * Sets this column's preferred width of the entire table.
   * The minimum width is set to zero and the maximum width is set to 300. 
   * 
   * @param nwidth the preferred width
   */
  public void setPreferredColumnWidth(int nwidth) {
    for (int table=0; table<tables.length; table++) {
      for(int col = 0; col<tables[table].getColumnCount(); col++) {
        javax.swing.table.TableColumn column=tables[table].getColumnModel().getColumn(col);
        column.setMinWidth(0);
        column.setMaxWidth(300);
        column.setPreferredWidth(nwidth);
      }
    }
  }

  /**
   * Sets the alignment of the contents of the given column along the X axis.
   * The alignment constants are defined in the SwingConstants class.
   * 
   * @param ncol the column
   * @param align  One of the following constants defined in <code>SwingConstants</code>:
   *           <code>LEFT</code>,
   *           <code>CENTER</code> (the default for image-only labels),
   *           <code>RIGHT</code>,
   *           <code>LEADING</code> (the default for text-only labels) or
   *           <code>TRAILING</code>.
   */
  public void setColumnAlignment(int ncol, int align) {
    for (int table=0; table<tables.length; table++) {
      for(int row = 0; row<tables[table].getRowCount(); row++) {
        javax.swing.table.TableCellRenderer renderer = tables[table].getCellRenderer(row,ncol);
        ((JLabel)renderer).setHorizontalAlignment(align);
      }
    }
  }
  
  /**
   * Sets the alignment of the contents of all table columns along the X axis.
   * The alignment constants are defined in the SwingConstants class.
   * 
   * @param align  One of the following constants defined in <code>SwingConstants</code>:
   *           <code>LEFT</code>,
   *           <code>CENTER</code> (the default for image-only labels),
   *           <code>RIGHT</code>,
   *           <code>LEADING</code> (the default for text-only labels) or
   *           <code>TRAILING</code>.
   */
  public void setColumnAlignment (int align) {
    for (int table=0; table<tables.length; table++) {
      for(int row = 0; row<tables[table].getRowCount(); row++) {
        for(int col = 0; col<tables[table].getColumnCount(); col++) {
          javax.swing.table.TableCellRenderer renderer = tables[table].getCellRenderer(row,col);
          ((JLabel)renderer).setHorizontalAlignment(align);
        }
      }
    }
  }
  
  // End of changes by Willy Gerber
  
  public int getFirstRowIndex() { return this.firstRowIndex; }

  /**
   * Sets the first row's index.
   *
   * @param index
   */
  public void setFirstRowIndex(int index) {
    this.firstRowIndex = index;
    for(int i = 0; i<tables.length; i++) {
      tables[i].setFirstRowIndex(index);
    }
  }

  /**
   * Sets the first column's index.
   *
   * @param index
   */
  public void setFirstColIndex(int index) {
    this.firstColIndex = index;
    for(int i = 0; i<tables.length; i++) {
      tables[i].setFirstColIndex(index);
    }
  }

  /**
   * Sets the column's lock flag.
   *
   * @param column   int
   * @param locked   boolean
   */
  public void setColumnLock(int columnIndex, boolean locked) {
    for(int i = 0; i<tables.length; i++) {
      tables[i].setColumnLock(columnIndex, locked);
    }
  }
  
  /**
   * Sets the lock flag for multiple columns.  Previously set locks are cleared.
   *
   * @param locked   boolean array
   */
  public void setColumnLocks(boolean[] locked) {
    for(int i = 0; i<tables.length; i++) {
        tables[i].setColumnLocks(locked);
    }
  }

  /**
   * Sets the editable property for the entire panel.
   *
   * @param editable true to allow editing of the cell values
   */
  public void setEditable(boolean _editable) {
    this.editable = _editable;
    for(int i = 0; i<tables.length; i++) {
      tables[i].setEditable(_editable);
    }
  }
  
  /**
   * Sets the transposed property for the array.
   * A transposed array switches its row and column values in the display.
   *
   * @param transposed  
   */
  public void setTransposed(boolean transposed) {
	    for(int i = 0; i<tables.length; i++) {
	      tables[i].setTransposed(transposed);
	    }
  }

  // -------------------------------
  // Getters
  // -------------------------------
  
  public int getNumColumns () { return tables[0].getColumnCount(); }
  
  //-------------------------------
  // Getters
  // -------------------------------
  
  
  /**
   * Refresh the data in all the tables.
   */
  public void refreshTable() {
    for(int i = 0; i<tables.length; i++) {
      tables[i].refreshTable();
    }
  }
  
  /**
   * Sets the <code>Timer</code>'s initial time delay (in milliseconds)
   * to wait after the timer is started
   * before firing the first event. 
   * @param delay
   */
  public void setRefreshDelay(int delay){
	for(int i = 0; i<tables.length; i++) {
	  tables[i].setRefreshDelay(delay);
	}
  }

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    this.removeAll(); // remove old elements Paco: be careful with this. If you use the ArrayPanel as a normal JPanel
    // and have added another component, it will be lost!
    this.setPreferredSize(new Dimension(400, 300));
    this.setLayout(new BorderLayout());
    scrollpane = new JScrollPane(tables[0]);
    if(tables.length>1) {
      // create spinner
      SpinnerModel model = new SpinnerNumberModel(0, 0, tables.length-1, 1);
      spinner = new JSpinner(model);
      JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
      editor.getTextField().setFont(tables[0].indexRenderer.getFont());
      spinner.setEditor(editor);
      spinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          int i = ((Integer) spinner.getValue()).intValue();
          scrollpane.setViewportView(tables[i]);
        }

      });
      Dimension dim = spinner.getMinimumSize();
      spinner.setMaximumSize(dim);
      add(scrollpane, BorderLayout.CENTER);
      JToolBar toolbar = new JToolBar();
      toolbar.setFloatable(false);
      toolbar.add(new JLabel(" index ")); //$NON-NLS-1$
      toolbar.add(spinner);
      toolbar.add(Box.createHorizontalGlue());
      add(toolbar, BorderLayout.NORTH);
    } else {
      scrollpane.createHorizontalScrollBar();
      add(scrollpane, BorderLayout.CENTER);
    }
    this.validate();                      // refresh the display
  }

  //_____________________________private constructors___________________________
  private void setArray(int[] array) {
    if (this.array instanceof int[]){// && ((int[])this.array).length==array.length) { // Paco
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
  }

  private void setArray(int[][] array) {
    if (this.array instanceof int[][]){ //&& ((int[][])this.array).length==array.length  && ((int[][])this.array)[0].length==array[0].length) { // Paco
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
  }

  private void setArray(int[][][] array) {
    /* Isn't this too much? Paco
    if (this.array instanceof int[][][] && ((int[][][])this.array).length==array.length ) {
      boolean quickChange = true;
      int[][][] thisArray = (int[][][]) this.array;
      for (int i=0; i<thisArray.length; i++) {
        if (thisArray[i].length!=array[i].length || thisArray[i][0].length!=array[i][0].length) {
          quickChange = false;
          break;
        }
      }
      if (quickChange) {
        for (int i=0; i<thisArray.length; i++) tables[i].tableModel.setArray(array[i]);
        return;
      }
    }
    */
    tables = new ArrayTable[array.length];
    for(int i = 0; i<tables.length; i++) {
      tables[i] = new ArrayTable(array[i]);
      tables[i].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    }
    createGUI();
  }

  private void setArray(double[] array) {
    if (this.array instanceof double[]){// ((double[])this.array).length==array.length) { // Paco added this
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
  }

  private void setArray(double[][] array) {
    if (this.array instanceof double[][]){ //&& ((double[][])this.array).length==array.length && ((double[][])this.array)[0].length==array[0].length) { // Paco
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
  }

  private void setArray(double[][][] array) {
    tables = new ArrayTable[array.length];
    for(int i = 0; i<tables.length; i++) {
      tables[i] = new ArrayTable(array[i]);
      tables[i].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    }
    createGUI();
  }

  private void setArray(String[] array) {
    if (this.array instanceof String[]){// && ((String[])this.array).length==array.length) { // Paco added this
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
  }

  private void setArray(String[][] array) {
    if (this.array instanceof String[][]){ //&& ((String[][])this.array).length==array.length && ((String[][])this.array)[0].length==array[0].length) { // Paco
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
  }

  private void setArray(String[][][] array) {
    tables = new ArrayTable[array.length];
    for(int i = 0; i<tables.length; i++) {
      tables[i] = new ArrayTable(array[i]);
      tables[i].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    }
    createGUI();
  }

  private void setArray(boolean[] array) {
    if (this.array instanceof boolean[]){// && ((boolean[])this.array).length==array.length) { // Paco added this
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
  }

  private void setArray(boolean[][] array) {
    if (this.array instanceof boolean[][]){ //&& ((boolean[][])this.array).length==array.length && ((boolean[][])this.array)[0].length==array[0].length) { // Paco
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
  }

  private void setArray(boolean[][][] array) {
    tables = new ArrayTable[array.length];
    for(int i = 0; i<tables.length; i++) {
      tables[i] = new ArrayTable(array[i]);
      tables[i].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    }
    createGUI();
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

