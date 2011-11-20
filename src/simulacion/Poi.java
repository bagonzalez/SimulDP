package simulacion;

  /**************************************************************************/
  /*Clase que genera una observación aleatoria de la distribucion Poisson   */
  /**************************************************************************/

public class Poi extends DistProb{

  public Poi(double Media) 
  {
  this.Media=Media;
  this.Observacion=0;
  
  double k, Prod;
  k=Math.exp(-Media);
  Prod=Aleatorio();
  while(Prod>=k){
     this.Observacion=this.Observacion+1;
     Prod=Prod*Aleatorio();     
  }
  this.ProbExito=Prod;  
  }

  /***************************************************************************************/
  /*Contructor para generar las observaciones a partir de datos cargados desde un archivo*/
  /*es decir, estas observaciones ya existen y solo se crean los objetos de nuevo.       */
  /**************************************************************************************/
  public Poi(double Num, double Observ){
     this.Observacion=Observ;
     this.NumObs=Num;
  }
 
}