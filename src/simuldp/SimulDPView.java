/*
 * SimulDPView.java
 */

package simuldp;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import simulacion.ControlSim;
import java.io.File;

/**
 * The application's main frame.
 */
public class SimulDPView extends FrameView {

    public SimulDPView(SingleFrameApplication app, ControlSim control) {
        super(app);       
        this.control=control;
        initComponents();
        this.CrearArbol();
      

        getFrame().setTitle("Generador de Observaciones Aleatorias - TDS115");
        getFrame().setResizable(false);

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = SimulDPApp.getApplication().getMainFrame();
            aboutBox = new SimulDPAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        SimulDPApp.getApplication().show(aboutBox);
    }

    public void CrearArbol(){
        File Directorio=new File("C:\\SimulDP\\ObsData\\Poi\\");
        String[] Direct;
        Direct=Directorio.list();

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Distribuciones");
        
        javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Poisson");
         treeNode1.add(treeNode2);
        javax.swing.tree.DefaultMutableTreeNode treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Geometrica");
        treeNode1.add(treeNode3);
        javax.swing.tree.DefaultMutableTreeNode treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("Uniforme");
        treeNode1.add(treeNode4);
        javax.swing.tree.DefaultMutableTreeNode treeNode5 = new javax.swing.tree.DefaultMutableTreeNode("Exponencial");
        treeNode1.add(treeNode5);
        javax.swing.tree.DefaultMutableTreeNode treeNode6 = new javax.swing.tree.DefaultMutableTreeNode("Normal");
        treeNode1.add(treeNode6);

        javax.swing.tree.DefaultMutableTreeNode treeNode8 = new javax.swing.tree.DefaultMutableTreeNode("Resultados");
        treeNode1.add(treeNode8);
        javax.swing.tree.DefaultMutableTreeNode treeNode7 = new javax.swing.tree.DefaultMutableTreeNode("");

        for(int i=0; i<Direct.length; i++)
        {
            treeNode7 = new javax.swing.tree.DefaultMutableTreeNode(Direct[i]);
            treeNode2.add(treeNode7);
        }

        Directorio=new File("C:\\SimulDP\\ObsData\\Geo\\");
        Direct=Directorio.list();

        for(int i=0; i<Direct.length; i++)
        {
            treeNode7 = new javax.swing.tree.DefaultMutableTreeNode(Direct[i]);
            treeNode3.add(treeNode7);
        }

        Directorio=new File("C:\\SimulDP\\ObsData\\Uni\\");
        Direct=Directorio.list();

        for(int i=0; i<Direct.length; i++)
        {
            treeNode7 = new javax.swing.tree.DefaultMutableTreeNode(Direct[i]);
            treeNode4.add(treeNode7);
        }

        Directorio=new File("C:\\SimulDP\\ObsData\\Exp\\");
        Direct=Directorio.list();

        for(int i=0; i<Direct.length; i++)
        {
            treeNode7 = new javax.swing.tree.DefaultMutableTreeNode(Direct[i]);
            treeNode5.add(treeNode7);
        }

        Directorio=new File("C:\\SimulDP\\ObsData\\Nor\\");
        Direct=Directorio.list();

        for(int i=0; i<Direct.length; i++)
        {
            treeNode7 = new javax.swing.tree.DefaultMutableTreeNode(Direct[i]);
            treeNode6.add(treeNode7);
        }

        Directorio=new File("C:\\SimulDP\\Resultados\\");
        Direct=Directorio.list();

        for(int i=0; i<Direct.length; i++)
        {
            treeNode7 = new javax.swing.tree.DefaultMutableTreeNode(Direct[i]);
            treeNode8.add(treeNode7);
        }
         ListaObservaciones.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
       
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      mainPanel = new javax.swing.JPanel();
      jPanel1 = new javax.swing.JPanel();
      jPanel2 = new javax.swing.JPanel();
      jButton1 = new javax.swing.JButton();
      jButton2 = new javax.swing.JButton();
      jLabel1 = new javax.swing.JLabel();
      jPanel3 = new javax.swing.JPanel();
      jButton3 = new javax.swing.JButton();
      jButton4 = new javax.swing.JButton();
      jButton5 = new javax.swing.JButton();
      jLabel2 = new javax.swing.JLabel();
      jPanel4 = new javax.swing.JPanel();
      jScrollPane1 = new javax.swing.JScrollPane();
      ListaObservaciones = new javax.swing.JTree();
      jButton6 = new javax.swing.JButton();
      jButton7 = new javax.swing.JButton();
      jLabel3 = new javax.swing.JLabel();
      menuBar = new javax.swing.JMenuBar();
      javax.swing.JMenu fileMenu = new javax.swing.JMenu();
      javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
      javax.swing.JMenu helpMenu = new javax.swing.JMenu();
      javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
      statusPanel = new javax.swing.JPanel();
      javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
      statusMessageLabel = new javax.swing.JLabel();
      statusAnimationLabel = new javax.swing.JLabel();
      progressBar = new javax.swing.JProgressBar();

      mainPanel.setName("mainPanel"); // NOI18N

      jPanel1.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 2, 2, 2, new java.awt.Color(102, 204, 255)));
      jPanel1.setName("jPanel1"); // NOI18N

      jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Distribuciones Discretas"));
      jPanel2.setName("jPanel2"); // NOI18N

      org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(simuldp.SimulDPApp.class).getContext().getResourceMap(SimulDPView.class);
      jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
      jButton1.setName("jButton1"); // NOI18N
      jButton1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton1ActionPerformed(evt);
         }
      });

      jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
      jButton2.setName("jButton2"); // NOI18N
      jButton2.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton2ActionPerformed(evt);
         }
      });

      jLabel1.setIcon(resourceMap.getIcon("jLabel1.icon")); // NOI18N
      jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
      jLabel1.setName("jLabel1"); // NOI18N

      org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel2Layout.createSequentialGroup()
            .add(18, 18, 18)
            .add(jLabel1)
            .addContainerGap(18, Short.MAX_VALUE))
         .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
            .addContainerGap(37, Short.MAX_VALUE)
            .add(jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 128, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(18, 18, 18)
            .add(jButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 129, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(32, 32, 32))
      );
      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .add(jLabel1)
            .add(18, 18, 18)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jButton2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
               .add(jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
      );

      jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Distribuciones Continuas"));
      jPanel3.setName("jPanel3"); // NOI18N

      jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
      jButton3.setName("jButton3"); // NOI18N
      jButton3.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton3ActionPerformed(evt);
         }
      });

      jButton4.setText(resourceMap.getString("jButton4.text")); // NOI18N
      jButton4.setName("jButton4"); // NOI18N
      jButton4.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton4ActionPerformed(evt);
         }
      });

      jButton5.setText(resourceMap.getString("jButton5.text")); // NOI18N
      jButton5.setName("jButton5"); // NOI18N
      jButton5.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton5ActionPerformed(evt);
         }
      });

      jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
      jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
      jLabel2.setName("jLabel2"); // NOI18N

      org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
      jPanel3.setLayout(jPanel3Layout);
      jPanel3Layout.setHorizontalGroup(
         jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel3Layout.createSequentialGroup()
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jPanel3Layout.createSequentialGroup()
                  .add(18, 18, 18)
                  .add(jButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 151, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .add(18, 18, 18)
                  .add(jButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 134, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 30, Short.MAX_VALUE)
                  .add(jButton5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 134, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
               .add(jPanel3Layout.createSequentialGroup()
                  .addContainerGap()
                  .add(jLabel2)))
            .addContainerGap())
      );
      jPanel3Layout.setVerticalGroup(
         jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
            .add(34, 34, 34)
            .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 142, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(18, 18, 18)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jButton5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
      );

      jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Observaciones Generadas"));
      jPanel4.setName("jPanel4"); // NOI18N

      jScrollPane1.setName("jScrollPane1"); // NOI18N

      javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
      ListaObservaciones.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
      ListaObservaciones.setName("ListaObservaciones"); // NOI18N
      jScrollPane1.setViewportView(ListaObservaciones);

      jButton6.setText(resourceMap.getString("jButton6.text")); // NOI18N
      jButton6.setName("jButton6"); // NOI18N
      jButton6.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton6ActionPerformed(evt);
         }
      });

      jButton7.setText(resourceMap.getString("jButton7.text")); // NOI18N
      jButton7.setName("jButton7"); // NOI18N
      jButton7.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton7ActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
      jPanel4.setLayout(jPanel4Layout);
      jPanel4Layout.setHorizontalGroup(
         jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel4Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
               .add(jPanel4Layout.createSequentialGroup()
                  .add(10, 10, 10)
                  .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                     .add(jButton6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE))))
            .addContainerGap())
      );
      jPanel4Layout.setVerticalGroup(
         jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 157, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jButton6)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jButton7)
            .add(69, 69, 69))
      );

      org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 503, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jPanel4, 0, 262, Short.MAX_VALUE)
               .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
               .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 262, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
      );

      jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      jLabel3.setIcon(resourceMap.getIcon("jLabel3.icon")); // NOI18N
      jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
      jLabel3.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 4, 4, 4, new java.awt.Color(0, 0, 0)));
      jLabel3.setName("jLabel3"); // NOI18N

      org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
      mainPanel.setLayout(mainPanelLayout);
      mainPanelLayout.setHorizontalGroup(
         mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(mainPanelLayout.createSequentialGroup()
            .add(19, 19, 19)
            .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 1052, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(11, Short.MAX_VALUE))
      );
      mainPanelLayout.setVerticalGroup(
         mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(mainPanelLayout.createSequentialGroup()
            .add(4, 4, 4)
            .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 191, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(18, 18, 18)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      menuBar.setName("menuBar"); // NOI18N

      fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
      fileMenu.setName("fileMenu"); // NOI18N

      javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(simuldp.SimulDPApp.class).getContext().getActionMap(SimulDPView.class, this);
      exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
      exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
      exitMenuItem.setName("exitMenuItem"); // NOI18N
      fileMenu.add(exitMenuItem);

      menuBar.add(fileMenu);

      helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
      helpMenu.setName("helpMenu"); // NOI18N

      aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
      aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text")); // NOI18N
      aboutMenuItem.setName("aboutMenuItem"); // NOI18N
      helpMenu.add(aboutMenuItem);

      menuBar.add(helpMenu);

      statusPanel.setName("statusPanel"); // NOI18N

      statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

      statusMessageLabel.setName("statusMessageLabel"); // NOI18N

      statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
      statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

      progressBar.setName("progressBar"); // NOI18N

      org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
      statusPanel.setLayout(statusPanelLayout);
      statusPanelLayout.setHorizontalGroup(
         statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1091, Short.MAX_VALUE)
         .add(statusPanelLayout.createSequentialGroup()
            .addContainerGap()
            .add(statusMessageLabel)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 921, Short.MAX_VALUE)
            .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(statusAnimationLabel)
            .addContainerGap())
      );
      statusPanelLayout.setVerticalGroup(
         statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(statusPanelLayout.createSequentialGroup()
            .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(statusMessageLabel)
               .add(statusAnimationLabel)
               .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(3, 3, 3))
      );

      setComponent(mainPanel);
      setMenuBar(menuBar);
      setStatusBar(statusPanel);
   }// </editor-fold>//GEN-END:initComponents

private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
String Titulo="";
Titulo=control.AbrirArc((String)((this.ListaObservaciones.getSelectionPath()).getPathComponent(2)).toString());
control.GetObs().ObsMaxMin();
JFrame mainFrame = SimulDPApp.getApplication().getMainFrame();
System.out.print("\n"+control.GetObs().GetTipoDist());

if(control.GetObs().GetTipoDist()==1 || control.GetObs().GetTipoDist()==2)
   ResDialog = new ResDialog(mainFrame, false, control, Titulo, 1, this);

if(control.GetObs().GetTipoDist()==3)
   ResDialog = new ResDialog(mainFrame, false, control, Titulo, 3, this);

if(control.GetObs().GetTipoDist()==4 || control.GetObs().GetTipoDist()==5)
   ResDialog = new ResDialog(mainFrame, false, control, Titulo, 2, this);

ResDialog.setLocationRelativeTo(mainFrame);
SimulDPApp.getApplication().show(ResDialog);
}//GEN-LAST:event_jButton6ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
if (GeoDialog == null) {
   JFrame mainFrame = SimulDPApp.getApplication().getMainFrame();
   GeoDialog = new GeoDialog(mainFrame, false, control, this);
   GeoDialog.setLocationRelativeTo(mainFrame);            
}
SimulDPApp.getApplication().show(GeoDialog);
}//GEN-LAST:event_jButton1ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
if (PoiDialog == null) {
   JFrame mainFrame = SimulDPApp.getApplication().getMainFrame();
   PoiDialog = new PoiDialog(mainFrame, false, control, this);
   PoiDialog.setLocationRelativeTo(mainFrame);            
}
SimulDPApp.getApplication().show(PoiDialog);

}//GEN-LAST:event_jButton2ActionPerformed

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
if (UniDialog == null) {
   JFrame mainFrame = SimulDPApp.getApplication().getMainFrame();
   UniDialog = new UniDialog(mainFrame, false, control, this);
   UniDialog.setLocationRelativeTo(mainFrame);            
}
SimulDPApp.getApplication().show(UniDialog);

}//GEN-LAST:event_jButton3ActionPerformed

private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
if (NorDialog == null) {
   JFrame mainFrame = SimulDPApp.getApplication().getMainFrame();
   NorDialog = new NorDialog(mainFrame, false, control, this);
   NorDialog.setLocationRelativeTo(mainFrame);            
}
SimulDPApp.getApplication().show(NorDialog);

}//GEN-LAST:event_jButton5ActionPerformed

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
if (ExpDialog == null) {
   JFrame mainFrame = SimulDPApp.getApplication().getMainFrame();
   ExpDialog = new ExpDialog(mainFrame, false, control, this);
   ExpDialog.setLocationRelativeTo(mainFrame);            
}
SimulDPApp.getApplication().show(ExpDialog);

}//GEN-LAST:event_jButton4ActionPerformed

private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
   control.Borrador(((this.ListaObservaciones.getSelectionPath()).getPathComponent(2)).toString());
   this.CrearArbol();
}//GEN-LAST:event_jButton7ActionPerformed

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JTree ListaObservaciones;
   private javax.swing.JButton jButton1;
   private javax.swing.JButton jButton2;
   private javax.swing.JButton jButton3;
   private javax.swing.JButton jButton4;
   private javax.swing.JButton jButton5;
   private javax.swing.JButton jButton6;
   private javax.swing.JButton jButton7;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JPanel jPanel3;
   private javax.swing.JPanel jPanel4;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JPanel mainPanel;
   private javax.swing.JMenuBar menuBar;
   private javax.swing.JProgressBar progressBar;
   private javax.swing.JLabel statusAnimationLabel;
   private javax.swing.JLabel statusMessageLabel;
   private javax.swing.JPanel statusPanel;
   // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
    private JDialog GeoDialog;
    private JDialog PoiDialog;
    private JDialog UniDialog;
    private JDialog NorDialog;
    private JDialog ExpDialog;
    private ControlSim control;
    private ResDialog ResDialog;
}
