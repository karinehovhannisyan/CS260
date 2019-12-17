import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.ImagePlus;
import java.awt.Color;

public class RGB_Calibration implements PlugInFilter {
    ImageProcessor referenceImageProcessor;
    double[][] referenceHistogram = new double[3][256];
    double[][] originalHistogram = new double[3][256];
    private int size, width, height;
    public int setup (String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run (ImageProcessor ip) {
        width = ip.getWidth();
        height = ip.getHeight();
        size = width * height;
        setReferenceImageProcessor();
        ordinaryHistogram(ip, originalHistogram);
        accumulateHistogram(originalHistogram, size);
        ordinaryHistogram(ip, referenceHistogram);
        accumulateHistogram(referenceHistogram, referenceImageProcessor.getWidth() * referenceImageProcessor.getHeight());
        calibrate();
    }

    private void setReferenceImageProcessor() {
        referenceImageProcessor = IJ.openImage().getProcessor();
    }

    private void calibrate() {
        int[] r = new int[256];
        int[] g = new int[256];
        int[] b = new int[256];

        for (int i = 0; i < 256; i++) {
            r[i] = closest(referenceHistogram[0], originalHistogram[0][i]);
            g[i] = closest(referenceHistogram[1], originalHistogram[1][i]);
            b[i] = closest(referenceHistogram[2], originalHistogram[2][i]);
        }
        
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                Color color = new Color(ip.getPixel(col, row));
                ip.putPixel(col, row, new int[] {r[color.getRed()], g[color.getGreen()], b[color.getBlue()]});
            }
        }
    }

    private int closest(double[] referenceHistogram, double value) {
        int index = 255;
        while (referenceHistogram[index] >= value && index > 0)
            index--;
        return index;
    }

    private void ordinaryHistogram(ImageProcessor ip, double[][] histogram ) {
        for (int col = 0; col < ip.getWidth(); col++)
            for (int row = 0; row < ip.getHeight(); row++)
                histogram[0][(new Color(ip.getPixel(col, row))).getRed()]++;
                histogram[1][(new Color(ip.getPixel(col, row))).getGreen()]++;
                histogram[2][(new Color(ip.getPixel(col, row))).getBlue()]++;
    }

    private void accumulateHistogram(double[][] histogram, int size) {

        histogram[0][0] /= size;
        histogram[1][0] /= size;
        histogram[2][0] /= size;

        for (int i = 1; i < 256; i++) {
            histogram[0][i] = (histogram[0][i] / size) + histogram[0][i-1];
            histogram[1][i] = (histogram[1][i] / size) + histogram[1][i-1];
            histogram[2][i] = (histogram[2][i] / size) + histogram[2][i-1];
        }
    }

}
