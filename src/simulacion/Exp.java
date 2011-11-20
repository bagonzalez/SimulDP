package simulacion;
public class Exp extends DistProb{

  public Exp(double Media) {
  this.Media=Media;
  double AleatR=Aleatorio();
  this.Observacion=(-1)*(this.Media*Math.log(AleatR));
  this.ProbExito=AleatR;  
  }

  /***************************************************************************************/
  /*Contructor para generar las observaciones a partir de datos cargados desde un archivo*/
  /*es decir, estas observaciones ya existen y solo se crean los objetos de nuevo.       */
  /**************************************************************************************/

  public Exp(double Num, double Observ){
     this.NumObs=Num;
     this.Observacion=Observ;
  }
}