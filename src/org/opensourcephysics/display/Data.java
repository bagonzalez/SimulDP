package org.opensourcephysics.display;

/**
 * The Data interface defines methods for obtaining and identifying OSP data.
 *
 * @author Wolfgang Christian, Douglas Brown
 * @version 1.0
 */
public interface Data {

   /**
    * Gets a 2D array of data.
    * The first column, double[0][] often contains x-values;
    * Remaining columns often contain y values.
    * May return null if data not yet generated or object does not support 2D data.
    *
    * @return double[][]
    */
   public double[][] getData2D();

   /**
    * Gets a 3D array of data.
    * May return null if data not yet generated or object does not support 3D data.
    *
    * @return double[][][]
    */
   public double[][][] getData3D();

   /**
    * Gets a list of datasets.
    * May return null if data not yet generated or object does not support Datasets.
    *
    * @return list of Datasets
    */
   public java.util.ArrayList<Dataset>  getDatasets();

   /**
    * Gets a list of complex datasets.
    * May return null if data not yet generated or object does not support ComplexDatasets.
    *
    * @return list of ComplexDatasets
    */
   public java.util.ArrayList<ComplexDataset>  getComplexDatasets();

}
