package simulacion;
import java.util.Vector;
import java.util.Date;
import java.util.Random;
import org.opensourcephysics.frames.PlotFrame;
import org.opensourcephysics.display.*;
import java.awt.Color;
import java.util.Enumeration;
import java.io.*;

/*****************************************************************************/
/*Clase GenObsAleat responsable de la generación de Observaciones Aleatorias */
/*****************************************************************************/
 public class GenObsAlet {

  private String Evento;
  private Date FechaCrea;
  private int CantObs;
  private int TipoDistr;  
  private double XMax;  
  private double XMin;   
  private int TotalX;
  
    /**
   * 
   * Colección de Observaciones aleatorias para una distribucion especifica;
   */
  public Vector  ObsDistrib;   


  /**************************************************************************************/
  /*Constructor de la clase GenObsAlet recibe un codigo de distribucion TipoDist        */
  /*Que equivale a 1: Geometrica 2: Poisson 3. Uniforme 4. Exponencial 5.Normal         */
  /*Crea ademas un vector de observaciones. CantObs cantidad de observaciones a generar */
  /**************************************************************************************/
  public GenObsAlet(int TipoDistr, int CantObs) 
  {
  this.TipoDistr=TipoDistr;
  this.CantObs=CantObs;      
  this.ObsDistrib=new Vector();
  }

  /*************************************************************************************/
  /*Generamos una cantidad de observaciones, y se almacenan en la colección de objetos */
  /* ObsDisTrib, estas observaciones son discretas.                                    */
  /*************************************************************************************/
  public void GenObsDisc(double ProbFracaso, double Media)
  {  
  for(int i=0; i<this.CantObs; i++){
     switch(this.TipoDistr){
        case 1:
           this.ObsDistrib.addElement(new Geo(ProbFracaso));           
           break;
        case 2:
           this.ObsDistrib.addElement(new Poi(Media));
           break;       
     }
  }  
  }

  /************************************************************************/
  /* Retornamos el tipo de distribucion                                   */
  /************************************************************************/

  public int GetTipoDist(){
      return this.TipoDistr;
  }

  /******************************************************************************************/
  /*Generamos una cantidad de observaciones, y se almacenan en la colección de objetos      */
  /* ObsDisTrib, estas observaciones son continuas.                                         */
  /*Asigna los parametros especificos a cada constructor de una distribucion de probabilidad*/
  /******************************************************************************************/
  public void GenObsCont(double LimInf, double LimSup, double Media, double DStd)
  {
     for(int i=0; i<this.CantObs; i++){
     switch(this.TipoDistr){
        case 3:
           this.ObsDistrib.addElement(new Uni(LimInf, LimSup));
           break;
        case 4:
           this.ObsDistrib.addElement(new Exp(Media));
           break;       
        case 5:
           this.ObsDistrib.addElement(new Nor(Media, DStd));
           break;
     }
  }     
  }
  
  /**************************************************************************/
  /*Retorna un arreglo bidimensional de objetos con las observaciones hechas*/
  /**************************************************************************/

  public Object[][] GetObser(){ 
  DistProb AuxElem;
  Object[][] TablaObs=new Object[this.ObsDistrib.size()][2];
  int i=0;
  /*Recorremos la coleccion */
  for (Enumeration e = this.ObsDistrib.elements() ; e.hasMoreElements(); ) { 
     if(e.hasMoreElements()){         
        AuxElem=(DistProb)e.nextElement();
        TablaObs[i][0]=new Double(i+1);
        TablaObs[i][1]=new Double(AuxElem.GetObserv()); 
        i++;
     }            
  }
  return TablaObs; 
  }

  /***************************************************************************/
  /*Carga de datos. Metodo con la responsabilidad de cargar en memoria un    */
  /* arreglo Data de objetos con observaciones.                              */
  /***************************************************************************/

  public void CargarD(Object[] Data)
  {
  double Dato;
  for(int i=0; i<Data.length; i++){
     if(Data[i]!=null){
        Dato=((Double)Data[i]).doubleValue(); /*Se extrae el dato*/
        switch(this.TipoDistr){
           case 1:  /* Se sigue con la codificación del tipo de Distribucion de Prob*/          
              this.ObsDistrib.addElement(new  Geo( Dato , Dato )); /*Se inserta en la coleccion */
           break;
           case 2:            
              this.ObsDistrib.addElement(new  Poi(Dato, Dato));           
           break;
           case 3:             
              this.ObsDistrib.addElement(new  Uni( Dato ));
           break;
           case 4:            
              this.ObsDistrib.addElement(new Exp(Dato, Dato));
           break;
           case 5:             
              this.ObsDistrib.addElement(new  Nor(Dato));
           break;
         }
     }
  }
  }

  /**************************************************************************/
  /*Metodo responsable de crear la tabla de frecuencia, recibe un parametro */
  /* para conocer si se realizara */
  /**************************************************************************/
  public Object[][] CrearTFrec(int DistTipo){
  double Longitud=this.XMax-this.XMin;  
  double TamInter=Longitud/9;
  
  Object[][] TablaFre=new Object[16][3];
  int Suma, i=0;
  DistProb AuxElem;
  Suma=0;

  for(double x=this.XMin, dx=TamInter; x<=this.XMax; x=x+dx){
     for (Enumeration e = this.ObsDistrib.elements() ; e.hasMoreElements(); ){
        if(e.hasMoreElements()){      
           AuxElem=(DistProb)e.nextElement(); 
           if(AuxElem.GetObserv()>=x && AuxElem.GetObserv()< (x+dx))
                  Suma=Suma+1;  
        }                        
      }                 
      if(i<13){        
        TablaFre[i][0]=new Double(x);
        TablaFre[i][1]=new Double(x+dx);
        TablaFre[i][2]=new Double(Suma);
      }        
     Suma=0;
     i++;                  
  }
  return TablaFre;  
  }
  /*************************************************************************/
  /* Obtiene la menor y mayor observacion generada                         */
  /*************************************************************************/

  public void ObsMaxMin(){
  DistProb AuxElem;
  double mayor, menor, numAux;
  mayor=menor=((DistProb)this.ObsDistrib.firstElement()).GetObserv();
  /*Recoremos la coleccion de elemntos elementos*/
  for (Enumeration e = this.ObsDistrib.elements() ; e.hasMoreElements(); ) {     
     if(e.hasMoreElements())
     {      
        AuxElem=(DistProb)e.nextElement();              
        numAux=AuxElem.GetObserv();        
        if(numAux>mayor){
           mayor=numAux;
        }
        else{
           if(numAux<menor)
              menor=numAux;
        }             
    }
  }  
  this.XMax=mayor;
  this.XMin=menor; 
  }

  /***************************************************************************/
  /*Este metodo obtiene un arreglo bidimensional que contiene la tabla de    */
  /*distribución a partir de la distribucion Axumulada de frecuencias        */
  /**************************************************************************/
  public Object[][]  GetPxDis(int TipoDist) {
  Object[][] TablaFre, TablaPro;
  TablaFre=GetFxDis(TipoDist);
  TablaPro=new Object[TablaFre.length][2];
  double XObs, Prob=0.000, NumeObs=0.0;
  for(int i=0; i<TablaFre.length; i++)
  {
     if(TablaFre[i][0]!=null){
        XObs=((Double)TablaFre[i][0]).doubleValue();
        if(i!=0)
           NumeObs=((Double)TablaFre[i][1]).doubleValue()-((Double)TablaFre[i-1][1]).doubleValue();
        else
           NumeObs=((Double)TablaFre[i][1]).doubleValue();
        
        Prob=NumeObs/TotalX;
        TablaPro[i][0]=new Double(XObs);
        TablaPro[i][1]=new Double(Prob);
     }
  }
  return TablaPro;
  }

  /******************************************************************************/
  /*Este metodo obtiene un arreglo bidimensional que contiene la tabla de       */
  /*frecuencia acumulada a partir de la de tabla de distribucion de frecuencias */
  /******************************************************************************/

  public Object[][]  GetFxDis(int TipoDist) {
  Object[][] TablaFre, TablaFrecA;
  TablaFre=CrearTFrec(TipoDist);
  TablaFrecA=new Object[TablaFre.length][2];
  double LimS, LimI, Suma=0.000;
  int IndiceNuevo=0;
  for(int i=0; i<TablaFre.length; i++)
  {
     if(TablaFre[i][0]!=null){
        LimI=((Double)TablaFre[i][0]).doubleValue();
        LimS=((Double)TablaFre[i][1]).doubleValue();
        Suma=(((Double)TablaFre[i][2]).doubleValue())+Suma;
        /*Obtenemos el Yi para la tabla */
        if(TipoDist==1)
           TablaFrecA[IndiceNuevo][0]=new Double(Math.round((LimI+LimS)/2));
        else
           TablaFrecA[IndiceNuevo][0]=new Double((LimI+LimS)/2.0);
        /*Sumamos los valores y devolvemos su acumulado */
        if(i!=0){
            if(Suma!=((Double)TablaFrecA[(IndiceNuevo-1)][1]).doubleValue()){
               TablaFrecA[IndiceNuevo][1]=new Double(Suma);
               IndiceNuevo++;
            }
        }
        else{
           TablaFrecA[IndiceNuevo][1]=new Double(Suma);
           IndiceNuevo++;
        }
        this.TotalX=new Double(Suma).intValue();
     }     
  }
  return TablaFrecA;
  }

  /************************************************************************/
  /*Grafica la distribucion de probabilidad a partir tabla de probabilidad*/
  /************************************************************************/
  
  public void GrafDist(String Titulo, int TipoDist) {
        Histogram histograma= new Histogram(); 
        PlotFrame frame = new PlotFrame("Observación", "Probabilidad", Titulo ); 
        frame.setSize(400, 400);                   
        Object[][] TablaPro;
        TablaPro=GetPxDis(TipoDist); /*Obtenemos la tabla de Probabilidad*/

        double XObs;
        double Prob;
        for(int i=0; i<TablaPro.length; i++)
        {   
            if(TablaPro[i][0]!=null){
               XObs=((Double)TablaPro[i][0]).doubleValue();
               Prob=((Double)TablaPro[i][1]).doubleValue();
               /*Graficamos ya sea un histograma o un valor de puntos continuos*/
               if(TipoDist==1)
                  histograma.append( XObs , Prob);
               else {
                   frame.append(0, XObs, Prob);                   
                   frame.setConnected(true);
                   if(TipoDist==3)
                     frame.setPreferredMinMax(this.XMin, this.XMax, 0, 1);
               }
            }
        }      
        histograma.setDiscrete(false);
        
        histograma.setBinColor(Color.GREEN, Color.RED);  
        histograma.setBinWidth(1);
        frame.setRowNumberVisible(true);
        frame.addDrawable(histograma);           
        frame.setVisible(true);         
  }

   /************************************************************************/
  /*Grafica la distribucion de probabilidad acumulada                     */
  /************************************************************************/

  public void GrafAcum(String Titulo, int TipoDist) {
       
        PlotFrame frame = new PlotFrame("Observación", "Probabilidad", Titulo );
        frame.setSize(400, 400);
        Object[][] TablaPro;       
        TablaPro=GetFxDis(TipoDist);   /*Obtenemos la tabla Acumulada */
        double XObs;
        double Prob;
        for(int i=0; i<TablaPro.length; i++)
        {
            if(TablaPro[i][0]!=null){
               XObs=((Double)TablaPro[i][0]).doubleValue();
               Prob=((Double)TablaPro[i][1]).doubleValue()/this.TotalX;
               frame.append(0, XObs , Prob);
               frame.setConnected(true);
            }
        }       
        frame.setRowNumberVisible(true);       
        frame.setVisible(true);
  }

  /************************************************************************/
  /*Almacena los resultados obtenidos de las observaciones en un archivo  */
  /*de texto. Se le proporciona el correlativo de la extencion a generar. */
  /************************************************************************/

  public boolean Guardar(int Correlativo){
      String Ruta="", Titulo="";
      this.FechaCrea=new Date();
      /*Creamos un titulo y almacenamos los parametros de estos*/
      switch(this.TipoDistr){
          case 1:
              Titulo="Resultados Distribución Geometrica -- "+
                     "Prob Frac. "+ ((Geo)this.ObsDistrib.firstElement()).ProbFrac;
              Ruta="Geo\\Geo";
              break;
          case 2:
              Titulo="Resultados Distribución Poisson  -- <<Media>>" +
                      ((Poi)this.ObsDistrib.firstElement()).Media;
              Ruta="Poi\\Poi";
              break;
          case 3:
              Titulo="Resultados Distribución Uniforme -- <<Lim. Inferior>>  "+
                       ((Uni)this.ObsDistrib.firstElement()).LimInf +
                       " <<Lim. Superior>> "+  ((Uni)this.ObsDistrib.firstElement()).LimSup;
              Ruta="Uni\\Uni";
              break;
          case 4:
              Titulo="Resultados Distribución Exponencial -- <<Media>>" +
                      ((Exp)this.ObsDistrib.firstElement()).Media;
              Ruta="Exp\\Exp";
              break;
          case 5:
              Titulo="Resultados Distribución Normal -- <<Media>> " +
                       ((Nor)this.ObsDistrib.firstElement()).Media +
                       " <<Desv. Estandar>>"+((Nor)this.ObsDistrib.firstElement()).DStd;
                      ;
              Ruta="Nor\\Nor";
              break;
      }
      
      if(Correlativo<=999){
          if(Correlativo<10){
              Ruta=Ruta+".00";
          }
          if(Correlativo>=10 & Correlativo<100){
              Ruta=Ruta+".0";
          }
      }

      if(Correlativo<=999){//IF DE VALIACION(SOLO SE PUEDEN GENERAR 999 ARCHIVOS)
          try//Asigna numero de correlativo y extension
          {//inicio try

                 String Guardar ="C:\\SimulDP\\ObsData\\"+Ruta+Correlativo;
                 FileWriter  Guardx=new FileWriter(Guardar);
                 String Cadena=new String();
                 Object[][] TablaObs;
                 TablaObs=GetObser();
                 double XObs, Prob;
                 /*Almacenamos un encabezado*/
                 Guardx.write("/**************************************************/\n");
                 Guardx.write(""+Titulo+"\n"+this.FechaCrea+"\n"+this.TipoDistr);
                 Guardx.write("\nArchivo: "+Correlativo);
                 Guardx.write("\n/************************************************/");

                 /*Guardamos los datos numericos */
                 for(int i=0; i<TablaObs.length; i++)
                 {
                    if(TablaObs[i][0]!=null){
                       XObs=((Double)TablaObs[i][0]).doubleValue();
                       Prob=((Double)TablaObs[i][1]).doubleValue();
                       Cadena="\n"+XObs+"\t"+Prob;
                       Guardx.write(Cadena);
                    }
                }
                Guardx.close();            
      }
      catch(IOException ioe)
      {
            System.out.println(ioe);
      }     
  }
      return true;
  } 

}