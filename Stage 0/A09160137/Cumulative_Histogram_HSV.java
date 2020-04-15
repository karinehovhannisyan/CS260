import ij.*;
import java.awt.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.lang.Math;
import ij.ImagePlus;
import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;

public class Cumulative_Histogram_HSV implements PlugInFilter {

    private double[] histogram = new double[256];

    public int setup(String args, ImagePlus im) {
        return DOES_RGB;
    }
    public void run(ImageProcessor ip) {
        TreeMap<Double, Double> histogramHue = new TreeMap<Double, Double>();
        TreeMap<Double, Double> histogramSat = new TreeMap<Double, Double>();
        TreeMap<Double, Double> histogramBright = new TreeMap<Double, Double>();
        int size = ip.getWidth() * ip.getHeight();
        for (int i = 0; i < ip.getWidth(); i++)
            for (int j = 0; j < ip.getHeight(); j++) {
                double[] hsv = getHSV(i, j, ip);
                set(histogramHue, hsv[0]);
                set(histogramSat, hsv[1]);
                set(histogramBright, hsv[2]);
            }

        accumulate(histogramHue, size);
        accumulate(histogramSat, size);
        accumulate(histogramBright, size);

        IJ.log("Hue");
        logHistogram(histogramHue);
        IJ.log("Sat");
        logHistogram(histogramSat);
        IJ.log("Val");
        logHistogram(histogramBright);
    }

    private void logHistogram(TreeMap<Double, Double> histogram) {
        for (Map.Entry<Double, Double> entry : histogram.entrySet()) {
            IJ.log("Key: " + entry.getKey() + ". Value: " + entry.getValue());
        }
    }

    private void set(TreeMap<Double, Double> hist, double index) {
        if (hist.containsKey(index))
            hist.put(index, hist.get(index) + 1);
        else
            hist.put(index, 1.);
    }

    private double[] getHSV(int col, int row, ImageProcessor ip) {
        Color color = new Color(ip.getPixel(col, row));
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        float[] hsvFloat = new float[3];
        Color.RGBtoHSB(r, g, b, hsvFloat);
        double[] hsv = new double[3];
        hsv[0] = (double) hsvFloat[0];
        hsv[1] = (double) hsvFloat[1];
        hsv[2] = (double) hsvFloat[2];
        return hsv;
    }

    private void accumulate(TreeMap<Double, Double> histogram, int size) {
        double previousValue = 0.0;
        for (Map.Entry<Double, Double> entry : histogram.entrySet()) {
            histogram.put(entry.getKey(), (histogram.get(entry.getKey()) / size) + previousValue);
            previousValue = histogram.get(entry.getKey());
        }
    }
}