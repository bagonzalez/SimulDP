package org.opensourcephysics.ejs.control;

import org.opensourcephysics.controls.SimControl;
import org.opensourcephysics.ejs.control.value.*;

/**
 * An Ejs control that behaves like a standard OSP control insofar as it parses mathematical expressions
 * stored as strings to produce integers and doubles.
 *
 * @author W. Christian
 * @version 1.0
 */
public class ParsedEjsControl extends EjsControl implements SimControl {

   public ParsedEjsControl(Object simulation){
      super(simulation);
   }

   /**
    * Gets the double keyed to this value.
    *
    * String values are converted to double using a math expression parser.
    *
    * @param var String
    * @return double
    */
   public double getDouble(String var){
      Value value = getValue(var);
      if (value instanceof DoubleValue){
         return super.getDouble(var);
      }else if (value instanceof IntegerValue){
         return super.getInt(var);
      } else {
         String str= super.getString(var);
         try{
            return Double.parseDouble(str);
         }catch (NumberFormatException ex){
            return org.opensourcephysics.numerics.Util.evalMath(str);
         }
      }
   }

   /**
    * Gets the object keyed to the variable.
    * @param var String
    * @return Object
    */
   public Object getObject(String var) {
     Value value = getValue(var);
     if (value == null){
       return null;
     }else if (value instanceof IntegerValue) {
       return new Integer(super.getInt(var));
     }else if (value instanceof DoubleValue) {
       return new Double(super.getDouble(var));
     }else if (value instanceof BooleanValue) {
       return new Boolean(super.getBoolean(var));
     } else if (value instanceof StringValue) {
       return super.getString(var);
     }
     return super.getObject(var);
   }

   /**
    * Gets the integer keyed to this value.
    *
    * String values are converted to int using a math expression parser.
    *
    * @param var String
    * @return double
    */
   public int getInt(String var){
      Value value = getValue(var);
      if (value instanceof IntegerValue){
         return super.getInt(var);
      }else if (value instanceof DoubleValue){
         return (int)super.getDouble(var);
      } else {
         String str= super.getString(var);
         try{
            return Integer.parseInt(str);
         }catch(NumberFormatException ex){
            return (int) org.opensourcephysics.numerics.Util.evalMath(str);
         }
      }
   }
// Ejs Control properties are set within the model.  Variables can be changed at any time.
public void removeParameter(String name) {
	setValue(name, (Object)null);
	variableTable.remove(name);
}

public void setAdjustableValue(String name, boolean val) {
	setValue(name, val);	
}

public void setAdjustableValue(String name, double val) {
	setValue(name, val);
}

public void setAdjustableValue(String name, int val) {
	setValue(name, val);	
}

public void setAdjustableValue(String name, Object val) {
	setValue(name, val);
}

public void setParameterToFixed(String name, boolean fixed) {
	//  Do nothing here.  Model should set visual element's enabled and editable property.
}

}
