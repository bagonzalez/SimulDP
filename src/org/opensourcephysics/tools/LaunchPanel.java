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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.LaunchNode.HTML;

/**
 * This is a panel that displays a tree with a LaunchNode root.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class LaunchPanel extends JPanel {
  // static fields
  protected static final String TEXT_TYPE = "text"; // text editor type //$NON-NLS-1$
  // instance fields
  protected JTree tree;
  protected DefaultTreeModel treeModel;
  protected JSplitPane splitPane;
  protected JPanel dataPanel;                       // on splitPane right
  protected JTabbedPane htmlTabs;                   // each tab is a Launcher.HTMLPane
  protected JEditorPane descriptionPane;
  protected JScrollPane descriptionScroller;
  protected boolean showAllNodes;
  protected Map<LaunchNode, VisibleNode> visibleNodeMap = new HashMap<LaunchNode, VisibleNode>();
  protected Launcher launcher;
  protected boolean rebuildingTabs;

  /**
   * Constructor.
   *
   * @param rootNode the root node
   * @param launcher the Launcher that is creating this panel
   */
  public LaunchPanel(LaunchNode rootNode, Launcher launcher) {
    showAllNodes = launcher instanceof LaunchBuilder;
    this.launcher = launcher;
    createGUI();
    createTree(rootNode);
    setSelectedNode(rootNode);
  }

  /**
   * Sets the selected node.
   *
   * @param node the node to select
   */
  public void setSelectedNode(LaunchNode node) {
    if(node==null) {
      return;
    }
    if(node==getSelectedNode()) {
      displayHTML(node);
    } else {
      tree.setSelectionPath(new TreePath(node.getPath()));
    }
  }

  /**
   * Sets the selected node.
   *
   * @param node the node to select
   * @param pageNumber the html page to display
   */
  public void setSelectedNode(LaunchNode node, int pageNumber) {
    setSelectedNode(node, pageNumber, null);
  }

  /**
   * Sets the selected node and displays a URL.
   *
   * @param node the node to select
   * @param pageNumber the html page to display
   * @param url the URL to display in the page
   */
  public void setSelectedNode(LaunchNode node, int pageNumber, java.net.URL url) {
    node.htmlTabNumber = ((url==null)&&(node.getHTMLCount()==0))
                         ? -1
                         : pageNumber;
    if (url != null) {
    	node.htmlURL = url;
    }
    setSelectedNode(node);
  }

  /**
   * Gets the selected node.
   *
   * @return the selected node
   */
  public LaunchNode getSelectedNode() {
    TreePath path = tree.getSelectionPath();
    if(path==null) {
      return null;
    }
    return(LaunchNode) path.getLastPathComponent();
  }

  /**
   * Gets the selected html page.
   *
   * @return the selected html tab
   */
  public int getSelectedHTMLPage() {
    return htmlTabs.getSelectedIndex();
  }

  /**
   * Gets the root node.
   *
   * @return the root node
   */
  public LaunchNode getRootNode() {
    return(LaunchNode) treeModel.getRoot();
  }

  // ______________________________ protected methods _____________________________

  /**
   * Returns the node with the same file name as the specified node.
   * May return null.
   *
   * @param node the node to match
   * @return the first node with the same file name
   */
  protected LaunchNode getClone(LaunchNode node) {
    if(node.getFileName()==null) {
      return null;
    }
    Enumeration<?> e = getRootNode().breadthFirstEnumeration();
    while(e.hasMoreElements()) {
      LaunchNode next = (LaunchNode) e.nextElement();
      if(node.getFileName().equals(next.getFileName())) {
        return next;
      }
    }
    return null;
  }

  /**
   * Displays the html for the specified node.
   *
   * @param node the LaunchNode
   */
  protected void displayHTML(LaunchNode node) {
    if(node!=null) {
      OSPLog.finer(LaunchRes.getString("Log.Message.NodeSelected") //$NON-NLS-1$
                   +" "+node);  //$NON-NLS-1$
      boolean isBuilder = launcher instanceof LaunchBuilder;
      String noTitle = LaunchRes.getString("HTMLTab.Title.Untitled"); //$NON-NLS-1$
      java.net.URL url = node.htmlURL;                                // what URL to display
      int tabNumber = node.htmlTabNumber;                             // which tab to display it in
      if((url==null)&&(node.getHTMLCount()>0)) {
        // node has multiple URLs so pick the tab-associated one
        tabNumber = Math.max(0, tabNumber);
        HTML html = node.getHTML(tabNumber);
        if(html.url!=null) {
          url = html.url;
        }
      }
      rebuildingTabs = true;
      // rebuild htmlTabs
      int tabCount = 0;
      if(!isBuilder) {
        htmlTabs.removeAll();
      }
      Iterator<?> it = node.htmlData.iterator();
      while(it.hasNext()) {
        final LaunchNode.HTML html = (LaunchNode.HTML) it.next();
        if(html.url!=null) {
          try {
            if(html.url.getContent()!=null) {
              final Launcher.HTMLPane tab = launcher.getHTMLTab(tabCount);
              final java.net.URL theURL = ((tabNumber==tabCount)&&(url!=null))
                                          ? url
                                          : html.url;
              Runnable runner = new Runnable() {
                public void run() {
                  try {
                    tab.editorPane.setPage(theURL);
                  } catch(IOException e) {}
                  if(theURL.getRef()!=null) {
                    tab.editorPane.scrollToReference(theURL.getRef());
                  }
                  launcher.setLinksEnabled(tab.editorPane, html.hyperlinksEnabled);
                }

              };
              if(SwingUtilities.isEventDispatchThread()) {
                runner.run();
              } else {
                SwingUtilities.invokeLater(runner);
              }
              if(!isBuilder) {
                String title = (html.title==null)
                               ? noTitle
                               : html.title;
                htmlTabs.addTab(title, tab.scroller);
                tabCount++;
              }
            }
          } catch(IOException ex) {
            OSPLog.fine(LaunchRes.getString("Log.Message.BadURL")  //$NON-NLS-1$
                        +" "+html.url);  //$NON-NLS-1$
          }
        }
      }
      // display appropriate tabs
      if(!isBuilder) {
        if(url!=null) {
          if((htmlTabs.getTabCount()==1)&&htmlTabs.getTitleAt(0).equals(noTitle)) {
            splitPane.setRightComponent(htmlTabs.getComponentAt(0));
          } else if(htmlTabs.getTabCount()>0) {
            splitPane.setRightComponent(htmlTabs);
            if(htmlTabs.getTabCount()>tabNumber) {
              htmlTabs.setSelectedIndex(tabNumber);
            }
          }
        } else {
          descriptionPane.setText(node.description);
          splitPane.setRightComponent(descriptionScroller);
        }
      }
      rebuildingTabs = false;
      launcher.refreshGUI();
    }
  }

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    setPreferredSize(new Dimension(400, 200));
    setLayout(new BorderLayout());
    splitPane = new JSplitPane();
    add(splitPane, BorderLayout.CENTER);
    dataPanel = new JPanel(new BorderLayout());
    descriptionPane = new JTextPane() {
      public void paintComponent(Graphics g) {
        if(OSPRuntime.antiAliasText) {
          Graphics2D g2 = (Graphics2D) g;
          RenderingHints rh = g2.getRenderingHints();
          rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
          rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        super.paintComponent(g);
      }

    };
    descriptionPane.setEditable(false);
    descriptionPane.setContentType(LaunchPanel.TEXT_TYPE);
    descriptionScroller = new JScrollPane(descriptionPane);
    // create the tabbed pane
    htmlTabs = new JTabbedPane(SwingConstants.TOP);
    htmlTabs.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        LaunchNode node = getSelectedNode();
        if((node!=null)&&(node==launcher.selectedNode)) {
        	if (rebuildingTabs && node.htmlURL != null) return;
          // save prev html properties
          node.prevTabNumber = node.htmlTabNumber;
          node.prevURL = node.htmlURL;
          // set new html properties
          int n = Math.max(0, htmlTabs.getSelectedIndex());
          node.htmlTabNumber = node.htmlData.isEmpty()
                               ? -1
                               : n;
          if(node.htmlTabNumber>-1) {
            Launcher.HTMLPane tab = launcher.getHTMLTab(node.htmlTabNumber);
            node.htmlURL = tab.editorPane.getPage();
          }
        }
      }

    });
    htmlTabs.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        LaunchNode node = getSelectedNode();
        if((node!=null)&&launcher.postEdits) {
          String nodePath = node.getPathString();
          Integer undoPage = new Integer(node.prevTabNumber);
          Integer redoPage = new Integer(node.htmlTabNumber);
          Object[] undoData = new Object[] {null, nodePath, undoPage, node.prevURL};
          Object[] redoData = new Object[] {null, nodePath, redoPage, node.htmlURL};
          LauncherUndo.NavEdit edit = launcher.undoManager.new NavEdit(undoData, redoData);
          launcher.undoSupport.postEdit(edit);
        }
      }

    });
    splitPane.setRightComponent(dataPanel);
    splitPane.setDividerLocation(160);
  }

  /**
   * Creates the tree.
   *
   * @param rootNode the root node
   */
  protected void createTree(LaunchNode rootNode) {
    // if not showing all nodes, create the VisibleNode structure
    if(!showAllNodes) {
      VisibleNode visibleRoot = new VisibleNode(rootNode);
      visibleNodeMap.put(rootNode, visibleRoot);
      addVisibleNodes(visibleRoot);
    }
    treeModel = new LaunchTreeModel(rootNode);
    tree = new JTree(treeModel);
    tree.setToolTipText(""); // enables tool tips for nodes //$NON-NLS-1$
    tree.setRootVisible(!rootNode.hiddenWhenRoot);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        LaunchNode node = launcher.getSelectedNode();
        if(launcher.postEdits) {
          // post undoable NavEdit if prev treePath is not null
          TreePath treePath = e.getOldLeadSelectionPath();
          if((treePath!=null)&&(node!=null)) {
            LaunchNode prevNode = (LaunchNode) treePath.getLastPathComponent();
            // set html properties of newly selected node
            if(!node.htmlData.isEmpty()) {
              int page = Math.max(0, node.htmlTabNumber);
              LaunchNode.HTML htmlData = node.htmlData.get(page);
              node.htmlURL = htmlData.url;
              node.htmlTabNumber = page;
            }
            LauncherUndo.NavEdit edit = launcher.undoManager.new NavEdit(prevNode, node);
            launcher.undoSupport.postEdit(edit);
          }
        }
        displayHTML(node);
      }

    });
    // put tree in a scroller and add to the split pane
    JScrollPane treeScroller = new JScrollPane(tree);
    splitPane.setLeftComponent(treeScroller);
  }

  /**
   * Returns a collection of nodes that are currently expanded.
   *
   * @return the expanded nodes
   */
  protected Collection<String> getExpandedNodes() {
    Collection<String> list = new ArrayList<String>();
    TreePath path = new TreePath(getRootNode());
    Enumeration<?> en = tree.getExpandedDescendants(path);
    while((en!=null)&&en.hasMoreElements()) {
      TreePath next = (TreePath) en.nextElement();
      LaunchNode node = (LaunchNode) next.getLastPathComponent();
      list.add(node.getPathString());
    }
    return list;
  }

  /**
   * Expands the specified nodes.
   *
   * @param expanded the nodes to expand
   */
  protected void setExpandedNodes(Collection<?> expanded) {
    Iterator<?> it = expanded.iterator();
    while(it.hasNext()) {
      // get node path
      String path = it.next().toString();
      Enumeration<?> e = getRootNode().breadthFirstEnumeration();
      while(e.hasMoreElements()) {
        LaunchNode node = (LaunchNode) e.nextElement();
        if(path.equals(node.getPathString())) { // found node
          // get treePath
          TreePath treePath = new TreePath(node.getPath());
          tree.expandPath(treePath);
        }
      }
    }
  }

  /**
   * A tree model class for launch nodes.
   */
  class LaunchTreeModel extends DefaultTreeModel {
    LaunchTreeModel(LaunchNode root) {
      super(root);
    }

    public Object getChild(Object parent, int index) {
      if(showAllNodes) {
        return super.getChild(parent, index);
      }
      VisibleNode visibleParent = visibleNodeMap.get(parent);
      if(visibleParent!=null) {
        VisibleNode visibleChild = (VisibleNode) visibleParent.getChildAt(index);
        if(visibleChild!=null) {
          return visibleChild.node;
        }
      }
      return null;
    }

    public int getChildCount(Object parent) {
      if(showAllNodes) {
        return super.getChildCount(parent);
      }
      VisibleNode visibleParent = visibleNodeMap.get(parent);
      if(visibleParent!=null) {
        return visibleParent.getChildCount();
      }
      return 0;
    }

    public int getIndexOfChild(Object parent, Object child) {
      if(showAllNodes) {
        return super.getIndexOfChild(parent, child);
      }
      VisibleNode visibleParent = visibleNodeMap.get(parent);
      VisibleNode visibleChild = visibleNodeMap.get(child);
      if((visibleParent!=null)&&(visibleChild!=null)) {
        return visibleParent.getIndex(visibleChild);
      }
      return -1;
    }

  }

  /**
   * A tree node that references a launch node.
   */
  private class VisibleNode extends DefaultMutableTreeNode {
    LaunchNode node;

    VisibleNode(LaunchNode node) {
      this.node = node;
    }

  }

  private void addVisibleNodes(VisibleNode visibleParent) {
    int n = visibleParent.node.getChildCount();
    for(int i = 0; i<n; i++) {
      LaunchNode child = (LaunchNode) visibleParent.node.getChildAt(i);
      if(child.isHiddenInLauncher()) {
        continue;
      }
      VisibleNode visibleChild = new VisibleNode(child);
      visibleNodeMap.put(child, visibleChild);
      visibleParent.add(visibleChild);
      addVisibleNodes(visibleChild);
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
