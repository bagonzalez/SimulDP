/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.dialogs;
import java.util.Locale;
import java.util.ResourceBundle;

import org.opensourcephysics.display.OSPRuntime;

/**
 * String constants for Dialogs.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class DialogsRes {
  public static  String AUTOSCALE_AUTOSCALE;
  public static  String AUTOSCALE_AUTO;
  public static  String AUTOSCALE_OK;
  public static  String AUTOSCALE_ZOOM_WARNING;
  public static  String SCALE_SCALE;
  public static  String SCALE_MIN;
  public static  String SCALE_MAX;
  public static  String SCALE_AUTO;
  public static  String SCALE_CANCEL;
  public static  String SCALE_OK;
  public static  String SCALE_HORIZONTAL;
  public static  String SCALE_VERTICAL;
  public static  String LOG_SCALE;
  public static  String LOG_X;
  public static  String LOG_Y;
  public static  String LOG_OK;
  public static  String LOG_WARNING;

  // static fields
  static final String BUNDLE_NAME = "org.opensourcephysics.resources.display.dialogs_res";  //$NON-NLS-1$
  static Locale resourceLocale=Locale.ENGLISH;
  static ResourceBundle res;

  static {  
	  String language=Locale.getDefault().getLanguage();
	  resourceLocale=Locale.ENGLISH;
	  for (Locale locale: OSPRuntime.getInstalledLocales()) {
		    if (locale.getLanguage().equals(language)){
		    	resourceLocale=locale;
		    	break;
		    }
	  }
	  res = ResourceBundle.getBundle(BUNDLE_NAME, resourceLocale); 
	  setLocalStrings(res);
  }
  
  private DialogsRes() {}

  /**
   * Sets the locale.
   *
   * @param loc the locale
   */
  public static void setLocale(Locale loc){
     if (resourceLocale==loc){
        return;
     }
     resourceLocale = loc;
     // get the new resource bundle
     ResourceBundle res = ResourceBundle.getBundle(BUNDLE_NAME, resourceLocale);
     setLocalStrings(res);
  }

  /**
  * Gets the local strings.
  *
  * Static strings are used for speed to avoid having to call the resource object.  This may no longer be necessary on fast machines.
  */
 private static void setLocalStrings(ResourceBundle res){
    AUTOSCALE_AUTOSCALE = res.getString("AUTOSCALE_AUTOSCALE"); //$NON-NLS-1$
    AUTOSCALE_AUTO = res.getString("AUTOSCALE_AUTO"); //$NON-NLS-1$
    AUTOSCALE_OK = res.getString("AUTOSCALE_OK"); //$NON-NLS-1$
    AUTOSCALE_ZOOM_WARNING = res.getString("AUTOSCALE_ZOOM_WARNING"); //$NON-NLS-1$
    SCALE_SCALE = res.getString("SCALE_SCALE"); //$NON-NLS-1$
    SCALE_MIN = res.getString("SCALE_MIN"); //$NON-NLS-1$
    SCALE_MAX = res.getString("SCALE_MAX"); //$NON-NLS-1$
    SCALE_AUTO = res.getString("SCALE_AUTO"); //$NON-NLS-1$
    SCALE_CANCEL = res.getString("SCALE_CANCEL"); //$NON-NLS-1$
    SCALE_OK = res.getString("SCALE_OK"); //$NON-NLS-1$
    SCALE_HORIZONTAL = res.getString("SCALE_HORIZONTAL"); //$NON-NLS-1$
    SCALE_VERTICAL = res.getString("SCALE_VERTICAL"); //$NON-NLS-1$
    LOG_SCALE = res.getString("LOG_SCALE"); //$NON-NLS-1$
    LOG_X = res.getString("LOG_X"); //$NON-NLS-1$
    LOG_Y = res.getString("LOG_Y"); //$NON-NLS-1$
    LOG_OK = res.getString("LOG_OK"); //$NON-NLS-1$
    LOG_WARNING = res.getString("LOG_WARNING"); //$NON-NLS-1$
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
