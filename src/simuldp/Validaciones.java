package simuldp;
public class Validaciones {     
    public boolean EntreCeroyUno(String texto)
    {   try
        {if(Double.parseDouble(texto)>0 && Double.parseDouble(texto)<1){
            return true;}
        else{ return false; }
        }
        catch (NumberFormatException nfe){
        return false;}
    }
 public boolean EsEntero(String texto)
 {
   if(texto.equals("")){
            return false;}
        else{
        if(!texto.matches("[1,2,3,4,5,6,7,8,9,0]*")){
         return false;}
        else{
         return true;}}
 }
 public boolean ValIntFloat(String texto)
 {
  int x=0,i;
     if(texto.equals("")){
        return false;}
    else
    {
         if(!texto.matches("[1,2,3,4,5,6,7,8,9,0,.]*")){
            return false;}
         else
         { char a[];
           a=texto.toCharArray();
           for(i=0; i<=(a.length)-1;i++){
            if(a[i]=='.'){
               x=x+1;}}
           if(x<2)
           {return true;}
           else{
             return false;}
      }
    }
}
 public boolean MayMin(String texto1, String texto2)
 {
  if(Double.parseDouble(texto1)< Double.parseDouble(texto2))
     return true;
  else
     return false;
 }

 public boolean Reales(String texto)
 { int x=0,i;
     if(texto.equals("")){
        return false;}
    else{if(!texto.matches("[1,2,3,4,5,6,7,8,9,0,.,-]*")){
            return false;}
         else
         { char a[];
           a=texto.toCharArray();
           for(i=0; i<=(a.length)-1;i++){
            if(a[i]=='.'){
               x=x+1;}}
           if(x<2)
           {return true;}
           else{ return false;}
      }
 }}
}

