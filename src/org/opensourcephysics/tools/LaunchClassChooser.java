/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.OSPRuntime;

/**
 * This modal dialog lets the user choose launchable classes from jar files.
 */
public class LaunchClassChooser extends JDialog {
  // static fields
  private static Pattern pattern;
  private static Matcher matcher;
  private static Map<String, LaunchableClassMap> classMaps = new TreeMap<String, LaunchableClassMap>(); // maps path to classMap
  protected static boolean jarsOnly = true;

  // instance fields
  private JTextField searchField;
  private String defaultSearch = ""; //$NON-NLS-1$
  private String currentSearch = defaultSearch;
  private JScrollPane scroller;
  private JList choices;               // list of search results (class names)
  private LaunchableClassMap classMap; // map of  available launchable classes
  private boolean applyChanges = false;
  private JButton okButton;

  /**
   * Constructs an empty LaunchClassChooser dialog.
   *
   * @param owner the component that owns the dialog (may be null)
   */
  public LaunchClassChooser(Component owner) {
    super(JOptionPane.getFrameForComponent(owner), true);
    setTitle(LaunchRes.getString("ClassChooser.Frame.Title")); //$NON-NLS-1$
    JLabel textLabel = new JLabel(LaunchRes.getString("ClassChooser.Search.Label")+" "); //$NON-NLS-1$ //$NON-NLS-2$
    // create the buttons
    okButton = new JButton(LaunchRes.getString("ClassChooser.Button.Accept")); //$NON-NLS-1$
    okButton.setEnabled(false);
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyChanges = true;
        setVisible(false);
      }
    });
    JButton cancelButton = new JButton(LaunchRes.getString("ClassChooser.Button.Cancel")); //$NON-NLS-1$
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    // create the search field
    searchField = new JTextField(defaultSearch);
    searchField.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        Object obj = choices.getSelectedValue();
        search();
        choices.setSelectedValue(obj, true);
      }
    });
    getRootPane().setDefaultButton(okButton);
    // lay out the header pane
    JPanel headerPane = new JPanel();
    headerPane.setLayout(new BoxLayout(headerPane, BoxLayout.X_AXIS));
    headerPane.add(textLabel);
    headerPane.add(Box.createHorizontalGlue());
    headerPane.add(searchField);
    headerPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
    // lay out the scroll pane
    JPanel scrollPane = new JPanel(new BorderLayout());
    scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    // lay out the button pane
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
    buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    buttonPane.add(Box.createHorizontalGlue());
    buttonPane.add(okButton);
    buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonPane.add(cancelButton);
    // add everything to the content pane
    Container contentPane = getContentPane();
    contentPane.add(headerPane, BorderLayout.NORTH);
    contentPane.add(scrollPane, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.SOUTH);
    // create the scroll pane
    scroller = new JScrollPane();
    scroller.setPreferredSize(new Dimension(400, 300));
    scrollPane.add(scroller, BorderLayout.CENTER);
    pack();
    // center dialog on the screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width-this.getBounds().width)/2;
    int y = (dim.height-this.getBounds().height)/2;
    setLocation(x, y);
  }

  /**
   * Sets the path to be searched. The path must be a set of jar file names
   * separated by semicolons unless jarsOnly is set to false.
   *
   * @param path the search path
   * @return true if at least one jar file was successfully loaded
   */
  public boolean setPath(String path) {
    String[] jarNames = parsePath(path);
    // reset class map to null
    classMap = null;
    if(jarNames==null||jarNames.length==0) {
      return false;
    }
    // create classMaps key
    String key = ""; //$NON-NLS-1$
    for(int i = 0;i<jarNames.length;i++) {
      if(!key.equals("")) { //$NON-NLS-1$
      	// separate jars with semicolon
        key += ";"; //$NON-NLS-1$
      }
      key += jarNames[i];
    }
    // set the current classMap
    classMap = classMaps.get(key);
    if(classMap==null) {
      classMap = new LaunchableClassMap(jarNames);
      classMaps.put(key, classMap);
    }
    return true;
  }

  /**
   * Determines if the specified path is loaded. This will return true
   * only if the path is one or more jar files all of which are loaded.
   *
   * @param path the path
   * @return true if all jars in the path are loaded
   */
  public boolean isLoaded(String path) {
    if(classMap==null) {
      return false;
    }
    String[] jarNames = parsePath(path);
    for(int i = 0;i<jarNames.length;i++) {
      if(!classMap.includesJar(jarNames[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Chooses a launchable class and assigns it to the specified launch node.
   *
   * @param node the node
   * @return true if the class assignment is approved
   */
  public boolean chooseClassFor(LaunchNode node) {
    search();
    // select node's current launch class, if any
    choices.setSelectedValue(node.launchClassName, true);
    applyChanges = false;
    setVisible(true);
    if(!applyChanges) {
      return false;
    }
    Object obj = choices.getSelectedValue();
    if(obj==null) {
      return false;
    }
    String className = obj.toString();
    // get the class name and the launchable class
    node.launchClass = classMap.get(className);
    node.launchClassName = className;
    return true;
  }

  /**
   * Gets the class with the given name in the current class map.
   *
   * @param className the class name
   * @return the Class object, or null if not found
   */
  public Class<?> getClass(String className) {
    if(classMap==null) {
      return null;
    }
    return classMap.getClass(className);
  }

  /**
   * Gets the class with the given name in the specified path.
   *
   * @param classPath the path
   * @param className the class name
   * @return the Class object, or null if not found
   */
  public static Class<?> getClass(String classPath, String className) {
    if(classPath==null||className==null) {
      return null;
    }
    // get the classMap for the specified path
    String[] jarNames = parsePath(classPath);
    LaunchableClassMap classMap = getClassMap(jarNames);
    // get the class from the classMap
    return classMap.getClass(className);
  }

  /**
   * Gets the class with the given name in the specified path.
   *
   * @param classPath the path
   * @return the ClassLoader object, or null if not found
   */
  public static ClassLoader getClassLoader(String classPath) {
    if(classPath==null||classPath.equals("")) { //$NON-NLS-1$
      return null;
    }
    // get the classMap for the specified path
    String[] jarNames = parsePath(classPath);
    LaunchableClassMap classMap = getClassMap(jarNames);
    // get the class loader from the classMap
    return classMap.classLoader;
  }

  /**
   * Gets the launchable class map for the specified jar name array.
   *
   * @param jarNames the string array of jar names
   * @return the class map
   */
  private static LaunchableClassMap getClassMap(String[] jarNames) {
    // create a key string from the jar names
    String key = ""; //$NON-NLS-1$
    for(int i = 0;i<jarNames.length;i++) {
      if(!key.equals("")) { //$NON-NLS-1$
        key += ";"; //$NON-NLS-1$
      }
      key += jarNames[i];
    }
    // get the classMap for the key
    LaunchableClassMap classMap = classMaps.get(key);
    if(classMap==null) {
      classMap = new LaunchableClassMap(jarNames);
      classMaps.put(key, classMap);
    }
    return classMap;
  }

  /**
   * Searches using the current search field text.
   */
  private void search() {
    if(classMap==null) {
      return;
    }
    classMap.loadAllLaunchables();
    if(search(searchField.getText())) {
      currentSearch = searchField.getText();
      searchField.setBackground(Color.white);
    } else {
      JOptionPane.showMessageDialog(this, LaunchRes.getString("Dialog.InvalidRegex.Message")+" \""+searchField.getText()+"\"", LaunchRes.getString("Dialog.InvalidRegex.Title"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      searchField.setText(currentSearch);
    }
  }

  /**
   * Searches for class names using a regular expression string
   * and puts matches into the class chooser list of choices.
   *
   * @param regex the regular expression
   * @return true if the search succeeded (even if no matches found)
   */
  private boolean search(String regex) {
    regex = regex.toLowerCase();
    okButton.setEnabled(false);
    try {
      pattern = Pattern.compile(regex);
    } catch(Exception ex) {
      return false;
    }
    ArrayList<String> matches = new ArrayList<String>();
    for(Iterator<?> it = classMap.keySet().iterator();it.hasNext();) {
      String name = (String)it.next();
      matcher = pattern.matcher(name.toLowerCase());
      if(matcher.find()) {
        matches.add(name);
      }
    }
    Object[] results = matches.toArray();
    choices = new JList(results);
    choices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    choices.setFont(searchField.getFont());
    choices.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        JList theList = (JList) e.getSource();
        okButton.setEnabled(!theList.isSelectionEmpty());
      }
    });
    choices.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        JList theList = (JList) e.getSource();
        if(e.getClickCount()==2&&!theList.isSelectionEmpty()) {
          okButton.doClick();
        }
      }
    });
    scroller.getViewport().setView(choices);
    return true;
  }

  /**
   * Parses the specified path into path tokens (at semicolons).
   *
   * @param path the path
   * @param jarsOnly true if only ".jar" names are returned
   * @return an array of path names
   */
  static String[] parsePath(String path) {
  	return parsePath(path, jarsOnly);
  }

  /**
   * Parses the specified path into path tokens (at semicolons).
   *
   * @param path the path
   * @param jarsOnly true if only ".jar" names are returned
   * @return an array of path names
   */
  static String[] parsePath(String path, boolean jarsOnly) {
    Collection<String> tokens = new ArrayList<String>();
    // get the first path token
    String next = path;
    int i = path.indexOf(";"); //$NON-NLS-1$
    if(i!=-1) {
      next = path.substring(0, i);
      path = path.substring(i+1);
    } else {
      path = ""; //$NON-NLS-1$
    }
    // iterate thru the path tokens, trim and add to token list
    while(next.length() > 0) {
      if(!jarsOnly||next.endsWith(".jar")) { //$NON-NLS-1$
        tokens.add(next);
      }
      i = path.indexOf(";"); //$NON-NLS-1$
      if(i==-1) {
        next = path.trim();
        path = ""; //$NON-NLS-1$
      } else {
        next = path.substring(0, i).trim();
        path = path.substring(i+1).trim();
      }
    }
    return tokens.toArray(new String[0]);
  }
}

/**
 * A map of jar/class name to launchable Class object.
 * The name of the jar is prepended to the class name.
 */

class LaunchableClassMap extends TreeMap<String, Class<?>> {
  // instance fields
  ClassLoader classLoader; // loads classes
  String[] jarNames; // names of jar files (paths relative to launch jar directory)
  boolean allLoaded = false;

  // constructor creates class loader
  LaunchableClassMap(String[] jarNames) {
    this.jarNames = jarNames;
    // create a URL for each jar
    Collection<URL> urls = new ArrayList<URL>();
    // changed by D Brown 2007-11-06 for Linux
  	String jarBase = OSPRuntime.getLaunchJarDirectory();
    for(int i = 0;i<jarNames.length;i++) {
    	String jarPath = XML.getResolvedPath(jarNames[i], jarBase);
      try {
        urls.add(new URL("file:" + jarPath)); //$NON-NLS-1$
      }
      catch (MalformedURLException ex) {
        OSPLog.info(ex + " " + jarPath); //$NON-NLS-1$
      }
    }
    // create the class loader
    classLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]));
  }

  /**
   * Loads a class from the URLClassLoader or, if this fails, from the
   * current class loader.
   *
   * @param name the class name
   * @return the Class
   * @throws ClassNotFoundException
   */
  Class <?>smartLoadClass(String name) throws ClassNotFoundException {
    try {
      return classLoader.loadClass(name);
    } catch(ClassNotFoundException e) { // added by Kip Barros
      return this.getClass().getClassLoader().loadClass(name);
    }
  }

  // loads all launchable classes
  void loadAllLaunchables() {
    if(allLoaded) {
      return;
    }
    JApplet applet = org.opensourcephysics.display.OSPRuntime.applet;
    // for each jar name
    for(int i = 0;i<jarNames.length;i++) {
      // create a JarFile
      JarFile jar = null;
      try {
        if (applet == null) { // application mode
          jar = new JarFile(jarNames[i]);
        }
        else {
          String path = XML.getResolvedPath(jarNames[i], applet.getCodeBase().toExternalForm());
          // create a URL that refers to a jar file on the web
          URL url = new URL("jar:" + path + "!/"); //$NON-NLS-1$ //$NON-NLS-2$
          // get the jar
          JarURLConnection conn = (JarURLConnection)url.openConnection();
          jar = conn.getJarFile();
        }
      } catch(IOException ex) {
        OSPLog.info(ex.getClass().getName() + ": " + ex.getMessage()); //$NON-NLS-1$
      } catch(SecurityException ex) {
        OSPLog.info(ex.getClass().getName() + ": " + ex.getMessage()); //$NON-NLS-1$
      }
      if(jar==null) {
        continue;
      }
      // iterate thru JarFile entries
      for(Enumeration<JarEntry> e = jar.entries();e.hasMoreElements();) {
        JarEntry entry = e.nextElement();
        String name = entry.getName();
        // ignore manifest and inner classes
        if(name.endsWith(".class")&&name.indexOf("$")==-1) { //$NON-NLS-1$ //$NON-NLS-2$
          // remove class extension and replace slashes
          name = name.substring(0, name.indexOf(".class")); //$NON-NLS-1$
          int j = name.indexOf("/"); //$NON-NLS-1$
          while(j!=-1) {
            name = name.substring(0, j)+"."+name.substring(j+1); //$NON-NLS-1$
            j = name.indexOf("/"); //$NON-NLS-1$
          }
          // skip to next name if class is already loaded
          if(get(name)!=null) {
            continue;
          }
          try {
            // load the class and, if launchable, store it with class name key
            Class<?> next = smartLoadClass(name); // changed by Kip Barros
            if(Launcher.isLaunchable(next)) {
              put(name, next);
            }
          } catch(ClassNotFoundException ex) {/** empty block */}
          catch(NoClassDefFoundError err) {
            OSPLog.info(err.toString());
          }
        }
      }
    }
    allLoaded = true;
  }

  // returns true of this classMap includes specified jar
  boolean includesJar(String jarName) {
    for(int i = 0;i<jarNames.length;i++) {
      if(jarNames[i].equals(jarName)) {
        return true;
      }
    }
    return false;
  }

  // gets the specified class, or null if not loadable or launchable
  Class<?> getClass(String className) {
    Class<?> type = get(className);
    if(type!=null||allLoaded) {
      return type;
    }
    try {
      // load the class and, if launchable, return it
      type = smartLoadClass(className); // changed by Kip Barros
      if(Launcher.isLaunchable(type)) {
        return type;
      }
    } catch(ClassNotFoundException ex) {/** empty block */}
    catch(NoClassDefFoundError err) {
      OSPLog.info(err.toString());
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
