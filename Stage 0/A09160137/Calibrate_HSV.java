import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.ImagePlus;

import java.awt.Color;
import java.util.*;

public class Calibrate_HSV implements PlugInFilter {

    public int setup(String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run(ImageProcessor ip) {
        ImageProcessor referenceImageProcessor = IJ.openImage().getProcessor();
        TreeMap<Double, Double> histogramHue = new TreeMap<Double, Double>();
        TreeMap<Double, Double> histogramSat = new TreeMap<Double, Double>();
        TreeMap<Double, Double> histogramBright = new TreeMap<Double, Double>();
        TreeMap<Double, Double> histogramHueR = new TreeMap<Double, Double>();
        TreeMap<Double, Double> histogramSatR = new TreeMap<Double, Double>();
        TreeMap<Double, Double> histogramBrightR = new TreeMap<Double, Double>();
        setHistogram(ip, histogramHue, histogramSat, histogramBright);
        setHistogram(referenceImageProcessor, histogramHueR, histogramSatR, histogramBrightR);


        for (int col = 0; col < ip.getWidth(); col++) {
            for (int row = 0; row < ip.getHeight(); row++) {
                double[] hsv = getHSV(col, row, ip);
                float hue = (float) findClosest(histogramHue.get(hsv[0]), histogramHueR);
                float sat = (float) findClosest(histogramSat.get(hsv[1]), histogramSatR);
                float val = (float) findClosest(histogramBright.get(hsv[2]), histogramBrightR);
                int rgb = Color.HSBtoRGB(hue, sat, val);
                ip.putPixel(col, row, rgb);
            }
        }
    }

    private void setHistogram(ImageProcessor ip, TreeMap<Double, Double> histogramHue, TreeMap<Double, Double>  histogramSat, TreeMap<Double, Double> histogramBright) {
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
    private double findClosest(double value, TreeMap<Double, Double> histogram) {
        double key = 0.0;
        for (Map.Entry<Double, Double> entry : histogram.entrySet()) {
            if (value < histogram.get(entry.getKey())) {
                return entry.getKey();
            }
            key = entry.getKey();
        }
        return key;
    }
}