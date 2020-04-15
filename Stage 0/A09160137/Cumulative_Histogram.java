import ij.*;
import java.awt.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.lang.Math;
import ij.ImagePlus;
import java.awt.Color;

public class Cumulative_Histogram implements PlugInFilter {

private double[] histogram = new double[256];

 public int setup(String args, ImagePlus im) {
  return DOES_RGB;
 }

 public void run(ImageProcessor ip) {
	double[][] histograms = new double[][] {new double[256],new double[256],new double[256]};
        double size = ip.getWidth() * ip.getHeight();
        for (int i = 0; i < ip.getWidth(); i++)
            for (int j = 0; j < ip.getHeight(); j++) {
                Color color = new Color(ip.getPixel(i, j));
                histograms[0][color.getRed()]++;
                histograms[1][color.getGreen()]++;
                histograms[2][color.getBlue()]++;
            }
        histograms[0][0] = histograms[0][0] / size;
        histograms[1][0] = histograms[1][0] / size;
        histograms[2][0] = histograms[2][0] / size;

        IJ.log("Value   Red   Green   Blue   ");
        IJ.log(String.format("0   %1$,.10f   %2$,.10f   %3$,.10f   ", histograms[0][0], histograms[1][0], histograms[2][0]));

        for (int i = 1; i < 256; i++) {
            histograms[0][i] = (histograms[0][i] / size) + histograms[0][i-1];
            histograms[1][i] = (histograms[1][i] / size) + histograms[1][i-1];
            histograms[2][i] = (histograms[2][i] / size) + histograms[2][i-1];
            IJ.log(String.format("%1d   %2$,.10f   %3$,.10f   %4$,.10f   ", i, histograms[0][i], histograms[1][i], histograms[2][i]));
 } 
}
}