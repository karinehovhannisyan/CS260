import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.ImagePlus;
import java.awt.Color;

public class Calibrate_RGB implements PlugInFilter {
    ImageProcessor referenceImageProcessor;
    double[][] referenceHistogram = new double[3][256];
    double[][] originalHistogram = new double[3][256];
    private int size, width, height;
    public int setup (String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run(ImageProcessor ip) {
        ImageProcessor reference = IJ.openImage(null).getProcessor();
        double[][] histogram = createHistogram(ip), refHistogram = createHistogram(reference);
        int[] red = new int[256], green = new int[256], blue = new int[256];
        for (int i = 0; i < 256; i++) {
            int calibratedRedColor = getClosest(refHistogram[0], histogram[0][i]);
            int calibratedGreenColor = getClosest(refHistogram[1], histogram[1][i]);
            int calibratedBlueColor = getClosest(refHistogram[2], histogram[2][i]);

            red[i] = calibratedRedColor;
            green[i] = calibratedGreenColor;
            blue[i] = calibratedBlueColor;
        }

        for (int col = 0; col < ip.getWidth(); col++) {
            for (int row = 0; row < ip.getHeight(); row++) {
                Color color = new Color(ip.getPixel(col, row));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                ip.putPixel(col, row, new int[] {red[r], green[g], blue[b]});
            }
        }
    }


    private double[][] createHistogram(ImageProcessor ip) {
        double[][] histograms = new double[][]{new double[256], new double[256], new double[256]};
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

        for (int i = 1; i < 256; i++) {
            histograms[0][i] = (histograms[0][i] / size) + histograms[0][i-1];
            histograms[1][i] = (histograms[1][i] / size) + histograms[1][i-1];
            histograms[2][i] = (histograms[2][i] / size) + histograms[2][i-1];
        }
        return histograms;
    }

    private int getClosest(double[] hist, double val) {
        int index = 255;
        while (hist[index] >= val && index > 0) {
            index--;
        }
        return index;
    }

}
