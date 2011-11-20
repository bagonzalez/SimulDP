/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensourcephysics.display.*;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;

/**
 * This tool allows users to create and manage editable Functions.
 *
 * @author Douglas Brown
 */
public class FunctionTool extends JDialog {

	// static fields
  protected static String[] parserNames = new String[] {
		"e", "pi", "min", "mod", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"sin", "cos", "abs", "log", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"acos", "acosh", "ceil", "cosh", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"asin", "asinh", "atan", "atanh", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"exp", "frac", "floor", "int", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"random", "round", "sign", "sinh", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"step", "tanh", "atan2", "max", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"sqrt", "sqr", "if", "tan"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$;

	// instance fields
  protected Map<String, FunctionPanel> panels = new TreeMap<String, FunctionPanel>(); // maps name to FunctionPanel
  protected HashSet<String> forbiddenNames = new HashSet<String>();
  protected JPanel contentPane = new JPanel(new BorderLayout());
  protected JPanel noData;
  protected JToolBar toolbar;
  protected JLabel spinnerLabel;
  protected SpinnerRollingListModel spinnerModel;
  protected JSpinner spinner;
  protected JPanel north = new JPanel(new BorderLayout());
  protected FunctionPanel selectedPanel;
  protected JScrollPane selectedPanelScroller;
  protected JButton helpButton, closeButton, fontButton, undoButton, redoButton;
  protected JPopupMenu popup;
  protected JMenuItem defaultFontSizeItem;
  protected JPanel buttonbar = new JPanel(new FlowLayout());
  protected JButton[] customButtons; // may be null
  protected String helpPath= ""; //$NON-NLS-1$
  protected String helpBase = "http://www.opensourcephysics.org/online_help/tools/"; //$NON-NLS-1$
  protected TextFrame helpFrame;
  protected ActionListener helpAction;
  protected int fontLevel = 0;
  protected boolean wasVisible = false;

  /**
   * Constructs a tool for the specified component (may be null)
   *
   * @param comp Component used to get Frame owner of this Dialog
   */
  public FunctionTool(Component comp) {
  	this(comp, null);
  }

  /**
   * Constructs a tool with custom buttons.
   *
   * @param comp Component used to get Frame owner of this Dialog
   * @param buttons an array of custom buttons
   */
  public FunctionTool(Component comp, JButton[] buttons) {
  	super(JOptionPane.getFrameForComponent(comp), false);
		customButtons = buttons;
		addForbiddenNames(parserNames);
		addForbiddenNames(UserFunction.dummyVars);
    setName("FunctionTool"); //$NON-NLS-1$
    createGUI();
    refreshGUI();
  }

  /**
   * Adds a FunctionPanel.
   *
   * @param name a descriptive name
   * @param panel the FunctionPanel
   * @return the added panel
   */
  public FunctionPanel addPanel(String name, FunctionPanel panel) {
		OSPLog.finest("adding panel "+name); //$NON-NLS-1$
  	panel.setFontLevel(fontLevel);
  	panel.setName(name);
  	panel.setFunctionTool(this);
  	panels.put(name, panel);
  	panel.addForbiddenNames(forbiddenNames.toArray(new String[0]));
  	refreshSpinner(name);
  	return panel;
  }

  /**
   * Removes a named FunctionPanel.
   *
   * @param name the name
   * @return the removed panel, if any
   */
  public FunctionPanel removePanel(String name) {
  	FunctionPanel panel = panels.get(name);
  	if (panel != null) {
  		OSPLog.finest("removing panel "+name); //$NON-NLS-1$
  		panels.remove(name);
    	refreshSpinner(null);
  	}
  	return panel;
  }

  /**
   * Renames a FunctionPanel.
   *
   * @param prevName the previous name
   * @param newName the new name
   * @return the renamed panel
   */
  public FunctionPanel renamePanel(String prevName, String newName) {
  	FunctionPanel panel = getPanel(prevName);
  	if (panel == null || prevName.equals(newName)) 
  		return panel;
		OSPLog.finest("renaming panel "+prevName+" to "+newName); //$NON-NLS-1$ //$NON-NLS-2$
  	panels.remove(prevName);
  	panels.put(newName, panel);
  	panel.prevName = prevName;
  	panel.setName(newName);
  	refreshSpinner(newName);
  	return panel;
  }

  /**
   * Selects a FunctionPanel by name.
   *
   * @param name the name
   */
  public void setSelectedPanel(String name) {
  	spinnerModel.setValue(name);
  }

  /**
   * Returns the name of the selected FunctionPanel.
   *
   * @return the name
   */
  public String getSelectedName() {
  	if (selectedPanel == null) return null;
  	Iterator<String> it = panels.keySet().iterator();
  	while (it.hasNext()) {
  		String name = it.next();
  		if (panels.get(name) == selectedPanel) return name;
  	}
  	return null;
  }

  /**
   * Returns the selected FunctionPanel.
   *
   * @return the FunctionPanel
   */
  public FunctionPanel getSelectedPanel() {
  	return getPanel(getSelectedName());
  }

  /**
   * Returns the named FunctionPanel.
   *
   * @param name the name
   * @return the FunctionPanel
   */
  public FunctionPanel getPanel(String name) {
  	return name == null? null: panels.get(name);
  }

  /**
   * Returns the set of all panel names.
   *
   * @return a set of names
   */
  public Set<String> getPanelNames() {
  	return panels.keySet();
  }

  /**
   * Clears all FunctionPanels.
   */
  public void clearPanels() {
		OSPLog.finest("clearing panels"); //$NON-NLS-1$
  	panels.clear();
  	refreshSpinner(null);
  }

  /**
   * Adds names to the forbidden set.
   */
  public void addForbiddenNames(String[] names) {
  	for (int i = 0; i < names.length; i++)
  		forbiddenNames.add(names[i]);
  }

  /**
   * Overrides JDialog setVisible method.
   *
   * @param vis true to show this inspector
   */
  public void setVisible(boolean vis) {
    super.setVisible(vis);
    firePropertyChange("visible", null, new Boolean(vis)); //$NON-NLS-1$
  }

  /**
   * Sets the path of the help file.
   *
   * @param path a filename or url
   */
  public void setHelpPath(String path) {
  	helpPath = path;
  }

  /**
   * Sets the help action. this will replace the current help action
   *
   * @param action a custom help action
   */
  public void setHelpAction(ActionListener action) {
  	helpButton.removeActionListener(helpAction);
  	helpAction = action;
  	helpButton.addActionListener(helpAction);
  }

  /**
   * Reports if this is empty.
   *
   * @return true if empty
   */
  public boolean isEmpty() {
  	return panels.isEmpty();
  }

  /**
   * Sets the font level.
   *
   * @param level the level
   */
  public void setFontLevel(int level) {
  	 level = Math.max(0, level);
  	 if (level == fontLevel) return;
     fontLevel = level;
     boolean vis = isVisible();
     setVisible(false);
     FontSizer.setFonts(this, level);
     FontSizer.setFonts(popup, level);
     for (Iterator<FunctionPanel> it = panels.values().iterator(); it.hasNext();) {
    	 FunctionPanel next = it.next();
    	 if (next == getSelectedPanel()) continue;
       next.setFontLevel(level);
     }
     if (level < popup.getSubElements().length) {
    	 MenuElement [] e = popup.getSubElements();
    	 JRadioButtonMenuItem item = (JRadioButtonMenuItem)e[level];
    	 item.setSelected(true);
     }
     int h = getHeight();
     pack();
     Dimension dim = getSize();
     dim.height = h;
     setSize(dim);
     validate();
     setVisible(vis);
  }

  /**
   * Gets the font level.
   *
   * @return the level
   */
  public int getFontLevel() {
    return fontLevel;
  }

  /**
   * Fires a property change. This makes this method visible to the tools package.
   */
  protected void firePropertyChange(String name, Object oldObj, Object newObj) {
    super.firePropertyChange(name, oldObj, newObj);
  }

  /**
   * Creates the GUI.
   */
  private void createGUI() {
     // listen to ToolsRes for locale changes
     ToolsRes.addPropertyChangeListener("locale", new PropertyChangeListener() { //$NON-NLS-1$
       public void propertyChange(PropertyChangeEvent e) {
         refreshGUI();
       }
     });
     // configure the dialog
     setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
   	 // create the noData panel
   	 noData = new JPanel(new BorderLayout());
   	 // create the dropdown toolbar
     toolbar = new JToolBar();
   	 toolbar.setFloatable(false);
   	 spinnerLabel = new JLabel();
   	 spinnerLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 2));
   	 spinnerLabel.addMouseListener(new MouseAdapter() {
       public void mousePressed(MouseEvent e) {
      	 String name = getSelectedName();
      	 if (name != null) {
         	 FunctionPanel panel = panels.get(name);
         	 panel.clearSelection();
      	 }
       }
     });
   	 toolbar.add(spinnerLabel);
     // create spinner rolling list model
     ArrayList<String> spinnerList = new ArrayList<String>();
   	 spinnerList.add(ToolsRes.getString("FunctionTool.Empty.Name")); //$NON-NLS-1$
   	 spinnerModel = new SpinnerRollingListModel(spinnerList);
     spinner = new JSpinner(spinnerModel);
     JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)spinner.getEditor();
     editor.getTextField().setBackground(Color.white);
     editor.getTextField().setEditable(false);
     spinner.addChangeListener(new ChangeListener() {
       public void stateChanged(ChangeEvent e) {
         String name = spinner.getValue().toString();
         select(name);
         FunctionPanel panel = panels.get(name);
         if (panel != null) {
           panel.getFunctionTable().clearSelection();
           panel.getFunctionTable().selectOnFocus = false;
           panel.getParamTable().clearSelection();
           panel.getParamTable().selectOnFocus = false;
           panel.refreshGUI();
         }
         helpButton.requestFocus();
       }
     });
   	 toolbar.add(spinner);
   	 if (customButtons != null) {
     	 toolbar.addSeparator();
     	 for (int i = 0; i < customButtons.length; i++) {
       	 toolbar.add(customButtons[i]);
     	 }
   	 }
   	 north.add(toolbar, BorderLayout.NORTH);
   	 north.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
     // create buttons
     closeButton = new JButton(ToolsRes.getString("Tool.Button.Close")); //$NON-NLS-1$
     closeButton.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         setVisible(false);
       }
     });
     helpAction = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         if (helpFrame == null) {
        	 // show web help if available
        	 String help = XML.getResolvedPath(helpPath, helpBase);
           if (ResourceLoader.getResource(help) != null)
          	 helpFrame = new TextFrame(help);
           else {
          	 String classBase = "/org/opensourcephysics/resources/tools/html/"; //$NON-NLS-1$
          	 help = XML.getResolvedPath(helpPath, classBase);
          	 helpFrame = new TextFrame(help);
           }
           helpFrame.setSize(700, 550);
           // center on the screen
           Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
           int x = (dim.width-helpFrame.getBounds().width)/2;
           int y = (dim.height-helpFrame.getBounds().height)/2;
           helpFrame.setLocation(x, y);
         }
         helpFrame.setVisible(true);
       }
     };
     helpButton = new JButton(ToolsRes.getString("Tool.Button.Help")); //$NON-NLS-1$
     helpButton.addActionListener(helpAction);
     undoButton = new JButton(ToolsRes.getString("DataFunctionPanel.Button.Undo")); //$NON-NLS-1$
     redoButton = new JButton(ToolsRes.getString("DataFunctionPanel.Button.Redo")); //$NON-NLS-1$
     // create font sizer button and popup
     fontButton = new JButton(ToolsRes.getString("Tool.Menu.FontSize")); //$NON-NLS-1$
     popup = new JPopupMenu();
     ButtonGroup group = new ButtonGroup();
     Action fontSizeAction = new AbstractAction() {
     	public void actionPerformed(ActionEvent e) {
     		int i = Integer.parseInt(e.getActionCommand());
       	setFontLevel(i);
     	}
     };
     for (int i = 0; i < 4; i++) {
     	JMenuItem item = new JRadioButtonMenuItem("+"+i); //$NON-NLS-1$
     	if (i == 0) defaultFontSizeItem = item;
     	item.addActionListener(fontSizeAction);
     	item.setActionCommand(""+i); //$NON-NLS-1$
     	popup.add(item);
     	group.add(item);
     	if (i == fontLevel) 
     		item.setSelected(true);
     }
     fontButton.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         popup.show(fontButton, 0, fontButton.getHeight());
       }
     });
     // prepare button bar
     buttonbar.setBorder(BorderFactory.createEtchedBorder());
     buttonbar.add(helpButton);
     buttonbar.add(undoButton);
     buttonbar.add(redoButton);
     buttonbar.add(fontButton);
     buttonbar.add(closeButton);
		 contentPane.add(north, BorderLayout.NORTH);
     contentPane.add(noData, BorderLayout.CENTER);
     contentPane.add(buttonbar, BorderLayout.SOUTH);
   	 setContentPane(contentPane);
   	 pack();
   	 Dimension dim = getSize();
   	 dim.height = Math.max(360, dim.height);
   	 setSize(dim);   	 
     // center this on the screen
     dim = Toolkit.getDefaultToolkit().getScreenSize();
     int x = (dim.width-getBounds().width)/2;
     int y = (dim.height-getBounds().height)/2;
     setLocation(x, y);
  }

  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
  	if (selectedPanel != null) {
  		String label = selectedPanel.getLabel();
    	spinnerLabel.setText(label+":"); //$NON-NLS-1$
  	}
  	spinner.setToolTipText(ToolsRes.getString("FunctionTool.Spinner.Tooltip")); //$NON-NLS-1$
    closeButton.setText(ToolsRes.getString("Tool.Button.Close"));               //$NON-NLS-1$
    closeButton.setToolTipText(ToolsRes.getString("Tool.Button.Close.ToolTip"));//$NON-NLS-1$
    helpButton.setText(ToolsRes.getString("Tool.Button.Help"));                 //$NON-NLS-1$
    helpButton.setToolTipText(ToolsRes.getString("Tool.Button.Help.ToolTip"));  //$NON-NLS-1$
    fontButton.setText(ToolsRes.getString("Tool.Menu.FontSize"));         //$NON-NLS-1$
    fontButton.setToolTipText(ToolsRes.getString("FunctionTool.Button.Display.Tooltip")); //$NON-NLS-1$
    defaultFontSizeItem.setText(ToolsRes.getString("Tool.MenuItem.DefaultFontSize")); //$NON-NLS-1$
    Iterator<FunctionPanel> it = panels.values().iterator();
    while (it.hasNext()) {
    	FunctionPanel panel = it.next();
      panel.refreshGUI();
    }
  }

  /**
   * Refreshes the name spinner and selects the specified object.
   * If object is null, spinner keeps the currrent selection if possible
   */
  private void refreshSpinner(Object toSelect) {
  	if (toSelect == null) toSelect = spinner.getValue();
    ArrayList<String> spinnerList = new ArrayList<String>();
  	spinnerList.addAll(panels.keySet());
  	if (spinnerList.isEmpty()) {
  		spinnerList.add(ToolsRes.getString("FunctionTool.Empty.Name")); //$NON-NLS-1$
  	}
    spinnerModel.setList(spinnerList, toSelect);
  }

  /**
   * Selects the named function panel.
   */
  private void select(String name) {
  	FunctionPanel panel = name == null? null: panels.get(name);
  	FunctionPanel prev = selectedPanel;
		if (selectedPanel != null) contentPane.remove(selectedPanelScroller);
		else contentPane.remove(noData);
  	selectedPanel = panel;
  	spinner.setEnabled(panel != null);
  	spinnerLabel.setEnabled(panel != null);
  	if (panel != null) {
  		selectedPanelScroller = new JScrollPane(panel);
  		contentPane.add(selectedPanelScroller, BorderLayout.CENTER);
  		panel.refreshGUI();
  	}
  	else {
  		contentPane.add(noData, BorderLayout.CENTER);
  		buttonbar.removeAll();
      buttonbar.add(helpButton);
      buttonbar.add(closeButton);
  	}
  	validate();
  	refreshGUI();
  	repaint();
  	firePropertyChange("panel", prev, panel); //$NON-NLS-1$
  }

  /**
   * Gets a unique name.
   *
   * @param proposedName the proposed name
   * @return the unique name
   */
  protected String getUniqueName(String proposedName) {
 	  // construct a unique name from that proposed by adding trailing digit
  	int i = 1;
  	String name = proposedName+i;
    while(panels.keySet().contains(name) || forbiddenNames.contains(name)) {
      i++;
      name = proposedName+i;
    }
    return name;
  }

	class SpinnerRollingListModel extends SpinnerListModel {
		boolean fireStateChange = true;

		SpinnerRollingListModel(List<String> list) {
			super(list);
		}

		protected void fireStateChanged() {
			if (fireStateChange) super.fireStateChanged();
		}

		public void setList(List<String> list, Object objToSelect) {
			fireStateChange = objToSelect==null || list.indexOf(objToSelect)<=0;
			super.setList(list); // sets index to 0
			fireStateChange = true;
			if (objToSelect != null && list.contains(objToSelect))
				setValue(objToSelect);
		}

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
