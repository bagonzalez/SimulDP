/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.beans.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * A dialog that displays an editable table of properties using an OSPControlTable.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class OSPTableInspector extends JDialog implements PropertyChangeListener {
  // static fields
  final static String FRAME_TITLE = ControlsRes.getString("OSPTableInspector.Properties_of"); //$NON-NLS-1$
  // instance fields

  private OSPControlTable table;


  /**
   * Constructs editable modal inspector for specified XMLControl.
   *
   * @param control the xml control
   */
  public OSPTableInspector() {
    this( true, true);
  }

  /**
   * Constructs modal inspector for specified XMLControl and sets editable flag.
   *
   * @param control the xml control
   * @param editable true to enable editing
   */
  public OSPTableInspector(boolean editable) {
    this(editable, true);
  }


  /**
   * Constructs inspector for specified XMLControl and sets editable and modal flags.
   *
   * @param control the xml control
   * @param editable true to enable editing
   * @param modal true if modal
   */
  public OSPTableInspector(boolean editable, boolean modal) {
    this(null, editable, modal);
  }

  /**
 * Constructs inspector for specified XMLControl and sets editable and modal flags.
 *
 * @param owner the frame's owner
 * @param control the xml control
 * @param editable true to enable editing
 * @param modal true if modal
 */

public OSPTableInspector(Frame owner, boolean editable, boolean modal) {
  super(owner, modal);
  XMLControlElement control=new XMLControlElement();
  table = new OSPControlTable(control);
  table.setEditable(editable);
  table.addPropertyChangeListener("cell", this); //$NON-NLS-1$
  createGUI();
  String s = XML.getExtension(control.getObjectClassName());
  setTitle(FRAME_TITLE+" "+s+" \""+control.getPropertyName()+"\" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
}



  /**
   * Gets the Control associated with this table.
   *
   * @return Control
   */
  public Control getControl(){
    return table;
  }

  /**
   * Listens for property change events from XMLTable.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    // forward event to listeners
    firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
  }

  /**
   * Gets the XMLTable.
   *
   * @return the table
   */
  public XMLTable getTable() {
    return table;
  }

  // creates the GUI
  private void createGUI() {
    setSize(400, 300);
    setContentPane(new JPanel(new BorderLayout()));
    JScrollPane scrollpane = new JScrollPane(table);
    scrollpane.createHorizontalScrollBar();
    getContentPane().add(scrollpane, BorderLayout.CENTER);
    if(!JDialog.isDefaultLookAndFeelDecorated()) return;
    JPanel panel= new JPanel(new FlowLayout());
    JButton closeButton= new JButton(ControlsRes.getString("OSPTableInspector.OK")); //$NON-NLS-1$
    panel.add(closeButton);
    getContentPane().add(panel, BorderLayout.SOUTH);
    closeButton.addActionListener(
      new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
      }
    }
    );
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
