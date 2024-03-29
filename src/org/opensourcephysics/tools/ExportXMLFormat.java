/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.io.File;
import java.util.List;
import org.opensourcephysics.controls.XMLControl;
import java.util.Iterator;
import org.opensourcephysics.controls.XMLControlElement;
import java.io.*;
import javax.swing.JOptionPane;

/**
 * Exports data in XML format.
 *
 * @author W. Christian
 * @version 1.0
 */
public class ExportXMLFormat implements ExportFormat {
  public String description() {
    return "XML"; //$NON-NLS-1$
  }

  public String extension() {
    return "xml"; //$NON-NLS-1$
  }

  public void export(File file, List<Object> data) {
    FileWriter fw = null;
    try {
      fw = new FileWriter(file);
    } catch(IOException ex) {
      JOptionPane.showMessageDialog(null, 
      		ToolsRes.getString("ExportFormat.Dialog.WriteError.Message"),  //$NON-NLS-1$
      		ToolsRes.getString("ExportFormat.Dialog.WriteError.Title"),  //$NON-NLS-1$
      		JOptionPane.ERROR_MESSAGE);
      return;
    }
    PrintWriter pw = new PrintWriter(fw);
    Iterator<Object> it = data.iterator();
    while(it.hasNext()) {
      XMLControl control = new XMLControlElement(it.next()); // convert the data to xml
      pw.print(control.toXML());
      pw.println();
    }
    pw.close();
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
