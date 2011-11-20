package simulacion;
public class Nor extends DistProb {

  public double DStd; /*Desviación estandar*/

  /**************************************************************************/
  /*Clase que genera una observación aleatoria de la distribucion Normal  */
  /**************************************************************************/

  public Nor(double Media, double DStd) 
  {
  double Suma=0, NumR;
  for(int i=1; i<=12; i++){
     NumR=Aleatorio();
     Suma=Suma+NumR;
  }
  this.ProbExito=Suma;
  this.DStd=DStd;
  this.Media=Media;
  this.Observacion=DStd*(Suma-6)+Media;     
  }

  /***************************************************************************************/
  /*Contructor para generar las observaciones a partir de datos cargados desde un archivo*/
  /*es decir, estas observaciones ya existen y solo se crean los objetos de nuevo.       */
  /**************************************************************************************/
  public Nor(double Obs){
     this.Observacion=Obs;
  }
}