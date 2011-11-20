package simulacion;
import java.lang.*;

public class Geo extends DistProb {

  public double ProbFrac;
  /**************************************************************************/
  /*Clase que genera una observación aleatoria de la distribucion Geometrica */
  /**************************************************************************/
  public Geo(double q) 
  {     
  this.ProbFrac=q;
  double NumAleaR=Aleatorio();
  if(NumAleaR==0.0000)
     NumAleaR=0.0001;
  while(NumAleaR>q) /* Si no se hace esto generaria observaciones para x=0*/
     NumAleaR=Aleatorio();  
  this.Observacion=new Double(Math.log(NumAleaR)/Math.log(q)).intValue();
  }

  /***************************************************************************************/
  /*Contructor para generar las observaciones a partir de datos cargados desde un archivo*/
  /*es decir, estas observaciones ya existen y solo se crean los objetos de nuevo.       */
  /**************************************************************************************/
   public Geo(double Num, double Obs)
   {
      this.Observacion=Obs;
      this.NumObs=Num;
   }
}