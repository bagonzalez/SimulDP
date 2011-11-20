/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import org.opensourcephysics.display.*;

/**
 * Utility classes to work with Ejs at a high level
 *
 * @author Francisco Esquembre (http://fem.um.es)
 * @version 1.0
 */
public class EjsTool {
  static private final String INFO_FILE = ".Ejs.txt"; //$NON-NLS-1$
  static public final String GET_MODEL_METHOD = "_getEjsModel"; //$NON-NLS-1$
  static public final String GET_RESOURCES_METHOD = "_getEjsResources"; //$NON-NLS-1$

  // ---- Localization

  static private final String BUNDLE_NAME = "org.opensourcephysics.resources.tools.tools"; //$NON-NLS-1$
  static private ResourceBundle res = ResourceBundle.getBundle(BUNDLE_NAME);

  static public void setLocale(Locale locale){
    res = ResourceBundle.getBundle(BUNDLE_NAME, locale);
  }

  static public String getString(String key) {
    try { return res.getString(key); }
    catch (MissingResourceException e) { return '!' + key + '!'; }
  }

  // --- End of localization

  /**
   * Whether a class provides an Ejs model.
   * @param _ejsClass Class
   * @return boolean
   */
  static public boolean hasEjsModel (Class<?> _ejsClass) {
    try {
      Class<?> [] c = {};
      java.lang.reflect.Method getModelMethod = _ejsClass.getMethod(GET_MODEL_METHOD,c);
      return getModelMethod!=null;
    }
    catch (Exception _exc) { return false; }
  }

  /**
   * Runs the Ejs model corresponding to the given class.
   * The model and resources required will be extracted using
   * the ResourceLoader utility.
   * @param _ejsClass Class
   * @return boolean true if successful
   */
  @SuppressWarnings("unchecked")
  static public boolean runEjs(Class<?> _ejsClass) {
    try {
      Class<?> [] c = {};
      java.lang.reflect.Method getModelMethod = _ejsClass.getMethod(GET_MODEL_METHOD,c);
      java.lang.reflect.Method getResourcesMethod = _ejsClass.getMethod(GET_RESOURCES_METHOD,c);
      Object[] o = {};
      String model = (String) getModelMethod.invoke(null,o);
      java.util.Set<String> list;
      if (getResourcesMethod!=null) list = (java.util.Set<String>) getResourcesMethod.invoke(null,o);
      else list = new java.util.HashSet<String>();
      return doRunEjs(model,list,_ejsClass);
    }
    catch (Exception _exc) {
      _exc.printStackTrace();
      String[] message=new String[]{res.getString("EjsTool.EjsNotRunning") ,res.getString("EjsTool.NoModel")+" "+_ejsClass.getName()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      JOptionPane.showMessageDialog((JFrame)null,message,res.getString("EjsTool.Error"),JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
      return false;
    }
  }

  /**
   * To be used by EJS version 3.47 and earlier only.
   * It is here to make sure that the information saved matches the
   * information read by runEjs().
   * @param _release String
   */
  static public void saveInformation(String _home,String _release) {
    try {
      String filename = System.getProperty("user.home").replace('\\','/'); //$NON-NLS-1$
      if (!filename.endsWith("/")) filename = filename + "/"; //$NON-NLS-1$ //$NON-NLS-2$
      filename = filename + INFO_FILE;
      String dir = System.getProperty("user.dir"); //$NON-NLS-1$
      FileWriter fout = new FileWriter(filename);
      fout.write("directory = "+dir+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
      fout.write("home = "+_home+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
      fout.write("version = "+_release+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
      fout.close();
    } catch (Exception exc) { exc.printStackTrace(); }
  }

  /**
   * To be used by EJS version 4.0 and later only.
   * It is here to make sure that the information saved matches the
   * information read by runEjs().
   * @param _binDirectoryPath String The binary directory from which EJS was launched
   * @param _consoleOptionsFilePath String The location of the console options file
   * @param _sourceDirectoryPath String The source directory path
   * @param _release String The EJS release version
   */
  static public void saveInformation (String _binDirectoryPath, String _sourceDirectoryPath, String _release) {
    try {
      String filename = System.getProperty("user.home").replace('\\','/'); //$NON-NLS-1$
      if (!filename.endsWith("/")) filename = filename + "/"; //$NON-NLS-1$ //$NON-NLS-2$
      FileWriter fout = new FileWriter(filename + INFO_FILE);
      fout.write("ejs_root_directory = "+_binDirectoryPath+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
      //fout.write("console_options = "+_consoleOptionsFilePath+"\n");
      fout.write("source_directory = "+_sourceDirectoryPath+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
      fout.write("version = "+_release+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
      fout.close();
    } catch (Exception exc) { exc.printStackTrace(); }
  }

  //------------------------------------
  // Private methods and inner classes
  //------------------------------------

  /**
   * Gets the path of a file in standard form.
   * If it is a directory, the path ends in "/"
   */
  static public String getPath (File _file) {
    String path;
    try { path = _file.getCanonicalPath(); }
    catch (Exception exc) { path = _file.getAbsolutePath(); }
    if (org.opensourcephysics.display.OSPRuntime.isWindows()) {
      path = path.replace('\\','/');
      // Sometimes the system provides c:, sometimes C:\
      int a = path.indexOf(':');
      if (a>0) path = path.substring(0,a).toUpperCase()+path.substring(a);
    }
    if (_file.isDirectory() && !path.endsWith("/")) path = path + "/"; //$NON-NLS-1$ //$NON-NLS-2$
    return path;
  }

  /**
   * Extracts an EJS model (and its resource files) from the given
   * source and then runs EJS with that model. The example is extracted
   * in the source directory of the users' EJS. The user will be warned
   * before overwriting any file.
   * @return boolean
   */
  static private boolean doRunEjs(String _model, java.util.Set<String> _resources, Class<?> _ejsClass) {
    String ejsRootDirPath = null;
    //String console_options = null;
    String sourceDirPath = null;
    String version = null;
    File ejsRootDirectory=null;
    java.awt.Component parentComponent = null;
    try {
      String filename = System.getProperty("user.home").replace('\\','/'); //$NON-NLS-1$
      if (!filename.endsWith("/")) filename = filename + "/"; //$NON-NLS-1$ //$NON-NLS-2$
      Reader reader=new FileReader(filename + INFO_FILE);
      LineNumberReader l = new LineNumberReader(reader);
      String sl = l.readLine();
      while (sl != null) {
        if      (sl.startsWith("ejs_root_directory = ")) ejsRootDirPath = sl.substring("ejs_root_directory = ".length()).trim(); //$NON-NLS-1$ //$NON-NLS-2$
        //else if (sl.startsWith("console_options = "))    console_options = sl.substring("console_options = ".length()).trim();
        else if (sl.startsWith("source_directory = "))   sourceDirPath = sl.substring("source_directory = ".length()).trim(); //$NON-NLS-1$ //$NON-NLS-2$
        else if (sl.startsWith("version = "))            version = sl.substring("version = ".length()).trim(); //$NON-NLS-1$ //$NON-NLS-2$
        sl = l.readLine();
      }
      reader.close();
      int major = 3; 
      if (version!=null) {
        int index = version.indexOf('.');
        if (index>=0) major = Integer.parseInt(version.substring(0,index));
      }
      if (major<4) { // Incorrect version, update to 4.0
        JOptionPane.showMessageDialog(parentComponent,version+" "+res.getString("EjsTool.IncorrectVersion"), //$NON-NLS-1$ //$NON-NLS-2$
            res.getString("EjsTool.Error"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
        return false;
      }
      // See if EjsConsole.jar is there
      ejsRootDirectory = new File(ejsRootDirPath);
      if (!new File (ejsRootDirectory,"EjsConsole.jar").exists()) ejsRootDirectory = null; //$NON-NLS-1$
    } 
    catch (Exception exc) {
      exc.printStackTrace(); 
      ejsRootDirectory = null; 
    }
    
    if (ejsRootDirectory==null) {
      // Create a chooser
      JFileChooser chooser = OSPRuntime.createChooser("", new String[]{}); //$NON-NLS-1$
      chooser.setDialogTitle(res.getString("EjsTool.EjsNotFound")); //$NON-NLS-1$
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setMultiSelectionEnabled(false);

      // The message
      JTextArea textArea   = new JTextArea (res.getString("EjsTool.IndicateRootDir")); //$NON-NLS-1$
      textArea.setWrapStyleWord(true);
      textArea.setLineWrap(true);
      textArea.setEditable(false);
      textArea.setFont(textArea.getFont().deriveFont(java.awt.Font.BOLD));
      textArea.setPreferredSize(new java.awt.Dimension(150,60));
      textArea.setBackground(chooser.getBackground());
      textArea.setBorder(new javax.swing.border.EmptyBorder(5,10,0,0));
      chooser.setAccessory(textArea);

      // Check that it exists or ask the user for it
      while (ejsRootDirectory==null) {
        if (chooser.showOpenDialog((java.awt.Component)null) != JFileChooser.APPROVE_OPTION) return false; // The user canceled
        ejsRootDirectory = chooser.getSelectedFile();
        if (ejsRootDirectory==null) return false; // The user canceled
        if (!new File (ejsRootDirectory,"EjsConsole.jar").exists()) ejsRootDirectory = null; //$NON-NLS-1$
      }
    }
    File sourceDir = new File (sourceDirPath);
    if (!sourceDir.exists()) sourceDir.mkdirs();
    
    // Extract the model and auxiliary files
    java.util.List<String> extractList = new ArrayList<String>();
    
    // Make relative files relative
    String modelPath = _model;
    int length=0;
    int index = modelPath.lastIndexOf('/');
    if (index>=0) {
      _model = "./"+modelPath.substring(index+1); //$NON-NLS-1$
      modelPath = modelPath.substring(0,index+1); // including the '/'
      length = modelPath.length();
    }
    if (!_resources.contains(_model)) _resources.add(_model); // Make sure the model is there
    if (length>0) for (String res : _resources) extractList.add(res.startsWith(modelPath) ? "./"+res.substring(length) : res); //$NON-NLS-1$
    else extractList.addAll(_resources);
    java.util.Collections.sort(extractList);
    
    JCheckBox quitCheckBox = new JCheckBox(res.getString("EjsTool.QuitSimulation"),true); //$NON-NLS-1$
    java.util.List<?> finalList = ejsConfirmList(parentComponent,new java.awt.Dimension(400,400),
      res.getString("EjsTool.ExtractingFiles"),res.getString("EjsTool.Message"),extractList, quitCheckBox); //$NON-NLS-1$ //$NON-NLS-2$
    if (finalList==null) return false; // The user canceled
    
    /* Add the "files" directory to the ResourceLoader
    String filesDir=null;
    if (ResourceLoader.getResource(_model)==null) { // Search in the class "files" directory
      filesDir = _ejsClass.getName().replace('.', '/') + "/files";
      ResourceLoader.addSearchPath(filesDir);
    }
    */

    // Create a chooser
    JFileChooser chooser = OSPRuntime.createChooser("", new String[]{}); //$NON-NLS-1$
    chooser.setDialogTitle(res.getString("EjsTool.ChooseDestinationDirectory")); //$NON-NLS-1$
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    chooser.setCurrentDirectory(sourceDir);
    // Choose a correct destination directory
    sourceDirPath = EjsTool.getPath(sourceDir);
    File destinationDirectory = null;
    String relativeDir=""; //$NON-NLS-1$
    while (destinationDirectory==null) {
      if (chooser.showOpenDialog((java.awt.Component)null) != JFileChooser.APPROVE_OPTION) return false; // The user canceled
      destinationDirectory = chooser.getSelectedFile();
      if (destinationDirectory==null) return false; // The user canceled
      String destDirPath = EjsTool.getPath(destinationDirectory); 
      if (!destDirPath.startsWith(sourceDirPath)) {
        JOptionPane.showMessageDialog(parentComponent,res.getString("EjsTool.MustBeUnderSource"), //$NON-NLS-1$
            res.getString("EjsTool.Error"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
        destinationDirectory = null;
      }
      else relativeDir = destDirPath.substring(sourceDirPath.length());
    }
    
    int policy = JarTool.NO;
    for (Iterator<?> it = finalList.iterator(); it.hasNext(); ) {
      String resource = (String) it.next();
      File targetFile = resource.startsWith("./") ? new File (destinationDirectory,resource.substring(2)) : new File(sourceDir,resource); //$NON-NLS-1$
      if (targetFile.exists()) {
        switch (policy) {
          case JarTool.NO_TO_ALL : continue;
          case JarTool.YES_TO_ALL : break; // will overwrite
          default :
            switch (policy = JarTool.confirmOverwrite(resource)) {
              case JarTool.NO_TO_ALL:
              case JarTool.NO : continue;
              default : // Do nothing, i.e., will overwrite the file
            }
        }
      }
      String originalName = resource.startsWith("./") ? modelPath+resource.substring(2) : resource; //$NON-NLS-1$
      File result = JarTool.extract (originalName,targetFile); // Use the ResourceLoader
      if (result==null) {
        String[] message=new String[]{res.getString("JarTool.FileNotExtracted"), //$NON-NLS-1$
            originalName+" "+res.getString("JarTool.FileNotExtractedFrom")+" "+_ejsClass.toString()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        JOptionPane.showMessageDialog((JFrame)null,message,res.getString("JarTool.Error"),JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
        return false;
      }
    }

    // Now run the EJS console
    //final String theOptions = console_options;
    final String theModel = relativeDir + (_model.startsWith("./") ? _model.substring(2) : _model); //$NON-NLS-1$
    final File theDir = ejsRootDirectory;
    Runnable runner = new Runnable() {
      public void run () {
        try {
          final Vector<String> cmd = new Vector<String>();
          String javaHome = System.getProperty("java.home"); //$NON-NLS-1$
          if (javaHome != null) cmd.add(javaHome + java.io.File.separator + "bin" + java.io.File.separator + "java"); //$NON-NLS-1$ //$NON-NLS-2$
          else cmd.add("java"); //$NON-NLS-1$
          //if (theOptions!=null) cmd.add("-Dejs.console_options="+theOptions);
          cmd.add("-jar"); //$NON-NLS-1$
          cmd.add("EjsConsole.jar"); //$NON-NLS-1$
          cmd.add("-file"); //$NON-NLS-1$
          cmd.add(theModel);
          String[] cmdarray = cmd.toArray(new String[0]);
//          for (int i=0; i<cmdarray.length; i++) System.out.println ("Trying to run ["+i+"] = "+cmdarray[i]);
          Process proc = Runtime.getRuntime().exec(cmdarray, null, theDir);
          proc.waitFor();
        }
        catch (Exception exc) { exc.printStackTrace(); }
      }
    };
    java.lang.Thread thread = new Thread(runner);
    thread.setPriority(Thread.NORM_PRIORITY);
    thread.start();
    return quitCheckBox.isSelected();
  }

  public static java.util.List<?> ejsConfirmList (Component _target, Dimension _size, 
      String _message, String _title, java.util.List<?> _list) {
    return ejsConfirmList (_target, _size, _message, _title, _list, (JComponent) null);
  }

  /**
   * This method receives a list of objects which it exposes to the user.
   * The user can remove objects from the list and click OK.
   * The class then returns the modified list.
   * If the user clicks cancel it will return null
   * @param _target Component The dialog will be shown relative to this component
   * @param _size Dimension The size of the display dialog
   * @param _message String The message to display
   * @param _title String The title for the display dialog
   * @param _list java.util.List<?> The initial list of objects
   * @param _bottomComponent JComponent and additional component to show at the bottom
   * @return AbstractList
   */
  public static java.util.List<?> ejsConfirmList (Component _target, Dimension _size, 
      String _message, String _title, java.util.List<?> _list, JComponent _bottomComponent) {
    class ReturnValue { boolean value = false; }
    final ReturnValue returnValue=new ReturnValue();

    final DefaultListModel listModel = new DefaultListModel();
    for (int i=0,n=_list.size(); i<n; i++) listModel.addElement(_list.get(i));
    final JList list = new JList(listModel);
    list.setEnabled(true);
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    list.setSelectionInterval(0,listModel.getSize()-1);
    JScrollPane scrollPane = new JScrollPane(list);
    scrollPane.setPreferredSize(_size);

    final JDialog dialog = new JDialog();

    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if      (aCmd.equals("ok"))      { returnValue.value = true;  dialog.setVisible (false); } //$NON-NLS-1$
        else if (aCmd.equals("cancel"))  { returnValue.value = false; dialog.setVisible (false); } //$NON-NLS-1$
        else if (aCmd.equals("selectall")) { list.setSelectionInterval(0,listModel.getSize()-1); } //$NON-NLS-1$
        else if (aCmd.equals("selectnone")) { list.removeSelectionInterval(0,listModel.getSize()-1); } //$NON-NLS-1$
      }
    };

    JButton okButton = new JButton (DisplayRes.getString("GUIUtils.Ok")); //$NON-NLS-1$
    okButton.setActionCommand ("ok"); //$NON-NLS-1$
    okButton.addMouseListener (mouseListener);

    JButton cancelButton = new JButton (DisplayRes.getString("GUIUtils.Cancel")); //$NON-NLS-1$
    cancelButton.setActionCommand ("cancel"); //$NON-NLS-1$
    cancelButton.addMouseListener (mouseListener);

    JButton excludeButton = new JButton (DisplayRes.getString("GUIUtils.SelectAll")); //$NON-NLS-1$
    excludeButton.setActionCommand ("selectall"); //$NON-NLS-1$
    excludeButton.addMouseListener (mouseListener);

    JButton includeButton = new JButton (DisplayRes.getString("GUIUtils.SelectNone")); //$NON-NLS-1$
    includeButton.setActionCommand ("selectnone"); //$NON-NLS-1$
    includeButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton);
    buttonPanel.add (excludeButton);
    buttonPanel.add (includeButton);
    buttonPanel.add (cancelButton);

    JPanel topPanel = new JPanel (new BorderLayout ());

    JTextArea textArea   = new JTextArea (_message);
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setEditable(false);
    textArea.setFont(textArea.getFont().deriveFont(Font.BOLD));
    textArea.setBackground(topPanel.getBackground());
    textArea.setBorder(new javax.swing.border.EmptyBorder(5,5,10,5));

    topPanel.setBorder(new javax.swing.border.EmptyBorder(5,10,5,10));
    topPanel.add (textArea,BorderLayout.NORTH);
    topPanel.add (scrollPane,BorderLayout.CENTER);
    if (_bottomComponent!=null) topPanel.add (_bottomComponent,BorderLayout.SOUTH);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel southPanel = new JPanel (new java.awt.BorderLayout());
    southPanel.add (sep1,java.awt.BorderLayout.NORTH);
    southPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    dialog.getContentPane().setLayout (new java.awt.BorderLayout(5,0));
    dialog.getContentPane().add (topPanel,java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add (southPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) { returnValue.value = false; }
      }
    );

//    dialog.setSize (_size);
    dialog.validate();
    dialog.pack();
    dialog.setTitle (_title);
    dialog.setLocationRelativeTo (_target);
    dialog.setModal(true);

    dialog.setVisible (true);
    if (!returnValue.value) return null;
    Object[] selection = list.getSelectedValues();
    java.util.List<Object> newList = new ArrayList<Object>();
    for (int i=0,n=selection.length; i<n; i++) newList.add(selection[i]);
    return newList;
  }



} // End of class

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
