/*
 * NorDialog.java
 *
 * Created on 25 de abril de 2009, 15:12
 */

package simuldp;
import org.jdesktop.application.Action;
import simulacion.ControlSim;
import javax.swing.*;

/**
 *
 * @author  bruno
 */
public class NorDialog extends javax.swing.JDialog {

    /** Creates new form NorDialog */
    public NorDialog(java.awt.Frame parent, boolean modal, ControlSim control, SimulDPView aThis) {
        super(parent, modal);
        this.control=control;
        this.SimulDPView=aThis;
        initComponents();
    }

    
    @Action public void CierreDia() {
        setVisible(false);        
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jPanel1 = new javax.swing.JPanel();
      jLabel1 = new javax.swing.JLabel();
      jScrollPane1 = new javax.swing.JScrollPane();
      jTextPane1 = new javax.swing.JTextPane();
      jPanel3 = new javax.swing.JPanel();
      jLabel2 = new javax.swing.JLabel();
      jTextField1 = new javax.swing.JTextField();
      jLabel3 = new javax.swing.JLabel();
      jTextField2 = new javax.swing.JTextField();
      jLabel4 = new javax.swing.JLabel();
      jTextField3 = new javax.swing.JTextField();
      jButton1 = new javax.swing.JButton();
      jButton2 = new javax.swing.JButton();
      jLabel5 = new javax.swing.JLabel();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setName("Form"); // NOI18N
      setResizable(false);

      jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
      jPanel1.setName("jPanel1"); // NOI18N

      org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(simuldp.SimulDPApp.class).getContext().getResourceMap(NorDialog.class);
      jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
      jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
      jLabel1.setName("jLabel1"); // NOI18N

      jScrollPane1.setName("jScrollPane1"); // NOI18N

      jTextPane1.setEditable(false);
      jTextPane1.setText(resourceMap.getString("jTextPane1.text")); // NOI18N
      jTextPane1.setName("jTextPane1"); // NOI18N
      jScrollPane1.setViewportView(jTextPane1);

      jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
      jPanel3.setName("jPanel3"); // NOI18N

      jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
      jLabel2.setName("jLabel2"); // NOI18N

      jTextField1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
      jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
      jTextField1.setName("jTextField1"); // NOI18N

      jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
      jLabel3.setName("jLabel3"); // NOI18N

      jTextField2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
      jTextField2.setText(resourceMap.getString("jTextField2.text")); // NOI18N
      jTextField2.setName("jTextField2"); // NOI18N

      jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
      jLabel4.setName("jLabel4"); // NOI18N

      jTextField3.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
      jTextField3.setText(resourceMap.getString("jTextField3.text")); // NOI18N
      jTextField3.setName("jTextField3"); // NOI18N

      org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
      jPanel3.setLayout(jPanel3Layout);
      jPanel3Layout.setHorizontalGroup(
         jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jLabel3)
               .add(jLabel2)
               .add(jLabel4))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 40, Short.MAX_VALUE)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
               .add(jTextField3, 0, 0, Short.MAX_VALUE)
               .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextField1)
               .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextField2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE))
            .addContainerGap())
      );
      jPanel3Layout.setVerticalGroup(
         jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel2)
               .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(14, 14, 14)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel3)
               .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(18, 18, 18)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel4)
               .add(jTextField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

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

      jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
      jLabel5.setIcon(resourceMap.getIcon("jLabel5.icon")); // NOI18N
      jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
      jLabel5.setName("jLabel5"); // NOI18N

      org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
               .add(jPanel1Layout.createSequentialGroup()
                  .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(jPanel1Layout.createSequentialGroup()
                        .add(51, 51, 51)
                        .add(jButton1)
                        .add(40, 40, 40)
                        .add(jButton2)))
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(jLabel5)
                  .add(13, 13, 13))
               .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 625, Short.MAX_VALUE)
               .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1))
            .addContainerGap())
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .add(jLabel1)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
               .add(jPanel1Layout.createSequentialGroup()
                  .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .add(18, 18, 18)
                  .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jButton2)
                     .add(jButton1))
                  .add(21, 21, 21))
               .add(jPanel1Layout.createSequentialGroup()
                  .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 192, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .addContainerGap())))
      );

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .addContainerGap()
            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 298, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
Validaciones DatoValido= new Validaciones();
String Error;
if((!DatoValido.EsEntero(jTextField3.getText()))){
Error="Numero de obsevaciones es Incorrecto";
   JOptionPane.showMessageDialog(null,Error, "ERROR",JOptionPane.ERROR_MESSAGE);}
else{
   if(!DatoValido.ValIntFloat(jTextField1.getText()))
   {
      Error="El valor de la media es incorrecto";
      JOptionPane.showMessageDialog(null,Error, "ERROR",JOptionPane.ERROR_MESSAGE);
   }
   else{
      if(!DatoValido.ValIntFloat(jTextField2.getText()))
      {
      Error="La Desviacón Estandar es Incorrecto";
      JOptionPane.showMessageDialog(null,Error, "ERROR",JOptionPane.ERROR_MESSAGE);}
      else{
      Double Media=new Double(this.jTextField1.getText());
      Double DStd=new Double(this.jTextField2.getText());
      Double NumOb=new Double(this.jTextField3.getText());
      this.control.CrearObs(5, NumOb.intValue());
      control.GetObs().GenObsCont(0, 0, Media.doubleValue(), DStd.doubleValue());
      control.GetObs().ObsMaxMin();
      JFrame mainFrame = SimulDPApp.getApplication().getMainFrame();
      ResDialog = new ResDialog(mainFrame, false, control, "Observaciones Aleatorias - Distribución Normal", 2, this.SimulDPView);
      ResDialog.setLocationRelativeTo(mainFrame);
      SimulDPApp.getApplication().show(ResDialog);
   }}}
}//GEN-LAST:event_jButton1ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
this.CierreDia();
}//GEN-LAST:event_jButton2ActionPerformed
    

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton jButton1;
   private javax.swing.JButton jButton2;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel5;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel3;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JTextField jTextField1;
   private javax.swing.JTextField jTextField2;
   private javax.swing.JTextField jTextField3;
   private javax.swing.JTextPane jTextPane1;
   // End of variables declaration//GEN-END:variables
   private ControlSim control; 
   private ResDialog ResDialog;
   private SimulDPView SimulDPView;
}
