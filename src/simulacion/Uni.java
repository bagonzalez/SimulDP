package simulacion;

public class Uni extends DistProb{

  public double LimInf;
  public double LimSup;

  /**************************************************************************/
  /*Clase que genera una observación aleatoria de la distribucion Uniforme  */
  /**************************************************************************/

  public Uni(double a, double b) 
  {
  this.LimInf=a;
  this.LimSup=b;
  double NumAleaR;
  boolean bandera=false;
  this.Observacion=a-1;/*Hacemos que entre el while*/
  /*Comprobamos que la observacion este en el rango */
  while(!(this.Observacion > a && this.Observacion < b )) {
      NumAleaR=Aleatorio();
      this.Observacion=a+(b-a)*NumAleaR;      
  }
  }
  /***************************************************************************************/
  /*Contructor para generar las observaciones a partir de datos cargados desde un archivo*/
  /*es decir, estas observaciones ya existen y solo se crean los objetos de nuevo.       */
  /**************************************************************************************/
  public Uni(double Obs){
     this.Observacion=Obs;
  }

}