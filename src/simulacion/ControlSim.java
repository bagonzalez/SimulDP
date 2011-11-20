package simulacion;
import java.util.Vector;
import java.io.*;
import javax.swing.JOptionPane;

/***********************************************************************/
/* Clase Controladora del flujo de la aplicación la cual tiene las     */
/* responsabilidad de manejar un vector de Objetos tipo GenObsAlet el  */
/* cual permite generar las observaciones aleatorias.                  */
/***********************************************************************/

public class ControlSim {

  int ContGeo=1;//Contador Generador de correlativo Geo
  int ContPoi=1;//Contador Generador de correlativo Poi
  int ContUni=1;//Contador Generador de correlativo Uni
  int ContExp=1;//Contador Generador de correlativo Exp
  int ContNor=1;//Contador Generador de correlativo Nor


  /**************************************************************************/
  /* Constructor de la clase ControlSim, crea el neevo vector que contendra */
  /* la colecciones de objetos Gen. de Obs. Aleatorias, adémas              */
  /* carga los contadores de las secuencias de archivos.                    */
  /**************************************************************************/
  
  public ControlSim() {
     this.GenObsAlea=new Vector();
     this.CargarCont();
  }
  
   /**
   * Colección de Objetos
   * Generador de Observaciones Aleatorias
   */
  public Vector  GenObsAlea;

  /***********************************************************************/
  /* Metodo que permite asignarle la responsabilidad de guardar las      */
  /* observaciones aleatorias al Generador de Observaciones aleatorias.  */  
  /* Funcionamiento: Por medio del codigo de distribucion asigna la      */
  /* responsabilidad de guardado, y aumenta el contador de archivos.     */
  /***********************************************************************/

  public boolean GuardarObs() {
  switch(this.GetObs().GetTipoDist()){
     case 1:
        this.GetObs().Guardar(ContGeo);
        ContGeo++;
        break;
     case 2:
        this.GetObs().Guardar(ContPoi);
        ContPoi++;
        break;
     case 3:
        this.GetObs().Guardar(ContUni);
        ContUni++;
        break;
     case 4:
        this.GetObs().Guardar(ContExp);
        ContExp++;
        break;
      case 5:
        this.GetObs().Guardar(ContNor);
        ContNor++;
        break;
  }
  this.GuardarCont();
  return true;
  }

  /***************************************************************************/
  /*Metodo que permite asignar la responsabilidad de cargar el Vector Datos  */
  /*al objeto generador de observaciones aleatorias.                         */
  /***************************************************************************/

  public void CargarData(int TipoDist, int Obs, String Titulo, Object[] Datos){
     CrearObs(TipoDist, Obs);
     GetObs().CargarD(Datos);         
  }

  /***************************************************************************/
  /*Metodo que permite crear un nuevo objeto tipo GenObsAleat y lo almacena  */
  /*en la colección GenObsAlea                                               */
  /***************************************************************************/

  public void CrearObs(int TipoDist, int NumObs) {
     this.GenObsAlea.addElement(new GenObsAlet(TipoDist, NumObs));         
  }

  /***************************************************************************/
  /*Metodo que permite obtener el último objeto GenObsAlet de la collecion   */
  /*GenObsAlea                                                               */
  /***************************************************************************/
  public GenObsAlet GetObs(){
     return ((GenObsAlet)this.GenObsAlea.lastElement());     
  }

  /***************************************************************************/
  /* Carga los distintos contadores almacenados en un archivo de texto, con el*/
  /* proposito de generar la siguiente estensión de archivo de observaciones. */
  /***************************************************************************/
  public void CargarCont() {
  File archivo=new File("C:\\SimulDP\\Otros\\Contador.txt");
  try{
     BufferedReader in=new BufferedReader(new FileReader(archivo));
     String data="";
     data=in.readLine();
     this.ContGeo = Integer.parseInt(data);//Pasa String a Entero
     data=in.readLine();
     this.ContPoi = Integer.parseInt(data);
     data=in.readLine();
     this.ContUni = Integer.parseInt(data);
     data=in.readLine();
     this.ContExp = Integer.parseInt(data);
     data=in.readLine();
     this.ContNor = Integer.parseInt(data);
  }
  catch(IOException e){
      System.out.println("no se encontro!");
  }
  }

  /**************************************************************************/
  /* Metodo que almacena los contadores de las secuencia de extensiones     */
  /* sugueridas por la catedra para la creacion de archivos de              */
  /* observaciones.                                                         */
  /**************************************************************************/

  public void GuardarCont(){
  try {// Guarda archivo txt con su contador "i" aumentado en 1
      String Archivoo="C:\\SimulDP\\Otros\\Contador.txt";
      String AUXi;
      FileWriter Guardxx=new FileWriter(Archivoo);
      AUXi= Integer.toString(this.ContGeo);//Pasa Entero a String
      Guardxx.write(AUXi+"\n");
      AUXi= Integer.toString(this.ContPoi);//Pasa Entero a String
      Guardxx.write(AUXi+"\n");
      AUXi= Integer.toString(this.ContUni);//Pasa Entero a String
      Guardxx.write(AUXi+"\n");
      AUXi= Integer.toString(this.ContExp);//Pasa Entero a String
      Guardxx.write(AUXi+"\n");
      AUXi= Integer.toString(this.ContNor);//Pasa Entero a String
      Guardxx.write(AUXi+"\n");
      Guardxx.close();
  }
  catch(IOException e){
     System.out.println("no se encontro!");
  }
  }

  /*************************************************************************/
  /* Abre un archivo de texto que contiene las observaciones aleatorias    */
  /* generadas por cualquier distribucion de probabilidad.                 */
  /* Recibimos el nombre del archivo NomArc y retornamos el titulo de la   */
  /* distribucion correspondiente.                                         */
  /*************************************************************************/

  public String AbrirArc(String NomArc){
  String Text="", Titulo="";
  String[] ArreDirec={ "C:\\SimulDP\\ObsData\\Geo\\","C:\\SimulDP\\ObsData\\Poi\\",
                       "C:\\SimulDP\\ObsData\\Uni\\", "C:\\SimulDP\\ObsData\\Exp\\",
                       "C:\\SimulDP\\ObsData\\Nor\\", "C:\\SimulDP\\Resultados\\" };
  int TipoD=0, ContLine=0;
  Object[] DatosObs; /*Aqui vamos a guardar los datos numericos de las Observaciones */
  File ArcAbrir=new File("");
  try
  {
     for(int i=0; i<ArreDirec.length ; i++){ /* Buscamos el archivo en los directorios*/
         ArcAbrir=new File(ArreDirec[i]+NomArc); /*Generamos la direccion absoluta */

         if(ArcAbrir.canRead()){
            FileReader Fichero=new FileReader(ArcAbrir);
            BufferedReader leer=new BufferedReader(Fichero);
            while((Text=leer.readLine())!=null){

                  if(ContLine==1) /*Capturamos el titulo en la primera linea de ArcAbrir*/
                     Titulo=Text;

                  if(ContLine==3) /*¿ Tipo de distribución ? en la linea 3*/
                     TipoD=Integer.parseInt(Text);

                  ContLine++; /* Conteo de lineas */
            }
            leer.close();            
         }
         /*De acuerdo al número de lineas instanciamos DatosObs */
         DatosObs=new Object[ContLine];
         ContLine=0;
         String  Val="";
         if(ArcAbrir.canRead()){
            FileReader Fichero=new FileReader(ArcAbrir);
            BufferedReader leer=new BufferedReader(Fichero);
            while((Text=leer.readLine())!=null){
               /* Los datos numericos comienzan en la linea 6 */
               if(ContLine>5){                                  
                   Val=Text.substring(Text.indexOf('\t')+1);                                                    
                   DatosObs[ContLine]=new Double(Val);                 
               }                
               ContLine++;               
            }
            /*Cargamos los datos haciendo referencia al metodo de esta clase*/
            this.CargarData(TipoD, DatosObs.length, Titulo, DatosObs);
         }         
     }
  }
  catch(IOException ioe)
  {
     System.out.println(ioe);
  }
  return Titulo;
  }

  /*************************************************************************/
  /* Este metodo elimina el archivo seleccionado que recibe como parametro */
  /*************************************************************************/
  
  public void Borrador (String NomArc)
  {
  String Text="", Titulo="";
  String[] ArreDirec={ "C:\\SimulDP\\ObsData\\Geo\\","C:\\SimulDP\\ObsData\\Poi\\",
                       "C:\\SimulDP\\ObsData\\Uni\\", "C:\\SimulDP\\ObsData\\Exp\\",
                       "C:\\SimulDP\\ObsData\\Nor\\", "C:\\SimulDP\\Resultados\\" };


  File ArcElim=new File("");  
  for(int i=0; i<ArreDirec.length ; i++){
     ArcElim=new File(ArreDirec[i]+NomArc);
     if(ArcElim.canRead()){
         if(JOptionPane.showConfirmDialog(null,"¿Desea Eliminar el archivo?", "Atencion Eliminar",  JOptionPane.INFORMATION_MESSAGE)==0){
             if (ArcElim.delete())
                JOptionPane.showMessageDialog(null,"El fichero " + ArreDirec[i]+NomArc + " ha sido borrado correctamente");
         }
      }
   }  
}
  
}