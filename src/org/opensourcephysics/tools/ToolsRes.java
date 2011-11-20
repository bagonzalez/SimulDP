package org.opensourcephysics.tools;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.event.SwingPropertyChangeSupport;
import org.opensourcephysics.ejs.EjsRes;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.controls.ControlsRes;
import org.opensourcephysics.display.dialogs.DialogsRes;

/**
 * ToolsRes provides access to string resources for OSPControls and fires a property change event
 * when the locale changes.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class ToolsRes {
  // static fields
  static final String BUNDLE_NAME="org.opensourcephysics.resources.tools.tools";//$NON-NLS-1$
  static Locale resourceLocale=Locale.ENGLISH;
  static ResourceBundle res=ResourceBundle.getBundle(BUNDLE_NAME, resourceLocale);
  static Object resObj = new ToolsRes();
  static PropertyChangeSupport support = new SwingPropertyChangeSupport(resObj);
  
  static{
	  String language=Locale.getDefault().getLanguage();
	  resourceLocale=Locale.ENGLISH;
	  for (Locale locale: OSPRuntime.getInstalledLocales()) {
		    if (locale.getLanguage().equals(language)){
		    	resourceLocale=locale;
		    	break;
		    }
	  }
	  res = ResourceBundle.getBundle(BUNDLE_NAME, resourceLocale);  
  }

  /**
   * Private constructor to prevent instantiation.
   */
  private ToolsRes() {/** empty block */}

  /**
   * Gets the localized value of a string. If no localized value is found, the
   * key is returned surrounded by exclamation points.
   *
   * @param key the string to localize
   * @return the localized string
   */
  static public String getString(String key) {
     try {
        return res.getString(key);
     } catch(MissingResourceException ex) {
        return "!"+key+"!"; //$NON-NLS-1$ //$NON-NLS-2$
     }
  }

  /**
   * Gets the language for this locale.
   * @return String
   */
  public static String getLanguage(){
     return resourceLocale.getLanguage();
  }

  /**
   * Sets the locale.
   *
   * @param loc the locale
   */
  public static void setLocale(Locale loc) {
     if(resourceLocale==loc) {
        return;
     }
     Locale prev = resourceLocale;
     resourceLocale = loc;
     // get the new resource bundle for the tool and other OSP resource objects
     res = ResourceBundle.getBundle("org.opensourcephysics.resources.tools.tools", resourceLocale); //$NON-NLS-1$
     ControlsRes.setLocale(resourceLocale);
     DisplayRes.setLocale(resourceLocale);
     EjsRes.setLocale(resourceLocale);
     DialogsRes.setLocale(resourceLocale);
     support.firePropertyChange("locale", prev, resourceLocale); //$NON-NLS-1$
  }

  /**
   * Adds a PropertyChangeListener.
   *
   * @param property the name of the property (only "locale" accepted)
   * @param listener the object requesting property change notification
   */
  public static void addPropertyChangeListener(String property, PropertyChangeListener listener) {
     if(property.equals("locale")) { //$NON-NLS-1$
        support.addPropertyChangeListener(property, listener);
     }
  }

  /**
   * Removes a PropertyChangeListener.
   *
   * @param property the name of the property (only "locale" accepted)
   * @param listener the listener requesting removal
   */
  public static void removePropertyChangeListener(String property, PropertyChangeListener listener) {
     support.removePropertyChangeListener(property, listener);
  }
}
