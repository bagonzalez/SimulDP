package org.opensourcephysics.display2d;

import org.opensourcephysics.numerics.Function;

public class ZExpansion implements Function{
   double expansion=1;
   double k=1;
   double min=-1;
   double max=1;
   double center=0;
   double a1, a2=1;

   public ZExpansion( double expansion){
      this.expansion=Math.abs(expansion);
   }

   public void setExpansion(double expansion){
      this.expansion=Math.abs(expansion);
      setMinMax( min,  max);
   }

   public void setMinMax(double min, double max){
      this.min=min;
      this.max=max;
      if(min==max){
        k=0;
        a1=a2=center=min;
      }else if(min<=0 && max >=0){
        center=0;
        k= expansion/Math.max(-min,max);
        a1=max/(1-Math.exp(-k*max));
        a2=-min/(1-Math.exp(k*min));
      } else if(min>0){  // min and max positive
         center=min;
         k= expansion/(max-min);
         a1=a2=max/(1-Math.exp(-k*(max-min)));
      } else{            // min and max negative
         center=max;
         k= expansion/(max-min);
         a1=a2=-min/(1-Math.exp(k*(max-min)));
      }

   }

   public double evaluate(double z){
      z=z-center;  // shift
      if(z>=0) return a1*(1-Math.exp(-k*z));
	return -a2*(1-Math.exp(k*z));
   }
}
