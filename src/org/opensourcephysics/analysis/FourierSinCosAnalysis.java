package org.opensourcephysics.analysis;
import org.opensourcephysics.numerics.FFTReal;
import org.opensourcephysics.display.*;

import java.awt.Color;

/**
 * FourierAnalysis adds gutter points to real data before performing a fast Fourier transform.
 * Gutter points increase the number points in order to approximate a nonperiodic function.
 *
 * The FFT output is phase shifted to account for the fact that the FFT basis functions are
 * defined on [0, 2*pi].
 *
 * @author W. Christian
 * @version 1.0
 */
public class FourierSinCosAnalysis implements Data {

   static final double PI2 = 2*Math.PI;
   FFTReal fft = new FFTReal();
   double[] fftData, omega, freqs;
   private double[] cosVec, sinVec, gutterVec;
   ComplexDataset[] complexDatasets = new ComplexDataset[1];
   Dataset[] realDatasets = new Dataset[3];
   boolean radians = false;

   /**
    * Fourier analyzes the given data y[] after adding gutter points at the start and end of the z[] array.
    *
    * @param x double[]
    * @param y double[]
    * @param gutter int
    * @return double[] the Fourier spectrum
    */
   public double[] doAnalysis(double[] x, double[] y, int gutter) {
	  int offset = y.length%2;  // zero if even number of points; one if odd number of points
      fftData = new double[y.length+2*gutter-offset];
      gutterVec = new double[gutter];
      System.arraycopy(y, 0, fftData, gutter, y.length-offset);
      fft.transform(fftData); // Computes the FFT of data leaving the result in fft_pts.
      double dx = x[1]-x[0];
      double xmin = x[0]-gutter*dx;
      double xmax = x[x.length-1-offset]+(gutter+1)*dx;
      omega = fft.getNaturalOmega(xmin, xmax);
      freqs = fft.getNaturalFreq(xmin, xmax);
      cosVec = new double[omega.length];
      sinVec = new double[omega.length];
      double norm=2.0/y.length;
      //double norm=2.0/fftData.length;
      for(int i = 0, nOmega = omega.length; i<nOmega; i++) {
         cosVec[i] = norm*Math.cos(omega[i]*xmin);
         sinVec[i] = norm*Math.sin(omega[i]*xmin);
      }
      cosVec[0] *=0.5;  // constant coefficient has factor of 1/2.
      sinVec[0] *=0.5;
      for(int i = 0, nOmega = omega.length; i<nOmega; i++) {
         double re = fftData[2*i];
         double im = fftData[2*i+1];
         fftData[2*i] = re*cosVec[i]+im*sinVec[i];       // cos coefficient
         fftData[2*i+1] = -im*cosVec[i]+re*sinVec[i];    // sin coefficient
      }
      return fftData;
   }

   /**
    * Repeats the Fourier analysis of the real data y[] with the previously set scale and gutter.
    *
    * @param y double[]
    * @return double[] the Fourier sin/cos coefficients
    */
   public double[] repeatAnalysis(double[] y) {
	  int offset = y.length%2;  // zero if even number of points; one if odd number of points
      if(fftData==null) {
         int n = y.length-offset;
         double[] x = new double[n];
         double x0 = 0, dx = 1.0/n;
         for(int i = 0; i<n; i++) {
            x[i] = x0;
            x0 += dx;
         }
         doAnalysis(x, y, 0);
      }
      System.arraycopy(gutterVec, 0, fftData, 0, gutterVec.length);                                 // zero the left gutter
      System.arraycopy(gutterVec, 0, fftData, fftData.length-1-gutterVec.length, gutterVec.length); // zero the right gutter
      System.arraycopy(y, 0, fftData, gutterVec.length, y.length-offset);
      fft.transform(fftData);                                                                       // Computes the FFT of data leaving the result in fft_pts.
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
    * Complexdatasets are not available.
    *
    * @return list of ComplexDatasets
    */
   public java.util.ArrayList<ComplexDataset> getComplexDatasets() {
     return new java.util.ArrayList<ComplexDataset>();
   }

   /**
    * Gets the datasets that contain the result of the last Fourier analysis.
    * The power spectrum is contained in the first dataset.
    * Sine coefficients are contained in the second dataset.
    * Cosine coefficients are in the third dataset.
    *
    * Dataset x-values are either frequencies (cycles) or angular frequencies (radians) depending
    * on the value of the radians flag.
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
        		DisplayRes.getString("FourierAnalysis.Column.Frequency"),  //$NON-NLS-1$
        		DisplayRes.getString("FourierSinCosAnalysis.Column.Power"),  //$NON-NLS-1$
        		DisplayRes.getString("FourierSinCosAnalysis.PowerSpectrum"));  //$NON-NLS-1$
        realDatasets[0].setLineColor(Color.GREEN.darker());
        realDatasets[0].setMarkerColor(Color.GREEN.darker());
        realDatasets[0].setMarkerShape(Dataset.BAR);
        realDatasets[0].setMarkerSize(4);
        realDatasets[1] = new Dataset();
        realDatasets[1].setXYColumnNames(
        		DisplayRes.getString("FourierAnalysis.Column.Frequency"),  //$NON-NLS-1$
        		DisplayRes.getString("FourierSinCosAnalysis.Column.Cosine"),  //$NON-NLS-1$
        		DisplayRes.getString("FourierSinCosAnalysis.CosineCoefficients"));  //$NON-NLS-1$
        realDatasets[1].setLineColor(Color.CYAN.darker());
        realDatasets[1].setMarkerColor(Color.CYAN.darker());
        realDatasets[1].setMarkerShape(Dataset.BAR);
        realDatasets[1].setMarkerSize(4);
        realDatasets[2] = new Dataset();
        realDatasets[2].setXYColumnNames(
        		DisplayRes.getString("FourierAnalysis.Column.Frequency"),  //$NON-NLS-1$
        		DisplayRes.getString("FourierSinCosAnalysis.Column.Sine"),  //$NON-NLS-1$
        		DisplayRes.getString("FourierSinCosAnalysis.SineCoefficients"));  //$NON-NLS-1$
        realDatasets[2].setLineColor(Color.BLUE.darker());
        realDatasets[2].setMarkerColor(Color.BLUE.darker());
        realDatasets[2].setMarkerShape(Dataset.BAR);
        realDatasets[2].setMarkerSize(4);
      } else {
         realDatasets[0].clear();
         realDatasets[1].clear();
         realDatasets[2].clear();
      }
      if(radians) {
         for(int i = 0, nOmega = omega.length; i<nOmega; i++) {
            double cos = fftData[2*i], sin = fftData[2*i+1];
            realDatasets[0].append(omega[i], sin*sin+cos*cos);
            realDatasets[1].append(omega[i], sin);
            realDatasets[2].append(omega[i], cos);
         }
      } else {
         for(int i = 0, nFreqs = freqs.length; i<nFreqs; i++) {
            double sin = fftData[2*i], cos = fftData[2*i+1];
            realDatasets[0].append(freqs[i], sin*sin+cos*cos);
            realDatasets[1].append(freqs[i], sin);
            realDatasets[2].append(freqs[i], cos);
         }
      }
      list.add(realDatasets[0]);
      list.add(realDatasets[1]);
      list.add(realDatasets[2]);
      return list;
   }

   /**
    * Gets the frequencies, power, cos, and sin coefficients.
    * @return double[][]
    */
   public double[][] getData2D() {
      if(fftData==null) {
         return null;
      }
      double[][] data = new double[4][];
      int n = fftData.length/2;
      data[1] = new double[n];
      data[2] = new double[n];
      data[3] = new double[n];
      for(int i = 0; i<n; i++) {
         double cos = fftData[2*i], sin = fftData[2*i+1];
         data[1][i] = sin*sin+cos*cos;
         data[2][i] = cos;
         data[3][i] = sin;
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
