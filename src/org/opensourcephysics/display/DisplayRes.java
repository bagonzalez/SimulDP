package org.opensourcephysics.display;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Locale;

/**
 * DisplayRes provides access to internationalized string resources for objects in the display package.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class DisplayRes {

   private static String BUNDLE_NAME = "org.opensourcephysics.resources.display.display_res"; //$NON-NLS-1$
   private static ResourceBundle res = ResourceBundle.getBundle(BUNDLE_NAME);
   private DisplayRes() {}

   public static void setLocale(Locale locale) {
      res = ResourceBundle.getBundle(BUNDLE_NAME, locale);
   }

   public static String getString(String key) {
      try {
         return res.getString(key);
      } catch(MissingResourceException e) {
         return '!'+key+'!';
      }
   }
}
