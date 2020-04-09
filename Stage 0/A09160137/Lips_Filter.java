import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.*;

public class Lips_Filter implements PlugInFilter {
    int LINE_COLOR = 65280;
    int width, height;

    public int setup(String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run(ImageProcessor ip) {
        RankFilters rankFilter = new RankFilters();
        this.width = ip.getWidth();
        this.height = ip.getHeight();
        ImageProcessor duplicateIp = ip.duplicate();
        rankFilter.rank(duplicateIp, 4, RankFilters.MIN);
        this.runBinaryLayer3(duplicateIp);
        //rankFilter.rank(duplicateIp, 3, RankFilters.MEDIAN);
        //rankFilter.rank(duplicateIp, 2, RankFilters.MAX);
        //rankFilter.rank(duplicateIp, 2, RankFilters.MIN);
        this.findEyes(duplicateIp, ip);
    }

    public void runBinaryLayer3(ImageProcessor ip) {
        int r, g, b;
        double rb;
        Color color;
        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++) {
                color = new Color(ip.getPixel(col, row));
                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();
                rb = (r + b) / 2.;
                if (b < g && g < r && rb >= bottom(g) && rb <= top(g))
                    ip.putPixel(col, row, 0); //BLACK
                else
                    ip.putPixel(col, row, 16777215); //WHITE
            }
    }

    private void findEyes(ImageProcessor ip, ImageProcessor drawBoxIp) {
        ArrayList<int[]> lips = findLargestRegion(ip);
        int i2 = ip.getWidth(), j2 = ip.getHeight(), i1 = 0, j1 = 0;
        for (int[] pt : lips) {
            if (pt[0] < i2) i2 = pt[0];
            else if (pt[0] > i1) i1 = pt[0];
            if (pt[1] < j2) j2 = pt[1];
            else if (pt[1] > j1) j1 = pt[1];
        }
        if (Math.abs(i2-i1) <= 20 || Math.abs(j2-j1) <= 20) {
            j2 -= 10;
            j1 += 10;
            i2 -= 25;
            i1 += 25;
        }
        drawLipBox(drawBoxIp, i2, j2, i1, j1);
    }

    private ArrayList<int[]> findLargestRegion(ImageProcessor ip) {
        Hashtable<Integer, ArrayList<int[]>> regions = new Hashtable<Integer, ArrayList<int[]>>();
        int width = ip.getWidth();
        int height = ip.getHeight();
        ImageProcessor ipForLabels = ip.duplicate();
        int label = 1;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color color = new Color(ipForLabels.getPixel(i, j));
                if (isBlack(color)) {
                    floodFill(regions, ipForLabels, i, j, label);
                    label++;
                }
            }
        }
        int cnt = 0;
        ArrayList<int[]> max = null;
        for (int labelRegion : regions.keySet()) {
            ArrayList<int[]> r = regions.get(labelRegion);
            if (r.size() > cnt) {
                cnt = r.size();
                max = r;
            }
        }
        return max;
    }

    private void floodFill(Hashtable<Integer, ArrayList<int[]>> list, ImageProcessor ip, int u, int v, int label) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        Deque<Point> S = new LinkedList<Point>();
        S.push(new Point(u, v));
        while (!S.isEmpty()) {
            Point p = S.pop();
            int x = p.x;
            int y = p.y;
            Color color = new Color(ip.getPixel(x, y));
            if ((x >= 0) && (x < width) && (y >= 0) && (y < height) && isBlack(color)) {
                ip.putPixel(x, y, new int[]{label, 0, 0});
                int[] pt = new int[]{x, y};
                if (list.containsKey(label)) {
                    list.get(label).add(pt);
                } else {
                    list.put(label, new ArrayList<int[]>(Collections.singletonList(pt)));
                }
                S.push(new Point(x + 1, y));
                S.push(new Point(x, y + 1));
                S.push(new Point(x, y - 1));
                S.push(new Point(x - 1, y));
            }
        }
    }


    private void drawLipBox(ImageProcessor drawBoxIp, int x1, int y1, int x2, int y2) {
        drawHorizontalLine(drawBoxIp, y2, x1, x2);
        drawHorizontalLine(drawBoxIp, y1, x1, x2);
        drawVerticalLine(drawBoxIp, x1, y1, y2);
        drawVerticalLine(drawBoxIp, x2, y1, y2);
    }


    private boolean isBlack(Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        return red == 0 && green == 0 && blue == 0;
    }

    private void drawVerticalLine(ImageProcessor ip, int col, int rowStart, int rowEnd) {
        for (int row = rowStart; row < rowEnd; row++) {
            ip.putPixel(col, row, LINE_COLOR);
        }
    }

    private void drawHorizontalLine(ImageProcessor ip, int row, int colStart, int colEnd) {
        for (int col = colStart; col < colEnd; col++) {
            ip.putPixel(col, row, LINE_COLOR);
        }
    }

    public double bottom(int x) {
        return -0.0013 * x * x + 1.2608 * x + 12.067;
    }
    public double top(int x) {
        return -0.0026 * x * x + 1.5713 * x + 14.8;
    }

}

