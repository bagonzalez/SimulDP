/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.opensourcephysics.tools.DataToolTable.WorkingDataset;
import org.opensourcephysics.display.*;

/**
 * This displays statistics and display properties of Datasets in a DataToolTable.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class DataToolPropsTable extends JTable {
	
	// static fields
  final static Color LIGHT_RED = new Color(255, 153, 153);
  
  // instance fields
  DataToolTable dataTable;
  PropsTableModel propsModel;
  DataToolTable.LabelRenderer labelRenderer;
  PropsRenderer propsRenderer;
  MarkerEditor markerEditor = new MarkerEditor();
	JDialog styleDialog;
	Dataset markerDataset = new Dataset();
	Dataset lineDataset = new Dataset();
	JButton closeButton, helpButton;
  String[] shapeNames;
  int[] shapeNumbers;
  JLabel shapeLabel, sizeLabel, patternLabel, sampleLabel;
  JCheckBox markerVisCheckbox, lineVisCheckbox;
  JButton markerColorButton, lineColorButton;
  JTextField patternField, sampleField;
  java.text.DecimalFormat sampleFormat;
	JDialog colorPopup;
  JSpinner shapeSpinner, sizeSpinner;
  int markerRow=0, lineRow=1, styleRow=2, axisRow=3;
  boolean labelDrawn;

  /**
   * Constructor.
   *
   * @param table the datatable
   */
  public DataToolPropsTable(DataToolTable table) {
    dataTable = table;
    propsModel = new PropsTableModel();
    init();
  }

  /**
   * Initializes the table.
   */
  protected void init() {
    dataTable.getColumnModel().addColumnModelListener(new
        TableColumnModelListener() {
      public void columnAdded(TableColumnModelEvent e) {/** empty block */}
      public void columnRemoved(TableColumnModelEvent e) {/** empty block */}
      public void columnSelectionChanged(ListSelectionEvent e) {/** empty block */}
      public void columnMarginChanged(ChangeEvent e) {refreshTable();}
      public void columnMoved(TableColumnModelEvent e) {refreshTable();}
    });
    // set and configure table model and header
    setModel(propsModel);
    setGridColor(Color.blue);
    setTableHeader(null); // no table header
    labelRenderer = dataTable.labelRenderer;
    propsRenderer = new PropsRenderer();
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    refreshCellWidths();
  }

  /**
   * Returns labels that describe the property rows.
   *
   * @return labels
   */
  private String[] getPropLabels() {
  	return new String[] {ToolsRes.getString("DataToolPropsTable.Label.Markers"), //$NON-NLS-1$
  				ToolsRes.getString("DataToolPropsTable.Label.Lines"), //$NON-NLS-1$
  				ToolsRes.getString("DataToolPropsTable.Label.Style"), //$NON-NLS-1$
  				ToolsRes.getString("DataToolPropsTable.Label.Axis")}; //$NON-NLS-1$
  }

  /**
   *  Refresh the data display in this table.
   */
  public void refreshTable() {
    Runnable refresh = new Runnable() {
      public synchronized void run() {
        tableChanged(new TableModelEvent(propsModel, TableModelEvent.HEADER_ROW));
        refreshCellWidths();
      }
    };
    if(SwingUtilities.isEventDispatchThread()) {
      refresh.run();
    } else {
      SwingUtilities.invokeLater(refresh);
    }
  }

  public void addColumn(TableColumn aColumn) {
    if (aColumn.getHeaderValue() == null) {
  int modelColumn = aColumn.getModelIndex();
  String columnName = getModel().getColumnName(modelColumn);
        aColumn.setHeaderValue(columnName);
    }
    getColumnModel().addColumn(aColumn);
}

  /**
   *  Refresh the cell widths in the table.
   */
  public void refreshCellWidths() {
    // set width of columns
  	if (getColumnCount() != dataTable.getColumnCount()) return;
    for (int i = 0; i < getColumnCount(); i++) {
    	TableColumn propColumn = getColumnModel().getColumn(i);
      TableColumn dataColumn = dataTable.getColumnModel().getColumn(i);
      propColumn.setMaxWidth(dataColumn.getWidth());
      propColumn.setMinWidth(dataColumn.getWidth());
      propColumn.setWidth(dataColumn.getWidth());
    }
  }

  /**
   *  Refresh the GUI.
   */
  public void refreshGUI() {
  	refreshLabelColumnWidth();
  	tableChanged(null);
    refreshTable();
  }

  /**
   *  Refresh the label column width.
   */
  public void refreshLabelColumnWidth() {
  	// set label column width
    int w = 40;
    String[] labels = getPropLabels();
    for (int i = 0; i < labels.length; i++) {
    	JLabel label = new JLabel(labels[i]);
    	label.setFont(labelRenderer.getFont());
      w = Math.max(w, label.getMinimumSize().width);
    }
    dataTable.setLabelColumnWidth(w+5);
  }

  /**
   * Returns the renderer for a cell specified by row and column.
   *
   * @param row the row number
   * @param column the column number
   * @return the cell renderer
   */
  public TableCellRenderer getCellRenderer(int row, int column) {
    int i = dataTable.convertColumnIndexToModel(column);
    if (i==0) return labelRenderer;
    return propsRenderer;
  }

  /**
   * Returns the editor for a cell specified by row and column.
   *
   * @param row the row number
   * @param column the column number
   * @return the cell editor
   */
  public TableCellEditor getCellEditor(int row, int column) {
    if (row == styleRow) return markerEditor;
    return getDefaultEditor(Boolean.class);
  }

	public void setFont(Font font) {
		super.setFont(font);
		setRowHeight(font.getSize()+4);
		if (dataTable != null) refreshLabelColumnWidth();
	}
	
  protected JDialog getStyleDialog() {
  	if (styleDialog == null) {
      // create style dialog
      final Frame frame = JOptionPane.getFrameForComponent(dataTable);
      styleDialog = new JDialog(frame, false);
      closeButton = new JButton();
      closeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	styleDialog.setVisible(false);
        	markerEditor.stopCellEditing();
        }
      });
      helpButton = new JButton();
      helpButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	String tab = "      "; //$NON-NLS-1$
        	String nl = System.getProperty("line.separator", "/n"); //$NON-NLS-1$ //$NON-NLS-2$
        	JOptionPane.showMessageDialog(styleDialog,
        					ToolsRes.getString("DataTool.NumberFormat.Help.Message1")+nl+nl+ //$NON-NLS-1$
        					tab+ToolsRes.getString("DataTool.NumberFormat.Help.Message2")+nl+ //$NON-NLS-1$
        					tab+ToolsRes.getString("DataTool.NumberFormat.Help.Message3")+nl+ //$NON-NLS-1$
        					tab+ToolsRes.getString("DataTool.NumberFormat.Help.Message4")+nl+ //$NON-NLS-1$
        					tab+ToolsRes.getString("DataTool.NumberFormat.Help.Message5")+nl+nl+ //$NON-NLS-1$
        					ToolsRes.getString("DataTool.NumberFormat.Help.Message6")+" \"12.34567\"",  //$NON-NLS-1$ //$NON-NLS-2$
        					ToolsRes.getString("DataTool.NumberFormat.Help.Title"),  //$NON-NLS-1$
        					JOptionPane.INFORMATION_MESSAGE);
        }
      });
      // create shape data and action
      shapeNames = new String[] {
        ToolsRes.getString("Shape.Circle"), //$NON-NLS-1$
        ToolsRes.getString("Shape.Square"), //$NON-NLS-1$
        ToolsRes.getString("Shape.Pixel"), //$NON-NLS-1$
        ToolsRes.getString("Shape.Bar"), //$NON-NLS-1$
        ToolsRes.getString("Shape.Post") //$NON-NLS-1$
      };
      shapeNumbers = new int[] {
        Dataset.CIRCLE, Dataset.SQUARE, Dataset.PIXEL,
        Dataset.BAR, Dataset.POST
      };
      // create shape spinner
      SpinnerModel model = new SpinnerListModel(shapeNames);
      shapeSpinner = new JSpinner(model) {
      	public Dimension getPreferredSize() {
      		Dimension dim = super.getPreferredSize();
      		dim.height = markerColorButton.getPreferredSize().height;
      		dim.width += 2;
      		return dim;
      	}
      };
      shapeSpinner.setToolTipText(ToolsRes.getString("Spinner.MarkerShape.ToolTip")); //$NON-NLS-1$
      shapeSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          String shape = shapeSpinner.getValue().toString();
          for(int i = 0;i<shapeNames.length;i++) {
            if(shapeNames[i].equals(shape)) {
      	    	WorkingDataset working = dataTable.workingMap.get(styleDialog.getName());
            	if (working != null) {
            		dataTable.dataToolTab.tabChanged = true;
            		working.setMarkerShape(shapeNumbers[i]);
            		markerDataset.setMarkerShape(shapeNumbers[i]);
            		styleDialog.repaint();
              	frame.repaint();
            	}
            }
          }
        }
      });
      // create size spinner
      SpinnerModel sizemodel = new SpinnerNumberModel(2, 1, 6, 1);
      sizeSpinner = new JSpinner(sizemodel) {
      	public Dimension getPreferredSize() {
      		return shapeSpinner.getPreferredSize();
      	}
      };
      sizeSpinner.setToolTipText(ToolsRes.getString("Spinner.MarkerSize.ToolTip")); //$NON-NLS-1$
      sizeSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          int size = ((Integer)sizeSpinner.getValue()).intValue();
  	    	WorkingDataset working = dataTable.workingMap.get(styleDialog.getName());
        	if (working != null) {
        		dataTable.dataToolTab.tabChanged = true;
        		working.setMarkerSize(size);
        		markerDataset.setMarkerSize(size);
        		styleDialog.repaint();
          	frame.repaint();
        	}
        }
      });
      // create checkboxes
      markerVisCheckbox = new JCheckBox(ToolsRes.getString(
      			"DataToolPropsTable.Dialog.Checkbox.Visible")); //$NON-NLS-1$
      markerVisCheckbox.setHorizontalAlignment(SwingConstants.CENTER);
      markerVisCheckbox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
  	    	WorkingDataset working = dataTable.workingMap.get(styleDialog.getName());
        	if (working != null) {
        		dataTable.dataToolTab.tabChanged(true);
        		working.setMarkersVisible(markerVisCheckbox.isSelected());
        		styleDialog.repaint();
          	frame.repaint();
        	}
        }
      });
      lineVisCheckbox = new JCheckBox(ToolsRes.getString(
					"DataToolPropsTable.Dialog.Checkbox.Visible")); //$NON-NLS-1$
      lineVisCheckbox.setHorizontalAlignment(SwingConstants.CENTER);
      lineVisCheckbox.addActionListener(new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
  	    	WorkingDataset working = dataTable.workingMap.get(styleDialog.getName());
			  	if (working != null) {
        		dataTable.dataToolTab.tabChanged(true);
			  		working.setConnected(lineVisCheckbox.isSelected());
			  		styleDialog.repaint();
			    	frame.repaint();
			  	}
			  }
			});
      // create marker and line plots
    	final DrawingPanel markerPlot = new DrawingPanel() {
    		public Dimension getPreferredSize() {
    			Dimension dim = markerColorButton.getPreferredSize();
    			dim.width -= 20;
      		return dim;
    		}
    	};
      markerPlot.setShowCoordinates(false);
    	markerPlot.setBorder(BorderFactory.createEtchedBorder());
			markerPlot.setBackground(Color.white);
			markerPlot.setAntialiasShapeOn(true);
			markerDataset.append(0, 1);
			markerPlot.addDrawable(markerDataset);
      // create line plot
    	DrawingPanel linePlot = new DrawingPanel() {
    		public Dimension getPreferredSize() {
      		return markerPlot.getPreferredSize();
    		}
    	};
      linePlot.setShowCoordinates(false);
      linePlot.setBorder(BorderFactory.createEtchedBorder());
      linePlot.setBackground(Color.white);
      linePlot.setAntialiasShapeOn(true);
			lineDataset.append(-1, 1);
			lineDataset.append(1, -1);
			lineDataset.setMarkerShape(Dataset.NO_MARKER);
			lineDataset.setConnected(true);
      linePlot.addDrawable(lineDataset);
      // create labels
      shapeLabel = new JLabel(ToolsRes.getString("DataToolPropsTable.Dialog.Label.Shape")); //$NON-NLS-1$
      sizeLabel = new JLabel(ToolsRes.getString("DataToolPropsTable.Dialog.Label.Size")); //$NON-NLS-1$
      patternLabel = new JLabel(ToolsRes.getString("DataToolPropsTable.Dialog.Label.Pattern")); //$NON-NLS-1$
      sampleLabel = new JLabel(ToolsRes.getString("DataToolPropsTable.Dialog.Label.Sample")); //$NON-NLS-1$
      // create text fields
      patternField = new JTextField(6);
      patternField.setAction(new AbstractAction() {
			  public void actionPerformed(ActionEvent e) {
			  	//double number = 12.34567;
			  	String pattern = patternField.getText();
			  	int i = pattern.indexOf("0e0"); //$NON-NLS-1$
			  	if (i > -1) {
			  		pattern = pattern.substring(0, i)+"0E0"+pattern.substring(i+3); //$NON-NLS-1$
			  	}
			  	try {
						showNumberFormatAndSample(pattern);
						String colName = styleDialog.getName();
						dataTable.setFormatPattern(colName, pattern);
						dataTable.refreshTable();
        		dataTable.dataToolTab.tabChanged(true);
					} catch (RuntimeException ex) {
          	patternField.setBackground(LIGHT_RED);
					}
			  }
			});
      patternField.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if(e.getKeyCode()==KeyEvent.VK_ENTER) {
          	patternField.setBackground(Color.white);
          } else {
          	patternField.setBackground(Color.yellow);
          }
        }
      });
      patternField.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
        	patternField.setBackground(Color.white);
        	patternField.getAction().actionPerformed(null);
        }
      });
      sampleField = new JTextField(6);
      sampleField.setEditable(false);
      java.text.NumberFormat format = java.text.NumberFormat.getNumberInstance();
      if (format instanceof java.text.DecimalFormat) {
      	sampleFormat = (java.text.DecimalFormat)format;
      }
      // create color chooser
      final JColorChooser cc = new JColorChooser();
      cc.getSelectionModel().addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          Color color = cc.getColor();
  	    	WorkingDataset working = dataTable.workingMap.get(styleDialog.getName());
          if (working != null) {
        		dataTable.dataToolTab.tabChanged = true;
          	if (colorPopup.getName().equals("marker")) { //$NON-NLS-1$
            	working.setColor(color, working.getLineColor());
          	}
          	else {
            	working.setColor(working.getEdgeColor(), color);
          	}
        		markerDataset.setMarkerColor(working.getFillColor(), working.getEdgeColor());
        		lineDataset.setLineColor(working.getLineColor());
        		colorPopup.setVisible(false);
        		styleDialog.repaint();
          	frame.repaint();
          }
        }
      });
      // create color popup, action and buttons
	  	colorPopup = new JDialog(frame, false);
	  	colorPopup.setUndecorated(true);
	  	colorPopup.getContentPane().add(cc.getChooserPanels()[0]);
	  	colorPopup.pack();
      ActionListener colorAction = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
			  	JButton b = (JButton)e.getSource();
			  	colorPopup.setName(b == markerColorButton? "marker": "line"); //$NON-NLS-1$ //$NON-NLS-2$
			  	Point loc = b.getLocationOnScreen();
			  	colorPopup.setLocation(loc.x, loc.y+b.getSize().height);
			  	colorPopup.setVisible(true);
			  }
			};
      markerColorButton = new JButton(ToolsRes.getString(
      			"DataToolPropsTable.Dialog.Button.Color")); //$NON-NLS-1$
      markerColorButton.addActionListener(colorAction);
      lineColorButton = new JButton(ToolsRes.getString(
					"DataToolPropsTable.Dialog.Button.Color")); //$NON-NLS-1$
			lineColorButton.addActionListener(colorAction);
      // assemble dialog
			JPanel contentPane = new JPanel(new BorderLayout());
      Box box = Box.createVerticalBox();
      contentPane.add(box);
      // marker properties
      Box markerBox = Box.createVerticalBox();
      box.add(markerBox);
      markerBox.setBorder(BorderFactory.createTitledBorder(
      			ToolsRes.getString("DataToolPropsTable.Dialog.Label.Markers"))); //$NON-NLS-1$
      JPanel markerNorth = new JPanel(new GridLayout());
      markerBox.add(markerNorth);
      JPanel markerPlotPanel = new JPanel();
      markerPlotPanel.add(markerPlot);
      markerNorth.add(markerPlotPanel);
	    JPanel markerButtonPanel = new JPanel();
	    markerButtonPanel.add(markerColorButton);
      markerNorth.add(markerButtonPanel);
      markerNorth.add(markerVisCheckbox);
      JPanel markerCenter = new JPanel(new GridLayout());
      markerBox.add(markerCenter);
	    JPanel sizePanel = new JPanel();
	    sizePanel.add(sizeLabel);
	    sizePanel.add(sizeSpinner);
	    markerCenter.add(sizePanel);
	    JPanel shapePanel = new JPanel();
	    shapePanel.add(shapeLabel);
	    shapePanel.add(shapeSpinner);
	    markerCenter.add(shapePanel);
	    // line properties
      Box lineBox = Box.createVerticalBox();
      box.add(lineBox);
      lineBox.setBorder(BorderFactory.createTitledBorder(
      			ToolsRes.getString("DataToolPropsTable.Dialog.Label.Lines"))); //$NON-NLS-1$
      JPanel lineNorth = new JPanel(new GridLayout());
      lineBox.add(lineNorth);
      JPanel linePlotPanel = new JPanel();
      linePlotPanel.add(linePlot);
      lineNorth.add(linePlotPanel);
	    JPanel lineButtonPanel = new JPanel();
	    lineButtonPanel.add(lineColorButton);
	    lineNorth.add(lineButtonPanel);
	    lineNorth.add(lineVisCheckbox);
	    // format properties
	    if (sampleFormat != null) {
	      Box formatBox = Box.createVerticalBox();
	      box.add(formatBox);
	      formatBox.setBorder(BorderFactory.createTitledBorder(
	      			ToolsRes.getString("DataToolPropsTable.Dialog.Label.Format"))); //$NON-NLS-1$
	      JPanel formatNorth = new JPanel(new GridLayout());
		    formatBox.add(formatNorth);
		    JPanel patternPanel = new JPanel();
		    patternPanel.add(patternLabel);
		    patternPanel.add(patternField);
		    formatNorth.add(patternPanel);
		    JPanel samplePanel = new JPanel();
		    samplePanel.add(sampleLabel);
		    samplePanel.add(sampleField);
		    formatNorth.add(samplePanel);
	    }
	    // close button
	    JPanel buttonPanel = new JPanel();
	    buttonPanel.add(helpButton);
	    buttonPanel.add(closeButton);
	    box.add(buttonPanel);
      styleDialog.setContentPane(contentPane);
      int fontLevel = dataTable.dataToolTab.dataTool.fontLevel;
      FontSizer.setFonts(styleDialog, fontLevel);
      styleDialog.pack();
     	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int x = (dim.width - styleDialog.getWidth()) / 2;
	    Point p = this.getLocationOnScreen();
	    int y = Math.max(0, p.y-styleDialog.getHeight());
	    styleDialog.setLocation(x, y);
  	}
  	return styleDialog;
  }
  
  private void showNumberFormatAndSample(String pattern) {
  	if (pattern == null || pattern.equals("")) { //$NON-NLS-1$
  		TableCellRenderer renderer = dataTable.getDefaultRenderer(Double.class);
  		Component c = renderer.getTableCellRendererComponent(dataTable, 12.34567, 
  						false, false, 0, 0);
  		if (c instanceof JLabel) {
  			String text = ((JLabel)c).getText();
  			sampleField.setText(text);
  		}
  	}
  	else {
  		sampleFormat.applyPattern(pattern);
  		sampleField.setText(sampleFormat.format(12.34567));  	
  	}
		patternField.setText(pattern);
  }

  /**
   * A class to provide model data for this table.
   */
  class PropsTableModel extends AbstractTableModel {

    public String getColumnName(int col) {
      return dataTable.getColumnName(col);
    }

    public int getRowCount() {
      return 4;
    }

    public int getColumnCount() {
      return dataTable.getModel().getColumnCount();
    }

    public Object getValueAt(int row, int col) {
      int labelCol = dataTable.convertColumnIndexToView(0);
    	if (col == labelCol) return getPropLabels()[row];
      int xCol = labelCol == 0? 1: 0;
    	if (row == axisRow) {
      	if (col == xCol) return ToolsRes.getString("DataToolPropsTable.Axis.Horizontal"); //$NON-NLS-1$
      	return ToolsRes.getString("DataToolPropsTable.Axis.Vertical"); //$NON-NLS-1$
    	}
     	String name = getColumnName(col);
 	    WorkingDataset data = dataTable.workingMap.get(name);
    	if (data == null) data = dataTable.getWorkingData(name);
    	if (row == markerRow) {
      	if (col == 0) return Boolean.FALSE;
       	return new Boolean(data.isMarkersVisible());
    	}
    	if (row == lineRow) {
      	if (col == 0) return Boolean.FALSE;
      	return new Boolean(data.isConnected());
    	}
    	// row == styleRow
    	if (col == xCol) return null;
    	return data;
    }

    public boolean isCellEditable(int row, int col) {
    	if (row == axisRow) return false;
      int labelCol = dataTable.convertColumnIndexToView(0);
      int xCol = labelCol == 0? 1: 0;
    	if (col == labelCol || col == xCol) return false;
      return true;
    }

    public Class<?> getColumnClass(int c) {
      return getValueAt(0, c).getClass();
    }

		// changes the value of a cell
		public void setValueAt(Object value, int row, int col) {
			if (value instanceof Boolean) {
				dataTable.dataToolTab.tabChanged(true);
				boolean selected = ((Boolean)value).booleanValue();
				String name = getColumnName(col);
	    	WorkingDataset working = dataTable.workingMap.get(name);
				if (row == markerRow) working.setMarkersVisible(selected);
				else if (row == lineRow) working.setConnected(selected);
	      int labelCol = dataTable.convertColumnIndexToView(0);
	      int xCol = labelCol == 0? 1: 0;
	      String xName = getColumnName(xCol);
	 	    if (working.getXSource() == null
	      		|| !working.getXSource().getYColumnName().equals(xName)) {
	      	working.setXSource((dataTable.workingMap.get(xName)).getYSource());
	      }
				firePropertyChange("display", null, name); //$NON-NLS-1$
			}
		}

  }

  /**
   * A class to render checkboxes and dataset markers.
   */
  class PropsRenderer implements TableCellRenderer {
		JPanel panel = new JPanel(new GridLayout());
		JCheckBox checkbox = new JCheckBox();
		DrawingPanel plot = new DrawingPanel();
		Dataset markerset = new Dataset();
		Dataset lineset = new Dataset();

		// Constructor
		public PropsRenderer() {
			checkbox.setHorizontalAlignment(SwingConstants.CENTER);
			checkbox.setBackground(Color.white);
			plot.setBackground(Color.white);
			plot.setAntialiasShapeOn(true);
			markerset.append(0, 1);
			lineset.append(-1, 2);
			lineset.append(1, 0);
			lineset.setMarkerShape(Dataset.NO_MARKER);
			lineset.setConnected(true);
			plot.addDrawable(markerset);
			plot.addDrawable(lineset);
      checkbox.setBackground(Color.white);
		}

		// Returns a component for the specified cell.
		public Component getTableCellRendererComponent(JTable table, Object value,
						boolean isSelected, boolean hasFocus, int row, int col) {
			panel.remove(checkbox);
			panel.remove(plot);
			int labelCol = dataTable.convertColumnIndexToView(0);
			int xCol = labelCol == 0 ? 1 : 0;
			int yCol = labelCol < 2 ? 2 : 1;
			Color color = col == xCol ? DataToolTable.xAxisColor
					: col == yCol ? DataToolTable.yAxisColor : Color.white;
			panel.setBackground(row != axisRow ? Color.white : color);
			if (value == null)
				return panel;
			if (value instanceof String) {
				Component c = getDefaultRenderer(String.class)
						.getTableCellRendererComponent(DataToolPropsTable.this, value,
								false, false, 0, 0);
				if (c instanceof JLabel) {
					JLabel label = (JLabel) c;
					label.setHorizontalAlignment(SwingConstants.CENTER);
					label.setBackground(color);
					label.setBorder(new CellBorder(new Color(240, 240, 240)));
					return label;
				}
			}
			if (value instanceof WorkingDataset) {
				WorkingDataset working = (WorkingDataset)value;
				markerset.setMarkerColor(working.getFillColor(), working.getEdgeColor());
				markerset.setMarkerSize(working.getMarkerSize());
				markerset.setMarkerShape(working.markerType);
				lineset.setLineColor(working.getLineColor());
				Boolean markerVis = (Boolean)propsModel.getValueAt(markerRow, col);
				Boolean lineVis = (Boolean)propsModel.getValueAt(lineRow, col);
				plot.clear();
				if (markerVis.booleanValue()) plot.addDrawable(markerset);
				if (lineVis.booleanValue()) plot.addDrawable(lineset);
				panel.add(plot);
				panel.setToolTipText(ToolsRes.getString(
							"DataToolPropsTable.Style.Tooltip")); //$NON-NLS-1$
			}
			else { // value is Boolean
				if (col == xCol) return panel;
				checkbox.setSelected(((Boolean)value).booleanValue());
				checkbox.setEnabled(propsModel.isCellEditable(row, col));
				panel.add(checkbox);
				if (row == markerRow)
					panel.setToolTipText(ToolsRes.getString(
						"DataToolPropsTable.Markers.Tooltip")); //$NON-NLS-1$
				else
					panel.setToolTipText(ToolsRes.getString(
						"DataToolPropsTable.Lines.Tooltip")); //$NON-NLS-1$
			}
	    panel.setBorder(new CellBorder(new Color(240, 240, 240)));
			return panel;
		}
	}


  /**
   * A class to edit dataset markers.
   */
  class MarkerEditor extends AbstractCellEditor implements TableCellEditor {

		// Gets the component to be displayed while editing.
		public Component getTableCellEditorComponent(JTable table, Object value,
						boolean isSelected, int row, int col) {
			JDialog dialog = getStyleDialog();
    	String name = getColumnName(col);
 	    WorkingDataset working = dataTable.workingMap.get(name);
    	markerDataset.setMarkerColor(
  					working.getFillColor(),working.getEdgeColor());
  		markerDataset.setMarkerSize(working.getMarkerSize());
  		markerDataset.setMarkerShape(working.markerType);
  		lineDataset.setLineColor(working.getLineColor());
    	helpButton.setText(ToolsRes.getString("Tool.Button.Help")); //$NON-NLS-1$
    	closeButton.setText(ToolsRes.getString("Button.OK")); //$NON-NLS-1$
			dialog.setName(null); // set to null to prevent changes when setting
	    sizeSpinner.setToolTipText(ToolsRes.getString("Spinner.MarkerSize.ToolTip")); //$NON-NLS-1$
      sizeSpinner.setValue(new Integer(working.getMarkerSize()));
	    shapeSpinner.setToolTipText(ToolsRes.getString("Spinner.MarkerShape.ToolTip")); //$NON-NLS-1$
	    shapeNames = new String[] {
	      ToolsRes.getString("Shape.Circle"), //$NON-NLS-1$
	      ToolsRes.getString("Shape.Square"), //$NON-NLS-1$
	      ToolsRes.getString("Shape.Pixel"), //$NON-NLS-1$
	      ToolsRes.getString("Shape.Bar"), //$NON-NLS-1$
	      ToolsRes.getString("Shape.Post") //$NON-NLS-1$
	    };
	    SpinnerModel model = new SpinnerListModel(shapeNames) {
    		public Object getNextValue() {
    			Object value = super.getNextValue();
    			if (value == null && getList().size() > 0) value = getList().get(0);
    			return value;
    		}
    		public Object getPreviousValue() {
    			Object value = super.getPreviousValue();
    			int n = getList().size();
    			if (value == null && n > 0) value = getList().get(n - 1);
    			return value;
    		}
	    };
	    shapeSpinner.setModel(model);
      for(int i = 0;i<shapeNumbers.length;i++) {
        if(shapeNumbers[i] == working.markerType) {
          shapeSpinner.setValue(shapeNames[i]);
        }
      }
      markerVisCheckbox.setSelected(working.isMarkersVisible());
      lineVisCheckbox.setSelected(working.isConnected());
      String pattern = dataTable.getFormatPattern(name);
      showNumberFormatAndSample(pattern);
			dialog.setName(name);
     	String var = TeXParser.removeSubscripting(name);
			String title = ToolsRes.getString("DataToolPropsTable.Dialog.Title"); //$NON-NLS-1$
			dialog.setTitle(title+" \""+var+"\""); //$NON-NLS-1$ //$NON-NLS-2$
      dialog.pack();
      // a hack to make sure spinners are displayed correctly...
      Dimension dim = dialog.getSize();
      dim.width += 6;
      dialog.setSize(dim);
			dialog.setVisible(true);
			return null;
		}

		// Called when editing is completed.
		public Object getCellEditorValue() {
			return null;
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
