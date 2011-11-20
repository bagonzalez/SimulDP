package simulacion;
import java.util.Random;

/******************************************************************************/
/*Clase abstracta que sirve como molde para generar las demas distribuciones de*/
/**probabilidad                                                                */
/******************************************************************************/

public abstract class DistProb{

  public double Observacion;
  public double NumObs;
  public double Media;  
  public double ProbExito;

  /************************************/
  /*Obtenemos la observacion generada */
  /************************************/
  public double GetObserv(){
        return this.Observacion;
  }

  /**************************************/
  /*Retornamos al probabilidad de exito */
  /**************************************/
  public double ProbExito(){
        return this.ProbExito;
  }

  /**********************************************************************/
  /*Creamos un número aleatorio por medio de las clases Random de java. */
  /* y lo retornamos                                                    */
  /* ********************************************************************/
  public double Aleatorio(){
      Random AleaT=new Random();
      return AleaT.nextDouble();
  }
  
}