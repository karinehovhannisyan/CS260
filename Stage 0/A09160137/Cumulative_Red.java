import ij.*;
import java.awt.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.lang.Math;
import ij.ImagePlus;
import java.awt.Color;

public class Cumulative_Green implements PlugInFilter {

private double[] histogram = new double[256];

 public int setup(String args, ImagePlus im) {
  return DOES_RGB;
 }

 public void run(ImageProcessor ip) {
	ordinaryHistogram(ip);
	accumulateHistogram(ip.getWidth() * ip.getHeight());
	logResult();
 }


 private void ordinaryHistogram(ImageProcessor ip) {
  for (int col = 0; col < ip.getWidth(); col++)
   for (int row = 0; row < ip.getHeight(); row++)
    histogram[(new Color(ip.getPixel(col, row))).getRed()]++;
 }

 private void accumulateHistogram(int size) {
  
  histogram[0] /= size;
  
  for (int i = 1; i < 256; i++)
   histogram[i] = (histogram[i] / size) + histogram[i - 1];
 }
 
 private void logResult() {
  for (int i = 0; i < histogram.length; i++)
   IJ.log(Double.toString(histogram[i]));
 }
 
}