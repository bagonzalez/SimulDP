package org.opensourcephysics.analysis;
import org.opensourcephysics.numerics.FFT;
import org.opensourcephysics.display.*;

import java.awt.Color;

/**
 * FourierAnalysis adds gutter points to complex-number data before performing a fast Fourier transform.
 * Gutter points increase the number points in order to approximate a nonperiodic function.
 *
 * The FFT output is phase shifted to account for the fact that the FFT basis functions are
 * defined on [0, 2*pi].
 *
 * @author W. Christian
 * @version 1.0
 */
public class FourierAnalysis implements Data {

   static final double PI2 = 2*Math.PI;
   FFT fft = new FFT();
   double[] fftData, omega, freqs;
   double[] cosVec, sinVec, gutterVec;
   ComplexDataset[] complexDatasets = new ComplexDataset[1];
   Dataset[] realDatasets = new Dataset[2];
   boolean radians = false;

   /**
    * Fourier analyzes the given complex data z[] after adding gutter points at the start and end of the z[] array.
    *
    * @param x double[]
    * @param z double[]
    * @param gutter int
    * @return double[] the Fourier spectrum
    */
   public double[] doAnalysis(double[] x, double[] z, int gutter) {
      fftData = new double[z.length+4*gutter];
      gutterVec = new double[2*gutter];
      System.arraycopy(z, 0, fftData, 2*gutter, z.length);
      fft.transform(fftData); // Computes the FFT of data leaving the result in fft_pts.
      fft.toNaturalOrder(fftData);
      double dx = x[1]-x[0];
      double xmin = x[0]-gutter*dx;
      double xmax = x[x.length-1]+(gutter+1)*dx;
      omega = fft.getNaturalOmega(xmin, xmax);
      freqs = fft.getNaturalFreq(xmin, xmax);
      cosVec = new double[omega.length];
      sinVec = new double[omega.length];
      double norm=fftData.length/(z.length);
      //double norm=1;
      for(int i = 0, nOmega = omega.length; i<nOmega; i++) {
         cosVec[i] = norm*Math.cos(omega[i]*xmin);
         sinVec[i] = norm*Math.sin(omega[i]*xmin);
      }
      for(int i = 0, nOmega = omega.length; i<nOmega; i++) {
         double re = fftData[2*i];
         double im = fftData[2*i+1];
         fftData[2*i] = re*cosVec[i]+im*sinVec[i];
         fftData[2*i+1] = im*cosVec[i]-re*sinVec[i];
      }
      return fftData;
   }

   /**
    * Repeats the Fourier analysis of the complex data z[] with the previously set scale and gutter.
    *
    * @param z double[]
    * @return double[] the Fourier spectrum
    */
   public double[] repeatAnalysis(double[] z) {
      if(fftData==null) {
         int n = z.length;
         double[] x = new double[n];
         double x0 = 0, dx = 1.0/n;
         for(int i = 0; i<n; i++) {
            x[i] = x0;
            x0 += dx;
         }
         doAnalysis(x, z, 0);
      }
      System.arraycopy(gutterVec, 0, fftData, 0, gutterVec.length);                                 // zero the left gutter
      System.arraycopy(gutterVec, 0, fftData, fftData.length-1-gutterVec.length, gutterVec.length); // zero the right gutter
      System.arraycopy(z, 0, fftData, gutterVec.length, z.length);
      fft.transform(fftData);                                                                       // Computes the FFT of data leaving the result in fft_pts.
      fft.toNaturalOrder(fftData);
      for(int i = 0, nOmega = omega.length; i<nOmega; i++) {
         double re = fftData[2*i];
         double im = fftData[2*i+1];
         fftData[2*i] = re*cosVec[i]+im*sinVec[i];
         fftData[2*i+1] = im*cosVec[i]-re*sinVec[i];
      }
      return fftData;
   }

   /**
    * Gets the angular frequencies of the Fourier spectrum.
    * @return double[]
    */
   public double[] getNaturalOmega() {
      return omega;
   }

   /**
    * Gets the frequencies of the Fourier spectrum.
    * @return double[]
    */
   public double[] getNaturalFreq() {
      return freqs;
   }

   /**
    * Sets the radians flag for the frequency values of datasets.
    * Dataset x-values are either frequencies (cycles) or angular frequencies (radians) depending
    * on the value of the radians flag.
    *
    * @param radians boolean
    */
   public void useRadians(boolean radians) {
      this.radians = radians;
   }

   /**
    * Gets the radians flag.
    * Radians is true if the dataset uses angular frequency as the x-coordinate.
    *
    * @return boolean
    */
   public boolean isRadians() {
      return radians;
   }

   /**
    * Gets a complex dataset that contains the result of the last Fourier analysis.
    * Complex dataset x-values are either frequencies (cycles) or angular frequencies (radians) depending
    * on the value of the radians flag.
    *
    * @return list of ComplexDatasets
    */
   public java.util.ArrayList<ComplexDataset> getComplexDatasets() {
     java.util.ArrayList<ComplexDataset> list = new java.util.ArrayList<ComplexDataset>();
      if(fftData==null) {
         return list;
      }
      if(complexDatasets[0]==null) {
         complexDatasets[0] = new ComplexDataset();
         complexDatasets[0].setXYColumnNames(
        		 DisplayRes.getString("FourierAnalysis.Column.Frequency"), //$NON-NLS-1$
        		 DisplayRes.getString("FourierAnalysis.Column.Real"), //$NON-NLS-1$
        		 DisplayRes.getString("FourierAnalysis.Column.Imaginary")); //$NON-NLS-1$
      } else {
         complexDatasets[0].clear();
      }
      if(radians) {
         complexDatasets[0].append(omega, fftData);
      } else {
         complexDatasets[0].append(freqs, fftData);
      }
      list.add(complexDatasets[0]);
      return list;
   }

   /**
    * Gets the complex datasets that contain the result of the last Fourier analysis.
    * Real coefficients are contained in the first dataset.
    * Complex coefficients are in the second dataset.
    *
    * @return list of Datasets
    */
   public java.util.ArrayList<Dataset> getDatasets() {
     java.util.ArrayList<Dataset> list = new java.util.ArrayList<Dataset>();
      if(fftData==null) {
         return list;
      }
      if(realDatasets[0]==null) {
         realDatasets[0] = new Dataset();
         realDatasets[0].setXYColumnNames(
        		 DisplayRes.getString("FourierAnalysis.Column.Frequency"), //$NON-NLS-1$
        		 DisplayRes.getString("FourierAnalysis.Column.Real"), //$NON-NLS-1$
        		 DisplayRes.getString("FourierAnalysis.RealCoefficients")); //$NON-NLS-1$
         realDatasets[0].setLineColor(Color.RED);
         realDatasets[1] = new Dataset();
         realDatasets[1].setXYColumnNames(
        		 DisplayRes.getString("FourierAnalysis.Column.Frequency"), //$NON-NLS-1$
        		 DisplayRes.getString("FourierAnalysis.Column.Imaginary"), //$NON-NLS-1$
        		 DisplayRes.getString("FourierAnalysis.ImaginaryCoefficients")); //$NON-NLS-1$
         realDatasets[1].setLineColor(Color.BLUE);
      } else {
         realDatasets[0].clear();
         realDatasets[1].clear();
      }
      if(radians) {
         for(int i = 0, nOmega = omega.length; i<nOmega; i++) {
            double re=fftData[2*i], im=fftData[2*i+1];
            realDatasets[0].append(omega[i], re);
            realDatasets[1].append(omega[i], im);
         }
      } else {
         for(int i = 0, nFreqs = freqs.length; i<nFreqs; i++) {
            double re=fftData[2*i], im=fftData[2*i+1];
            realDatasets[0].append(freqs[i], re);
            realDatasets[1].append(freqs[i], im);
         }
      }
      list.add(realDatasets[0]);
      list.add(realDatasets[1]);
      return list;
   }

   /**
    * Gets the frequencies, real, and imaginary coefficients.
    * @return double[][]
    */
   public double[][] getData2D() {
      if(fftData==null) {
         return null;
      }
      double[][] data = new double[3][];
      int n = fftData.length/2;
      data[1] = new double[n];
      data[2] = new double[n];
      for(int i = 0; i<n; i++) {
         double re = fftData[2*i], im = fftData[2*i+1];
         data[1][i] = re;
         data[2][i] = im;
      }
      if(radians) {
         data[0] = omega;
      } else {
         data[0] = freqs;
      }
      return data;
   }

   /**
    * 3D data is not available.
    *
    * @return double[][][]
    */
   public double[][][] getData3D() {
      return null;
   }
}
