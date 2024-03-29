/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.text.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.numerics.*;

/**
 * A panel that displays and controls functional curve fits to a Dataset.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class DatasetCurveFitter extends JPanel {
  // instance fields
  FunctionTool fitBuilder;
  Dataset dataset;               // the data to be fit
  KnownFunction fit;             // the function to fit to the data
  HessianMinimize hessian = new HessianMinimize();
  LevenbergMarquardt levmar = new LevenbergMarquardt();
  FunctionDrawer drawer;
  Color color = Color.MAGENTA;
  JButton colorButton, closeButton;
  JCheckBox autofitCheckBox;
  String[] fitNames;
  JLabel fitLabel, eqnLabel, rmsLabel;
  JComboBox fitDropDown;
  JTextField eqnField;
  NumberField rmsField;
  ParamTableModel paramModel;
  JTable paramTable;
  ParamCellRenderer cellRenderer;
  SpinCellEditor spinCellEditor; // uses number-crawler spinner
  Map<String, KnownFunction> namedFits = new HashMap<String, KnownFunction>();
  int fitNumber = 1;
  JButton fitBuilderButton, newFitButton, deleteFitButton;
  boolean refreshing = false;
  JSplitPane splitPane;
  JDialog colorDialog;
  String dataSourceName;
  int fontLevel;

  /**
   * Constructs a DatasetCurveFitter for the specified Dataset.
   *
   * @param data the dataset
   */
  public DatasetCurveFitter(Dataset data) {
    dataset = data;
    createGUI();
    fit(fit);
  }

  /**
   * Gets the function drawer.
   *
   * @return the drawer
   */
  public Drawable getDrawer() {
    return drawer;
  }

  /**
   * Gets the data.
   *
   * @return the dataset
   */
  public Dataset getData() {
    return dataset;
  }

  /**
   * Sets the dataset.
   *
   * @param data the dataset
   */
  public void setData(Dataset data) {
    dataset = data;
    fit(fit);
  }

  /**
   * Sets the color.
   *
   * @param newColor the color
   */
  public void setColor(Color newColor) {
    color = newColor;
    if(drawer!=null) {
      drawer.setColor(newColor);
      LookAndFeel currentLF = UIManager.getLookAndFeel();
      boolean nimbus = currentLF.getClass().getName().indexOf("Nimbus")>-1; //$NON-NLS-1$
      if(nimbus) {
        colorButton.setIcon(new ColorIcon(color, 12, DataTool.buttonHeight-8));
      } else {
        colorButton.setBackground(color);
      }
      firePropertyChange("changed", null, null);                            //$NON-NLS-1$
    }
  }

  /**
   * Fits a fit function to the current data.
   *
   * @param fit the function to fit
   */
  public void fit(Function fit) {
    if(drawer==null) {
      selectFit((String) fitDropDown.getSelectedItem());
    }
    if((fit==null)||(dataset==null)) {
      if(fit instanceof KnownFunction) {
        eqnField.setText("y = "+                     //$NON-NLS-1$
          ((KnownFunction) fit).getExpression("x")); //$NON-NLS-1$
      }
      fitBuilderButton.setEnabled(false);
      autofitCheckBox.setEnabled(false);
      spinCellEditor.stopCellEditing();
      paramTable.setEnabled(false);
      rmsField.setText("");                          //$NON-NLS-1$
      return;
    }
    fitBuilderButton.setEnabled(true);
    autofitCheckBox.setEnabled(true);
    paramTable.setEnabled(true);
    double[] x = dataset.getValidXPoints();
    double[] y = dataset.getValidYPoints();
    double devSq = 0;
    // autofit if checkbox is selected
    double[] prevParams = null;
    // get deviation before fitting
    double prevDevSq = getDevSquared(fit, x, y);
    if(autofitCheckBox.isSelected()) {
      if(fit instanceof KnownPolynomial) {
        KnownPolynomial poly = (KnownPolynomial) fit;
        poly.fitData(x, y);
      } else if(fit instanceof UserFunction) {
        // use HessianMinimize to autofit user function 
        UserFunction f = (UserFunction) fit;
        double[] params = new double[f.getParameterCount()];
        // can't autofit if no parameters
        if(params.length>0) {
          MinimizeUserFunction minFunc = new MinimizeUserFunction(f, x, y);
          prevParams = new double[params.length];
          for(int i = 0; i<params.length; i++) {
            params[i] = prevParams[i] = f.getParameterValue(i);
          }
          double tol = 1.0E-6;
          int iterations = 20;
          hessian.minimize(minFunc, params, iterations, tol);
          // get deviation after minimizing
          devSq = getDevSquared(fit, x, y);
          // restore previous parameters and try Levenberg-Marquardt if Hessian fit is worse
          if(devSq>prevDevSq) {
            for(int i = 0; i<prevParams.length; i++) {
              f.setParameterValue(i, prevParams[i]);
            }
            levmar.minimize(minFunc, params, iterations, tol);
            // get deviation after minimizing
            devSq = getDevSquared(fit, x, y);
          }
          // restore previous parameters and deviation if new fit still worse
          if(devSq>prevDevSq) {
            for(int i = 0; i<prevParams.length; i++) {
              f.setParameterValue(i, prevParams[i]);
            }
            devSq = prevDevSq;
            autofitCheckBox.setSelected(false);
            Toolkit.getDefaultToolkit().beep();
          }
        }
      }
      drawer.functionChanged = true;
      paramTable.repaint();
    }
    if(devSq==0) {
      devSq = getDevSquared(fit, x, y);
    }
    rmsField.setValue(Math.sqrt(devSq/x.length));
    firePropertyChange("fit", null, null); //$NON-NLS-1$
  }

  /**
   * Adds a user fit function.
   *
   * @param panel the user fit function panel
   */
  public void addUserFit(FitFunctionPanel panel) {
    String name = panel.getName();
    UserFunction fit = panel.getFitFunction();
    namedFits.put(name, fit);
    fitDropDown.insertItemAt(name, 0);
    getFitBuilder().addPanel(name, panel);
    fitBuilder.setSelectedPanel(name);
    fitDropDown.setSelectedItem(name);
    firePropertyChange("changed", null, null); //$NON-NLS-1$
  }

  /**
   * Gets the selected fit name.
   *
   * @return the name of the fit
   */
  public String getSelectedFitName() {
    for(Iterator<String> it = namedFits.keySet().iterator(); it.hasNext(); ) {
      String name = it.next().toString();
      if(namedFits.get(name)==fit) {
        return name;
      }
    }
    return null;
  }

  /**
   * Sets the data source name.
   *
   * @param name the name of the data source
   */
  public void setDataSourceName(String name) {
    dataSourceName = name;
    if(fitBuilder!=null) {
      fitBuilder.setTitle(ToolsRes.getString("DatasetCurveFitter.FitBuilder.Title") //$NON-NLS-1$
                          +": "+dataSourceName);                                    //$NON-NLS-1$
    }
  }

  // _______________________ protected & private methods __________________________

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    setLayout(new BorderLayout());
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setResizeWeight(0.5);
    splitPane.setDividerSize(4);
    // create autofit checkbox
    autofitCheckBox = new JCheckBox("", true); //$NON-NLS-1$
    autofitCheckBox.setSelected(false);
    autofitCheckBox.setOpaque(false);
    autofitCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        spinCellEditor.stopCellEditing();
        paramTable.clearSelection();
        fit(fit);
        firePropertyChange("changed", null, null); //$NON-NLS-1$
      }

    });
    fitNames = new String[] {ToolsRes.getString("Function.Poly1.Name"),            //$NON-NLS-1$
                             ToolsRes.getString("Function.Poly2.Name"),            //$NON-NLS-1$
                             ToolsRes.getString("Function.Poly3.Name")};           //$NON-NLS-1$
    // create labels
    fitLabel = new JLabel(ToolsRes.getString("DatasetCurveFitter.Label.FitName")); //$NON-NLS-1$
    fitLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    eqnLabel = new JLabel(ToolsRes.getString("DatasetCurveFitter.Label.Equation")); //$NON-NLS-1$
    eqnLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    rmsLabel = new JLabel();
    rmsLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    fitDropDown = new JComboBox(fitNames) {
      public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.height = DataTool.buttonHeight-2;
        return dim;
      }

    };
    fitDropDown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(!fitDropDown.getSelectedItem().equals(getFitName())) {
          firePropertyChange("changed", null, null); //$NON-NLS-1$
        }
        selectFit((String) fitDropDown.getSelectedItem());
      }

    });
    // create equation field
    eqnField = new JTextField() {
      public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.height = DataTool.buttonHeight-2;
        return dim;
      }

    };
    eqnField.setEditable(false);
    eqnField.setEnabled(true);
    eqnField.setBackground(Color.white);
    // create dataBuilder button
    colorButton = DataTool.createButton(""); //$NON-NLS-1$
    colorButton.setToolTipText(ToolsRes.getString("DatasetCurveFitter.Button.Color.Tooltip")); //$NON-NLS-1$
    colorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JDialog dialog = getColorDialog();
        closeButton.setText(ToolsRes.getString("Button.OK"));                                  //$NON-NLS-1$
        dialog.setTitle(ToolsRes.getString("DatasetCurveFitter.Dialog.Color.Title"));          //$NON-NLS-1$
        dialog.setVisible(true);
      }

    });
    // create rms field
    rmsField = new NumberField(6) {
      public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.height = DataTool.buttonHeight-2;
        return dim;
      }

    };
    rmsField.setEditable(false);
    rmsField.setEnabled(true);
    rmsField.setBackground(Color.white);
    // create table
    cellRenderer = new ParamCellRenderer();
    spinCellEditor = new SpinCellEditor();
    paramModel = new ParamTableModel();
    paramTable = new ParamTable(paramModel);
    paramTable.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        // clear selection if pressed on the name column
        if(paramTable.getSelectedColumn()==0) {
          paramTable.clearSelection();
        }
      }

    });
    splitPane.setRightComponent(new JScrollPane(paramTable));
    add(splitPane, BorderLayout.CENTER);
    // create "define" button
    fitBuilderButton = DataTool.createButton(ToolsRes.getString("DatasetCurveFitter.Button.Define.Text")); //$NON-NLS-1$
    fitBuilderButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        autofitCheckBox.setSelected(false);
        if(fitBuilder==null) {
          fitBuilder = getFitBuilder();
          fitBuilder.setTitle(ToolsRes.getString("DatasetCurveFitter.FitBuilder.Title") //$NON-NLS-1$
                              +": "+dataSourceName);                                    //$NON-NLS-1$
          fitBuilder.customButtons[0].doClick();
        } else if(fit instanceof UserFunction) {
          fitBuilder.setSelectedPanel(getFitName());
        } else if(fitBuilder.getSelectedName()!=null) {
          fitDropDown.setSelectedItem(fitBuilder.getSelectedName());
        } else {
          fitBuilder.customButtons[0].doClick();
        }
        fitBuilder.setVisible(true);
      }

    });
    // assemble components
    JPanel fitPanel = new JPanel(new BorderLayout());
    splitPane.setLeftComponent(fitPanel);
    JToolBar fitBar = new JToolBar();
    fitBar.setFloatable(false);
    fitBar.setBorder(BorderFactory.createEtchedBorder());
    fitBar.add(fitLabel);
    fitBar.add(fitDropDown);
    fitBar.addSeparator();
    fitBar.add(fitBuilderButton);
    fitPanel.add(fitBar, BorderLayout.NORTH);
    JPanel eqnPanel = new JPanel(new BorderLayout());
    fitPanel.add(eqnPanel, BorderLayout.CENTER);
    JToolBar eqnBar = new JToolBar();
    eqnBar.setFloatable(false);
    eqnBar.setBorder(BorderFactory.createEtchedBorder());
    eqnBar.add(eqnLabel);
    eqnBar.add(eqnField);
    eqnBar.add(colorButton);
    eqnPanel.add(eqnBar, BorderLayout.NORTH);
    JPanel rmsPanel = new JPanel(new BorderLayout());
    eqnPanel.add(rmsPanel, BorderLayout.CENTER);
    JToolBar rmsBar = new JToolBar();
    rmsBar.setFloatable(false);
    rmsBar.setBorder(BorderFactory.createEtchedBorder());
    rmsBar.add(autofitCheckBox);
    rmsBar.addSeparator();
    rmsBar.add(rmsLabel);
    rmsBar.add(rmsField);
    rmsPanel.add(rmsBar, BorderLayout.NORTH);
    refreshGUI();
  }

  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
    autofitCheckBox.setText(ToolsRes.getString("Checkbox.Autofit.Label"));                           //$NON-NLS-1$
    rmsLabel.setText(ToolsRes.getString("DatasetCurveFitter.Label.RMSDeviation"));                   //$NON-NLS-1$
    fitBuilderButton.setText(ToolsRes.getString("DatasetCurveFitter.Button.Define.Text"));           //$NON-NLS-1$
    fitBuilderButton.setToolTipText(ToolsRes.getString("DatasetCurveFitter.Button.Define.Tooltip")); //$NON-NLS-1$
    if(fitBuilder!=null) {
      newFitButton.setText(ToolsRes.getString("DatasetCurveFitter.Button.NewFit.Text"));                 //$NON-NLS-1$
      newFitButton.setToolTipText(ToolsRes.getString("DatasetCurveFitter.Button.NewFit.Tooltip"));       //$NON-NLS-1$
      deleteFitButton.setText(ToolsRes.getString("DatasetCurveFitter.Button.DeleteFit.Text"));           //$NON-NLS-1$
      deleteFitButton.setToolTipText(ToolsRes.getString("DatasetCurveFitter.Button.DeleteFit.Tooltip")); //$NON-NLS-1$
    }
    paramTable.tableChanged(null);
    int n = fitDropDown.getItemCount();
    Object[] list = new Object[n];
    fitNames = new String[] {ToolsRes.getString("Function.Poly1.Name"),  //$NON-NLS-1$
                             ToolsRes.getString("Function.Poly2.Name"),  //$NON-NLS-1$
                             ToolsRes.getString("Function.Poly3.Name")}; //$NON-NLS-1$
    int k = n-fitNames.length;
    for(int i = 0; i<n; i++) {
      if(i<k) {
        list[i] = fitDropDown.getItemAt(i);
      } else {
        list[i] = fitNames[i-k];
      }
    }
    DefaultComboBoxModel model = new DefaultComboBoxModel(list);
    int i = fitDropDown.getSelectedIndex();
    fitDropDown.setModel(model);
    fitDropDown.setSelectedIndex(i);
    LookAndFeel currentLF = UIManager.getLookAndFeel();
    boolean nimbus = currentLF.getClass().getName().indexOf("Nimbus")>-1; //$NON-NLS-1$
    if(nimbus) {
      colorButton.setIcon(new ColorIcon(color, 12, DataTool.buttonHeight-8));
    } else {
      colorButton.setBackground(color);
    }
  }

  /**
   * Sets the font level.
   *
   * @param level the level
   */
  protected void setFontLevel(int level) {
    fontLevel = level;
    FontSizer.setFonts(this, fontLevel);
    if(fitBuilder!=null) {
      fitBuilder.setFontLevel(level);
    }
  }

  /**
   * Sets the value of a parameter.
   *
   * @param row the row number
   * @param value the value
   */
  protected void setParameterValue(int row, double value) {
    if(row<fit.getParameterCount()) {
      fit.setParameterValue(row, value);
    }
  }

  /**
   * Selects a named fit.
   */
  protected void selectFit(String name) {
    fit = namedFits.get(name);
    if(fit==null) {
      if(name.equals(ToolsRes.getString("Function.Poly1.Name"))) {        //$NON-NLS-1$
        fit = new KnownPolynomial(new double[] {0, 0});
      } else if(name.equals(ToolsRes.getString("Function.Poly2.Name"))) { //$NON-NLS-1$
        fit = new KnownPolynomial(new double[] {0, 0, 0});
      } else if(name.equals(ToolsRes.getString("Function.Poly3.Name"))) { //$NON-NLS-1$
        fit = new KnownPolynomial(new double[] {0, 0, 0, 0});
      }
      if(fit!=null) {
        namedFits.put(name, fit);
      }
    }
    if(fit!=null) {
      FunctionDrawer prev = drawer;
      drawer = new FunctionDrawer(fit);
      drawer.setColor(color);
      paramTable.tableChanged(null);
      // construct equation string
      String depVar = (dataset==null)? "y":                                           //$NON-NLS-1$
    	  TeXParser.removeSubscripting(dataset.getColumnName(1));
      String indepVar = (dataset==null)? "x":                                         //$NON-NLS-1$
    	  TeXParser.removeSubscripting(dataset.getColumnName(0));
      eqnField.setText(depVar+" = "+fit.getExpression(indepVar)); //$NON-NLS-1$
      firePropertyChange("drawer", prev, drawer);                 //$NON-NLS-1$
      fit(fit);
      if((fit instanceof UserFunction)&&(fitBuilder!=null)&&fitBuilder.isVisible()) {
        UserFunction f = (UserFunction) fit;
        fitBuilder.setSelectedPanel(f.getName());
      }
      revalidate();
    }
  }

  /**
   * Gets the fit builder.
   */
  protected FunctionTool getFitBuilder() {
    if(fitBuilder==null) {
      newFitButton = new JButton(ToolsRes.getString("DatasetCurveFitter.Button.NewFit.Text"));          //$NON-NLS-1$
      newFitButton.setToolTipText(ToolsRes.getString("DatasetCurveFitter.Button.NewFit.Tooltip"));      //$NON-NLS-1$
      newFitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // create new user function and select it 
          String name = fitBuilder.getUniqueName(ToolsRes.getString("DatasetCurveFitter.NewFit.Name")); //$NON-NLS-1$
          UserFunction f = new UserFunction(name);
          String colName = TeXParser.removeSubscripting(dataset.getColumnName(0));
          f.setExpression("0", new String[] {colName});                                                 //$NON-NLS-1$
          namedFits.put(name, f);
          fitDropDown.insertItemAt(name, 0);
          UserFunctionEditor editor = new UserFunctionEditor();
          editor.setMainFunctions(new UserFunction[] {f});
          FitFunctionPanel panel = new FitFunctionPanel(editor);
          fitBuilder.addPanel(f.getName(), panel);
          fitBuilder.setSelectedPanel(name);
          fitDropDown.setSelectedItem(name);
        }

      });
      deleteFitButton = new JButton(ToolsRes.getString("DatasetCurveFitter.Button.DeleteFit.Text")); //$NON-NLS-1$
      deleteFitButton.setToolTipText(ToolsRes.getString("DatasetCurveFitter.Button.DeleteFit.Tooltip")); //$NON-NLS-1$
      deleteFitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // remove selected fit and panel 
          String name = fitBuilder.getSelectedName();
          namedFits.remove(name);
          fitDropDown.removeItem(name);
          fitBuilder.removePanel(name);
          firePropertyChange("changed", null, null);                                                     //$NON-NLS-1$
        }

      });
      fitBuilder = new FunctionTool(this, new JButton[] {newFitButton, deleteFitButton});
      fitBuilder.setFontLevel(fontLevel);
      fitBuilder.addForbiddenNames(fitNames);
      fitBuilder.setHelpPath("fit_builder_help.html"); //$NON-NLS-1$
      JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) fitBuilder.spinner.getEditor();
      final JTextField field = editor.getTextField();
      field.setEditable(true);
      field.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if(e.getKeyCode()==KeyEvent.VK_ENTER) {
            String newName = field.getText();
            FunctionEditor editor = fitBuilder.selectedPanel.functionEditor;
            editor.tableModel.setValueAt(newName, 0, 0);
            field.setBackground(Color.white);
          } else {
            field.setBackground(Color.yellow);
          }
        }

      });
      fitBuilder.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
          if(refreshing) {
            return;
          }
          String prop = e.getPropertyName();
          if(!prop.equals("function")&&!prop.equals("panel")) { //$NON-NLS-1$ //$NON-NLS-2$
            return;
          }
          firePropertyChange("changed", null, null);            //$NON-NLS-1$
          FunctionPanel panel = fitBuilder.getPanel(fitBuilder.getSelectedName());
          deleteFitButton.setEnabled(panel!=null);
          if(panel instanceof FitFunctionPanel) {
            UserFunction fit = ((FitFunctionPanel) panel).getFitFunction();
            String name = fit.getName();
            if(prop.equals("function")) {                       //$NON-NLS-1$
              String prevName = (String) e.getOldValue();
              if(prevName!=null) {                              // fit name has changed
                namedFits.remove(prevName);
                namedFits.put(name, fit);
                for(int i = 0; i<fitDropDown.getItemCount(); i++) {
                  Object obj = fitDropDown.getItemAt(i);
                  if(obj.equals(prevName)) {
                    fitDropDown.insertItemAt(name, i);
                    fitDropDown.removeItem(prevName);
                    break;
                  }
                }
              }
            }
            if(!fitDropDown.getSelectedItem().equals(name)) {
              fitDropDown.setSelectedItem(name);
            }
            refreshGUI();
          }
        }

      });
    }
    return fitBuilder;
  }

  /**
   * Gets the total deviation squared between function and data
   */
  private double getDevSquared(Function f, double[] x, double[] y) {
    double total = 0;
    for(int i = 0; i<x.length; i++) {
      double next = f.evaluate(x[i]);
      double dev = (next-y[i]);
      total += dev*dev;
    }
    return total;
  }

  /**
   * Gets the name of the current fit
   */
  private String getFitName() {
    for(Iterator<String> it = namedFits.keySet().iterator(); it.hasNext(); ) {
      Object key = it.next();
      if(fit==namedFits.get(key)) {
        return key.toString();
      }
    }
    return null;
  }

  protected JDialog getColorDialog() {
    if(colorDialog==null) {
      // create color dialog
      final Frame frame = JOptionPane.getFrameForComponent(this);
      final JColorChooser cc = new JColorChooser();
      cc.getSelectionModel().addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          color = cc.getColor();
          setColor(color);
          frame.repaint();
        }

      });
      colorDialog = new JDialog(frame, false);
      closeButton = new JButton();
      closeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          colorDialog.setVisible(false);
        }

      });
      JPanel contentPane = new JPanel(new BorderLayout());
      JPanel buttonPanel = new JPanel();
      buttonPanel.add(closeButton);
      JPanel chooser = cc.getChooserPanels()[0];
      chooser.setBorder(BorderFactory.createEmptyBorder(2, 2, 12, 2));
      contentPane.add(chooser, BorderLayout.CENTER);
      contentPane.add(buttonPanel, BorderLayout.SOUTH);
      colorDialog.setContentPane(contentPane);
      colorDialog.pack();
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int x = (dim.width-colorDialog.getWidth())/2;
      Point p = this.getLocationOnScreen();
      int y = Math.max(0, p.y-colorDialog.getHeight());
      colorDialog.setLocation(x, y);
    }
    return colorDialog;
  }

  // _______________________ inner classes __________________________

  /**
   * A table to display and edit parameters.
   */
  class ParamTable extends JTable {
    /**
     * Constructor ParamTable
     * @param model
     */
    public ParamTable(ParamTableModel model) {
      super(model);
      setPreferredScrollableViewportSize(new Dimension(60, 50));
      setGridColor(Color.blue);
      JTableHeader header = getTableHeader();
      header.setForeground(Color.blue);
    }

    public TableCellRenderer getCellRenderer(int row, int column) {
      return cellRenderer;
    }

    public TableCellEditor getCellEditor(int row, int column) {
      spinCellEditor.rowNumber = row;
      return spinCellEditor;
    }

    public void setFont(Font font) {
      super.setFont(font);
      if(cellRenderer!=null) {
        Font aFont = cellRenderer.labelFont;
        aFont = aFont.deriveFont(font.getSize2D());
        cellRenderer.labelFont = aFont;
        spinCellEditor.stepSizeLabel.setFont(aFont);
        aFont = cellRenderer.fieldFont;
        aFont = aFont.deriveFont(font.getSize2D());
        cellRenderer.fieldFont = aFont;
        spinCellEditor.field.setFont(aFont);
      }
      setRowHeight(font.getSize()+4);
    }

  }

  /**
   * A class to provide model data for the parameters table.
   */
  class ParamTableModel extends AbstractTableModel {
    public String getColumnName(int col) {
      return (col==0)? ToolsRes.getString("Table.Heading.Parameter"): //$NON-NLS-1$
          ToolsRes.getString("Table.Heading.Value"); //$NON-NLS-1$
    }

    public int getRowCount() {
      return (fit==null)
             ? 0
             : fit.getParameterCount();
    }

    public int getColumnCount() {
      return 2;
    }

    public Object getValueAt(int row, int col) {
      if(col==0) {
        return fit.getParameterName(row);
      }
      return new Double(fit.getParameterValue(row));
    }

    public boolean isCellEditable(int row, int col) {
      return col!=0;
    }

    public Class<?> getColumnClass(int c) {
      return getValueAt(0, c).getClass();
    }

  }

  /**
   * A cell renderer for the parameter table.
   */
  class ParamCellRenderer extends JLabel implements TableCellRenderer {
    Color lightBlue = new Color(204, 204, 255);
    Color lightGray = javax.swing.UIManager.getColor("Panel.background"); //$NON-NLS-1$
    Font fieldFont = new JTextField().getFont();
    Font labelFont = getFont();

    // Constructor

    /**
     * Constructor ParamCellRenderer
     */
    public ParamCellRenderer() {
      super();
      setOpaque(true); // make background visible
      setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 2));
    }

    // Returns a label for the specified cell.
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int col) {
      setHorizontalAlignment(SwingConstants.LEFT);
      setBorder(new CellBorder(new Color(240, 240, 240)));
      if(value instanceof String) { // parameter name string
        setFont(labelFont);
        setBackground(isSelected
                      ? Color.LIGHT_GRAY
                      : lightGray);
        setForeground(Color.black);
        setText(value.toString());
      } else {                      // Double value
        setFont(fieldFont);
        setBackground(isSelected
                      ? lightBlue
                      : Color.white);
        setForeground(isSelected
                      ? Color.red
                      : Color.black);
        Format format = spinCellEditor.field.format;
        setText(format.format(value));
      }
      return this;
    }

  }

  /**
   * A cell editor that uses a JSpinner with a number crawler model.
   */
  class SpinCellEditor extends AbstractCellEditor implements TableCellEditor {
    JPanel panel = new JPanel(new BorderLayout());
    SpinnerNumberCrawlerModel crawlerModel = new SpinnerNumberCrawlerModel(1);
    JSpinner spinner;
    NumberField field;
    int rowNumber;
    JLabel stepSizeLabel = new JLabel("10%"); //$NON-NLS-1$

    // Constructor.
    SpinCellEditor() {
      panel.setOpaque(false);
      spinner = new JSpinner(crawlerModel);
      spinner.setToolTipText(ToolsRes.getString("Table.Spinner.ToolTip")); //$NON-NLS-1$
      spinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          autofitCheckBox.setSelected(false);
          double val = ((Double) spinner.getValue()).doubleValue();
          field.setValue(val);
          fit.setParameterValue(rowNumber, val);
          if((fitBuilder!=null)&&(fit instanceof UserFunction)) {
            // get dependent parameter values from fit builder
            UserFunction f = (UserFunction) fit;
            String name = f.getName();
            FitFunctionPanel panel = (FitFunctionPanel) fitBuilder.getPanel(name);
            if(panel!=null) {
              name = fit.getParameterName(rowNumber);
              Parameter seed = new Parameter(name, field.getText());
              Iterator<?> it = panel.getParamEditor().evaluateDependents(seed).iterator();
              while(it.hasNext()) {
                Parameter p = (Parameter) it.next();
                // find row number, set value in fit
                for(int i = 0; i<fit.getParameterCount(); i++) {
                  if(fit.getParameterName(i).equals(p.getName())) {
                    fit.setParameterValue(i, p.getValue());
                    paramModel.fireTableCellUpdated(i, 1);
                    break;
                  }
                }
              }
              panel.getFitFunctionEditor().parametersValid = false;
            }
          }
          drawer.functionChanged = true;
          fit(fit);
          firePropertyChange("changed", null, null);                       //$NON-NLS-1$
        }

      });
      field = new NumberField(10);
      field.setBorder(BorderFactory.createEmptyBorder(1, 1, 0, 0));
      spinner.setBorder(BorderFactory.createEmptyBorder(0, 1, 1, 0));
      spinner.setEditor(field);
      stepSizeLabel.addMouseListener(new MouseInputAdapter() {
        public void mousePressed(MouseEvent e) {
          JPopupMenu popup = new JPopupMenu();
          ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              // set the percent delta
              double percent = Double.parseDouble(e.getActionCommand());
              crawlerModel.setPercentDelta(percent);
              crawlerModel.refreshDelta();
              stepSizeLabel.setText(e.getActionCommand()+"%"); //$NON-NLS-1$
            }

          };
          for(int i = 0; i<3; i++) {
            String val = (i==0)? "10": (i==1)? "1.0": "0.1"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            JMenuItem item = new JMenuItem(val+"%"); //$NON-NLS-1$
            item.setActionCommand(val);
            item.addActionListener(listener);
            popup.add(item);
          }
          // show the popup
          popup.show(stepSizeLabel, 0, stepSizeLabel.getHeight());
        }

      });
      field.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          JComponent comp = (JComponent) e.getSource();
          if(e.getKeyCode()==KeyEvent.VK_ENTER) {
            spinner.setValue(new Double(field.getValue()));
            comp.setBackground(Color.white);
            crawlerModel.refreshDelta();
          } else {
            comp.setBackground(Color.yellow);
          }
        }

      });
      panel.add(spinner, BorderLayout.CENTER);
      panel.add(stepSizeLabel, BorderLayout.EAST);
    }

    // Gets the component to be displayed while editing.
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      spinner.setValue(value);
      crawlerModel.refreshDelta();
      return panel;
    }

    // Determines when editing starts.
    public boolean isCellEditable(EventObject e) {
      if(e instanceof MouseEvent) {
        return true;
      } else if(e instanceof ActionEvent) {
        return true;
      }
      return false;
    }

    // Called when editing is completed.
    public Object getCellEditorValue() {
      if(field.getBackground()==Color.yellow) {
        fit.setParameterValue(rowNumber, field.getValue());
        drawer.functionChanged = true;
        DatasetCurveFitter.this.firePropertyChange("fit", null, null); //$NON-NLS-1$
        field.setBackground(Color.white);
        firePropertyChange("changed", null, null);                     //$NON-NLS-1$
      }
      return null;
    }

  }

  /**
   * A number spinner model with a settable delta.
   */
  class SpinnerNumberCrawlerModel extends AbstractSpinnerModel {
    double val = 0;
    double delta;
    double percentDelta = 10;

    /**
     * Constructor SpinnerNumberCrawlerModel
     * @param initialDelta
     */
    public SpinnerNumberCrawlerModel(double initialDelta) {
      delta = initialDelta;
    }

    public Object getValue() {
      return new Double(val);
    }

    public Object getNextValue() {
      return new Double(val+delta);
    }

    public Object getPreviousValue() {
      return new Double(val-delta);
    }

    public void setValue(Object value) {
      if(value!=null) {
        val = ((Double) value).doubleValue();
        fireStateChanged();
      }
    }

    public void setPercentDelta(double percent) {
      percentDelta = percent;
    }

    public double getPercentDelta() {
      return percentDelta;
    }

    // refresh delta based on current value and percent
    public void refreshDelta() {
      if(val!=0) {
        delta = Math.abs(val*percentDelta/100);
      }
    }

  }

  /**
   * A polynomial that implements KnownFunction.
   */
  class KnownPolynomial extends PolynomialLeastSquareFit implements KnownFunction {
    String[] names = {"a", "b", "c", "d", "e", "f"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    KnownPolynomial(double[] xdata, double[] ydata, int degree) {
      super(xdata, ydata, degree);
    }

    KnownPolynomial(double[] coeffs) {
      super(coeffs);
    }

    /**
     * Gets the parameter count.
     * @return the number of parameters
     */
    public int getParameterCount() {
      return coefficients.length;
    }

    /**
     * Gets a parameter name.
     *
     * @param i the parameter index
     * @return the name of the parameter
     */
    public String getParameterName(int i) {
      return names[i];
    }

    /**
     * Gets a parameter value.
     *
     * @param i the parameter index
     * @return the value of the parameter
     */
    public double getParameterValue(int i) {
      return coefficients[coefficients.length-i-1];
    }

    /**
     * Sets a parameter value.
     *
     * @param i the parameter index
     * @param value the value
     */
    public void setParameterValue(int i, double value) {
      coefficients[coefficients.length-i-1] = value;
    }

    /**
     * Gets the equation.
     *
     * @param indepVarName the name of the independent variable
     * @return the equation
     */
    public String getExpression(String indepVarName) {
      StringBuffer eqn = new StringBuffer();
      int end = coefficients.length-1;
      for(int i = 0; i<=end; i++) {
        eqn.append(getParameterName(i));
        if(end-i>0) {
          eqn.append("*");   //$NON-NLS-1$
          eqn.append(indepVarName);
          if(end-i>1) {
            eqn.append("^"); //$NON-NLS-1$
            eqn.append(end-i);
          }
          eqn.append(" + "); //$NON-NLS-1$
        }
      }
      return eqn.toString();
    }

  }

  /**
   * A function whose value is the total deviation squared
   * between a multivariable function and a set of data points.
   * This is minimized by the HessianMinimize class.
   */
  public class MinimizeMultiVarFunction implements MultiVarFunction {
    MultiVarFunction f;
    double[] x, y; // the data
    double[] vars = new double[5];

    // Constructor
    MinimizeMultiVarFunction(MultiVarFunction f, double[] x, double[] y) {
      this.f = f;
      this.x = x;
      this.y = y;
    }

    // Evaluates the function
    public double evaluate(double[] params) {
      System.arraycopy(params, 0, vars, 1, 4);
      double sum = 0.0;
      for(int i = 0, n = x.length; i<n; i++) {
        vars[0] = x[i];
        // evaluate the function and find deviation
        double dev = y[i]-f.evaluate(vars);
        // sum the squares of the deviations
        sum += dev*dev;
      }
      return sum;
    }

  }

  /**
   * A function whose value is the total deviation squared
   * between a user function and a set of data points.
   * This function is minimized by the HessianMinimize class.
   */
  public class MinimizeUserFunction implements MultiVarFunction {
    UserFunction f;
    double[] x, y; // the data

    // Constructor
    MinimizeUserFunction(UserFunction f, double[] x, double[] y) {
      this.f = f;
      this.x = x;
      this.y = y;
    }

    // Evaluates this function
    public double evaluate(double[] params) {
      // set the parameter values of the user function
      for(int i = 0; i<params.length; i++) {
        f.setParameterValue(i, params[i]);
      }
      double sum = 0.0;
      for(int i = 0; i<x.length; i++) {
        // evaluate the user function and find deviation
        double dev = y[i]-f.evaluate(x[i]);
        // sum the squares of the deviations
        sum += dev*dev;
      }
      return sum;
    }

  }

  /**
   * A JTextField that accepts only numbers.
   */
  class NumberField extends JTextField {
    // instance fields
    protected NumberFormat format = NumberFormat.getInstance();
    protected double prevValue;

    /**
     * Constructor NumberField
     * @param columns
     */
    public NumberField(int columns) {
      super(columns);
      if(format instanceof DecimalFormat) {
        ((DecimalFormat) format).applyPattern("0.000E0"); //$NON-NLS-1$
      }
      setForeground(Color.black);
    }

    public double getValue() {
      if(getText().equals(format.format(prevValue))) {
        return prevValue;
      }
      double retValue;
      try {
        retValue = format.parse(getText()).doubleValue();
      } catch(ParseException e) {
        Toolkit.getDefaultToolkit().beep();
        setValue(prevValue);
        return prevValue;
      }
      return retValue;
    }

    public void setValue(double value) {
      if(!isVisible()) {
        return;
      }
      setText(format.format(value));
      prevValue = value;
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
