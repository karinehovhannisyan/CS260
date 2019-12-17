import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.ImagePlus;

import java.awt.Color;
import java.util.*;

public class HSV_Calibration implements PlugInFilter {
    ImageProcessor referenceImageProcessor;
    TreeMap<Double, Double> HueOriginal = new TreeMap<Double, Double>();
    TreeMap<Double, Double> SaturationOriginal = new TreeMap<Double, Double>();
    TreeMap<Double, Double> ValOriginal = new TreeMap<Double, Double>();
    TreeMap<Double, Double> HueReference = new TreeMap<Double, Double>();
    TreeMap<Double, Double> SaturationReference = new TreeMap<Double, Double>();
    TreeMap<Double, Double> ValueReference = new TreeMap<Double, Double>();
    int size, width, height;

    public int setup(String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run(ImageProcessor ip) {
        size = ip.getWidth() * ip.getHeight();
        width = ip.getWidth();
        height = ip.getHeight();
        referenceImageProcessor = IJ.openImage().getProcessor();
        int referenceSize = referenceImageProcessor.getWidth() * referenceImageProcessor.getHeight();
        setHistogram(ip, HueOriginal, SaturationOriginal, ValOriginal);
        setHistogram(referenceImageProcessor, HueReference, SaturationReference, ValueReference);
        accumulateHistogram(HueOriginal, size);
        accumulateHistogram(SaturationOriginal, size);
        accumulateHistogram(ValOriginal, size);
        accumulateHistogram(HueReference, referenceSize);
        accumulateHistogram(SaturationReference, referenceSize);
        accumulateHistogram(ValueReference, referenceSize);
        calibrate(ip);
    }

    private void calibrate(ImageProcessor ip) {
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                double[] hsv = getHSV(col, row, ip);
                float hue = (float) findClosest(HueOriginal.get(hsv[0]), HueReference);
                float sat = (float) findClosest(SaturationOriginal.get(hsv[0]), SaturationReference);
                float val = (float) findClosest(ValOriginal.get(hsv[0]), ValueReference);
                int rgb = Color.HSBtoRGB(hue, sat, val);
                ip.putPixel(col, row, rgb);
            }
        }
    }

    private void setHistogram(ImageProcessor ip, TreeMap<Double, Double> hughHistogram, TreeMap<Double, Double> satHistogram, TreeMap<Double, Double> valHistogram) {
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                double[] hsv = getHSV(col, row, ip);
                increaseHistogram(hughHistogram, hsv[0]);
                increaseHistogram(satHistogram, hsv[1]);
                increaseHistogram(valHistogram, hsv[2]);
            }
        }
    }

    private void increaseHistogram(TreeMap<Double, Double> histogram, double key) {
        if (histogram.containsKey(key)) {
            histogram.put(key, histogram.get(key) + 1);
        } else {
            histogram.put(key, 1.);
        }
    }

    private void accumulateHistogram(TreeMap<Double, Double> histogram, int imageSize) {
        double previousValue = 0.0;
        for (Map.Entry<Double, Double> entry : histogram.entrySet()) {
            histogram.put(entry.getKey(), (histogram.get(entry.getKey()) / imageSize) + previousValue);
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

    private double[] getHSV(int i, int j, ImageProcessor ip) {
        Color color = new Color(ip.getPixel(i, j));
        float[] hsvFloat = new float[3];
        return toDouble(Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsvFloat));
    }

    private static double[] toDouble(float[] input) {
        if (input == null) {
            return null;
        }
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = input[i];
        }
        return output;
    }
}