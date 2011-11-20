/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;

import java.lang.reflect.Method;
import java.rmi.*;
import java.util.*;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.*;

/**
 * This tool sends data to any tool that requests it.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class DataRefreshTool implements Tool {
  
	private static Map<Data, DataRefreshTool> tools = new HashMap<Data, DataRefreshTool>();
  
	private Data data; // data source 

  /**
   * Returns a DataRefreshTool for the specified data object.
   *
   * @param data the data
   * @return the tool
   */
  public static DataRefreshTool getTool(Data data) {
  	DataRefreshTool tool = tools.get(data);
  	if (tool == null) {
  		tool = new DataRefreshTool(data);
  		tools.put(data, tool);
  	}
  	return tool;
  }

  /**
   * Constructs a DataRefreshTool for the specified data object.
   *
   * @param data the data
   */
  public DataRefreshTool(Data data) {
    this.data = data;
  }

  /**
   * Sends a job to this tool and specifies a tool to reply to.
   * The job xml must describe a data object containing datasets with ids
   * that match those in this tool's data.
   *
   * @param job the Job
   * @param replyTo the tool requesting refreshed data
   * @throws RemoteException
   */
  public void send(Job job, Tool replyTo) throws RemoteException {
    XMLControlElement control = new XMLControlElement(job.getXML());
    if(control.failedToRead() || replyTo == null
    		||!Data.class.isAssignableFrom(control.getObjectClass())) {
      return;
    }
    DatasetManager reply = new DatasetManager();
    Data request = (Data)control.loadObject(null, true, true);
    // set reply name to request name
    String name = null;
    try {
      Method m = request.getClass().getMethod("getName", new Class[0]); //$NON-NLS-1$
      name = (String) m.invoke(request, new Object[0]);
      reply.setName(name);
    } catch(Exception ex) {}
    // find datasets that match requested ids
    for (Dataset next: request.getDatasets()) {
    	if (next == null) continue;
    	Dataset match = getMatch(next.getID());
    	if (match != null) reply.addDataset(match); 
    }
    // send datasets to requesting tool
    if (!reply.getDatasets().isEmpty()) {
	    control = new XMLControlElement(reply);
	    job.setXML(control.toXML());    
	    replyTo.send(job, this);
	  }
  }
  
  private Dataset getMatch(int id) {
    for (Dataset next: data.getDatasets()) {
    	if (next.getID() == id) return next; 
    }  	
  	return null;
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
