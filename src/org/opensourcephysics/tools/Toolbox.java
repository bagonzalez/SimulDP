/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.rmi.*;
import java.util.*;
import org.opensourcephysics.controls.*;
import java.rmi.registry.*;
import javax.swing.JOptionPane;

/**
 * Toolbox stores tools that can exchange data using the Tool interface.
 *
 * @author Wolfgang Christian and Doug Brown
 * @version 0.1
 */
public class Toolbox {
  protected static Map<String, Tool> tools = new HashMap<String, Tool>();
  protected static Registry registry;
  protected static int allowRMI = -1; // flag to determine if RMI should be allowed.

  protected Toolbox() {/** empty block */}

  public static void addTool(String name, Tool tool) {
  	if (tools.get(name) == null) {
      tools.put(name, tool);
      OSPLog.fine("Added to toolbox: "+name); //$NON-NLS-1$
  	}
  }

  public static boolean addRMITool(String name, Tool tool) {
    initRMI();
    if(allowRMI==0) { // user has choosen not to allow RMI
      return false;
    }
    try {
      Tool remote = new RemoteTool(tool);
      registry.bind(name, remote);
      OSPLog.fine("Added to RMI registry: "+name); //$NON-NLS-1$
      return true;
    } catch(Exception ex) {
      OSPLog.warning("RMI registration failed: "+name+" ["+ex+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      return false;
    }
  }

  public static Tool getTool(String name) {
    if(tools.containsKey(name)) {
      // look for local tool
      Tool tool = tools.get(name);
      OSPLog.fine("Found local tool: "+name); //$NON-NLS-1$
      return tool;
    }
    initRMI();
    if(allowRMI==0) { // user has choosen not to allow RMI
      return null;
    }
    // look for RMI tool
    try {
      Tool tool = (Tool) registry.lookup(name);
      OSPLog.fine("Found RMI tool "+name); //$NON-NLS-1$
      return new RemoteTool(tool);
    } catch(Exception ex) {
      OSPLog.info("RMI lookup failed: "+name+" ["+ex+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    return null;
  }

  private static void initRMI() {
    if(allowRMI==0) { // user has choosen not to allow RMI
      return;
    }
    int selection = JOptionPane.showConfirmDialog(null, ToolsRes.getString("Toolbox.Dialog.UseRemote.Query"), ToolsRes.getString("Toolbox.Dialog.UseRemote.Title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
    if(selection==JOptionPane.YES_OPTION) {
      allowRMI = 1;
    } else {
      allowRMI = 0;
      return;
    }
    if(registry==null) {
      try {
        registry = LocateRegistry.getRegistry(1099);
        registry.list();
      } catch(RemoteException ex) {
        try {
          registry = LocateRegistry.createRegistry(1099);
        } catch(RemoteException ex1) {
          OSPLog.info(ex1.getMessage());
        }
      }
    }
    if(System.getSecurityManager()==null) {
      try {
        // set the rmi server codebase and security policy properties
        String base = "file:"+System.getProperty("user.dir"); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("java.rmi.server.codebase", base+"/classes/"); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("java.security.policy", base+"/Remote.policy"); //$NON-NLS-1$ //$NON-NLS-2$
        // set the security manager
        System.setSecurityManager(new RMISecurityManager());
      } catch(Exception ex) {
        OSPLog.info(ex.getMessage());
      }
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
