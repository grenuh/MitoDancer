import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

import static java.util.Arrays.*;

class MathRC4 {
    static private final ResourceBundle res = ResourceBundle.getBundle("data");
    static private double[] delta = new double[]{0.95, 0.98, 0.99, 0.995, 1.0, 1.005, 1.01, 1.02, 1.05};

    private static double[] getLine(double[] time, double[] values) {
        RealMatrix coef = null; // will hold prediction coefs once we get values
        if (time.length != values.length) {
            throw new IllegalArgumentException(String.format("The numbers of y and x values must be equal (%d != %d)", values.length, time.length));
        }
        double[][] xData = new double[time.length][];
        for (int i = 0; i < time.length; i++) {
            xData[i] = xVector(time[i]);
        }
        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
        ols.setNoIntercept(true);
        ols.newSampleData(values, xData);
        try {
            coef = MatrixUtils.createColumnRealMatrix(ols.estimateRegressionParameters());
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert coef != null;
        double v2_b = coef.preMultiply(xVector(0))[0];
        double v2_a = coef.preMultiply(xVector(1))[0] - v2_b;
        double[] valuesPredicted = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            valuesPredicted[i] = v2_b + (v2_a * time[i]);
        }
        return new double[]{v2_a, v2_b, getR2(values, valuesPredicted), -1};
    }

    static private double[] xVector(double x) { // {1, x, x*x, x*x*x, ...}
        double[] poly = new double[2];
        double xi = 1;
        for (int i = 0; i <= 1; i++) {
            poly[i] = xi;
            xi *= x;
        }
        return poly;
    }

    static private double getR2(double[] values, double[] valuesPredicted) {
        double ss_res = 0;
        double y_ = 0;
        for (int i = 0; i < values.length; i++) {
            double qdr = (values[i] - valuesPredicted[i]);
            ss_res = ss_res + (qdr * qdr);
            y_ = y_ + values[i];
        }
        y_ = y_ / values.length;
        double ss_tot = 0;
        for (double value : values) {
            double qdr = (value - y_);
            ss_tot = ss_tot + (qdr * qdr);
        }
        return 1 - (ss_res / ss_tot);
    }

    @SuppressWarnings("unchecked")
    static Object[] calculate(String ent, boolean additionalDiagrams, boolean preCalibr, boolean fading, boolean export, boolean preSMA, boolean oldCalc, Object[] fileData) {
        int[] consts = new int[0];
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("objects.dat"));
            consts = (int[]) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        double[] dataPre = (double[]) fileData[0];
        double[] data;
        if (preSMA) {
            data = getSMA(dataPre, 5);
        } else {
            data = dataPre;
        }
        int timeStep = (int) fileData[1];
        ArrayList<Integer> addition = (ArrayList<Integer>) fileData[2];
        String path = (String) fileData[3];
        ArrayList<double[]> formulas = new ArrayList<>();
        String[] arr = ent.split("");
        double[] time = new double[data.length];
        for (int i = 0; i < time.length; i++) {
            time[i] = (double) (i * timeStep) / 1000.0;
        }
        for (int i = 0; i < arr.length; i++) {
            switch (arr[i]) {
                case "1":
                    int a = addition.get(i) - (5000 / timeStep);
                    if (a < 0) {
                        a = 0;
                    }
                    int b = addition.get(i);
                    double[] values1 = new double[b - a];
                    double[] values1Pred = new double[b - a];
                    System.arraycopy(data, a, values1, 0, values1.length);
                    double sum = 0;
                    for (double aValues1 : values1) {
                        sum = sum + aValues1;
                    }
                    double highMark = sum / values1.length;
                    for (int i2 = 0; i2 < values1Pred.length; i2++) {
                        values1Pred[i] = highMark;
                    }
                    formulas.add(new double[]{0.0, highMark, MathRC4.getR2(values1, values1Pred), 1});
                    double aa = ((data[addition.get(i) - 1] - data[addition.get(i)] + 9) / (time[addition.get(i) - 1] - time[addition.get(i)]));
                    double bb = data[addition.get(i) - 1] - (aa * time[addition.get(i) - 1]);
                    double[] pseudo = new double[]{aa, bb, 1.0, 0};
                    formulas.add(pseudo);
                    break;
                case "2":
                    int a2 = addition.get(i);
                    int b2;
                    try {
                        b2 = addition.get(i + 1);
                    } catch (Exception e) {
                        b2 = time.length - 1;
                    }
                    int a_2 = (a2 + b2) / 2;
                    double[] values2 = new double[b2 - a_2];
                    System.arraycopy(data, a_2, values2, 0, values2.length);
                    double[] time2 = new double[b2 - a_2];
                    System.arraycopy(time, a_2, time2, 0, time2.length);
                    double[] res2 = MathRC4.getLine(time2, values2);
                    res2[3] = 2;
                    formulas.add(res2);
                    if (additionalDiagrams) {
                        XYChart chart = new XYChart(600, 600);
                        XYSeries series = chart.addSeries("Raw data ", time2, values2);
                        series.setMarker(SeriesMarkers.NONE);
                        double[] yData2 = new double[time2.length];
                        for (int y = 0; y < yData2.length; y++) {
                            yData2[y] = (res2[0] * time2[y]) + res2[1];
                        }
                        XYSeries series2 = chart.addSeries("Calc data", time2, yData2);
                        series2.setMarker(SeriesMarkers.NONE);
                        if (res2[2] > 0.95) {
                            series2.setLineColor(Color.GREEN);
                        } else {
                            series2.setLineColor(Color.RED);
                        }
                        showChart(chart);
                    }
                    break;
                case "4":
                    int klusters = 2;
                    int a4 = addition.get(i) + (consts[1] / timeStep);
                    int b4;
                    try {
                        b4 = addition.get(i + 1) - (consts[1] / timeStep);
                    } catch (Exception e) {
                        b4 = time.length - 1 - (consts[1] / timeStep);
                    }
                    double[] values4 = new double[b4 - a4];
                    System.arraycopy(data, a4, values4, 0, values4.length);
                    double[] time4 = new double[b4 - a4];
                    System.arraycopy(time, a4, time4, 0, time4.length);
                    ArrayList<double[]> marks = new ArrayList<>();
                    int duration = consts[0] / timeStep;
                    int end4 = values4.length - duration + 1;
                    XYChart chart4 = new XYChart(600, 600);
                    XYChart chart = new XYChartBuilder().width(800).height(600).build();
                    if (additionalDiagrams) {
                        XYSeries series = chart4.addSeries(res.getString("RawData"), time4, values4);
                        series.setMarker(SeriesMarkers.NONE);
                        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
                        chart.getStyler().setChartTitleVisible(false);
                        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideSW);
                        chart.getStyler().setMarkerSize(16);
                    }
                    for (int e = 0; e < end4; e++) {
                        double[] valuesTemp = new double[duration];
                        System.arraycopy(values4, e, valuesTemp, 0, valuesTemp.length);
                        double[] timeTemp = new double[duration];
                        System.arraycopy(time4, e, timeTemp, 0, timeTemp.length);
                        double[] string = MathRC4.getLine(timeTemp, valuesTemp);
                        marks.add(string);
                        if (additionalDiagrams) {
                            double[] yData2 = new double[timeTemp.length];
                            for (int y = 0; y < yData2.length; y++) {
                                yData2[y] = (string[0] * timeTemp[y]) + string[1];
                            }
                            XYSeries series2 = chart4.addSeries(String.valueOf(e), timeTemp, yData2);
                            series2.setMarker(SeriesMarkers.NONE);
                            series2.setLineColor(Color.GREEN);
                        }
                    }
                    DoublePoint[] points = new DoublePoint[marks.size()];
                    for (int yyt = 0; yyt < marks.size(); yyt++) {
                        points[yyt] = new DoublePoint(new double[]{marks.get(yyt)[0], marks.get(yyt)[1]});
                    }
                    KMeansPlusPlusClusterer<DoublePoint> ghhh = new KMeansPlusPlusClusterer<>(klusters);
                    java.util.List<CentroidCluster<DoublePoint>> clusters = ghhh.cluster(asList(points));
                    LinkedHashMap<Integer, double[]> yd2 = new LinkedHashMap<>();
                    for (int g = 0; g < klusters; g++) {
                        yd2.put(g, clusters.get(g).getCenter().getPoint());
                    }
                    double[] xCenter = new double[klusters];//center data
                    double[] yCenter = new double[klusters];//center data
                    double[] xMedian = new double[klusters];//Median data
                    double[] yMedian = new double[klusters];//Median data
                    for (int hh = 0; hh < klusters; hh++) {
                        xCenter[hh] = yd2.get(hh)[0];
                        yCenter[hh] = yd2.get(hh)[1];
                    }
                    for (int iw = 0; iw < clusters.size(); iw++) {
                        ArrayList<Double> xX = new ArrayList<>();
                        ArrayList<Double> yY = new ArrayList<>();
                        for (int k = 0; k < clusters.get(iw).getPoints().size(); k++) {
                            xX.add(clusters.get(iw).getPoints().get(k).getPoint()[0]);
                            yY.add(clusters.get(iw).getPoints().get(k).getPoint()[1]);
                        }
                        if (additionalDiagrams) {
                            chart.addSeries(res.getString("Cluster") + " " + iw, xX, yY);
                        }
                        Collections.sort(xX);
                        Collections.sort(yY);
                        xMedian[iw] = xX.get(xX.size() / 2);
                        yMedian[iw] = yY.get(yY.size() / 2);
                    }
                    int b30p = (a4 + ((values4.length * consts[2]) / 100));
                    double[] values4_1_3 = new double[b30p - a4];
                    System.arraycopy(data, a4, values4_1_3, 0, values4_1_3.length);
                    double[] time4_1_3 = new double[values4_1_3.length];
                    System.arraycopy(time, a4, time4_1_3, 0, time4_1_3.length);
                    double[] medianV3pred = new double[time4_1_3.length];
                    double[] centerV3pred = new double[time4_1_3.length];
                    double[] medianV3pred2 = new double[time4_1_3.length];
                    double[] centerV3pred2 = new double[time4_1_3.length];
                    for (int r = 0; r < time4_1_3.length; r++) {
                        centerV3pred[r] = (xCenter[0] * time4_1_3[r]) + yCenter[0];
                        medianV3pred[r] = (xMedian[0] * time4_1_3[r]) + yMedian[0];
                        centerV3pred2[r] = (xCenter[1] * time4_1_3[r]) + yCenter[1];
                        medianV3pred2[r] = (xMedian[1] * time4_1_3[r]) + yMedian[1];
                    }
                    double centerR2_31 = MathRC4.getR2(values4_1_3, centerV3pred);
                    double medianR2_31 = MathRC4.getR2(values4_1_3, medianV3pred);
                    double centerR2_32 = MathRC4.getR2(values4_1_3, centerV3pred2);
                    double medianR2_32 = MathRC4.getR2(values4_1_3, medianV3pred2);
                    boolean rights = (centerR2_31 + medianR2_31) > (centerR2_32 + medianR2_32);
                    double centerR2_3;
                    double medianR2_3;
                    if (rights) {
                        centerR2_3 = centerR2_31;
                        medianR2_3 = medianR2_31;
                    } else {
                        centerR2_3 = centerR2_32;
                        medianR2_3 = medianR2_32;
                    }
                    int a60p = a4 + ((values4.length * consts[3]) / 100);
                    double[] values4_3_3 = new double[b4 - a60p];
                    System.arraycopy(data, a60p, values4_3_3, 0, values4_3_3.length);
                    double[] time4_3_3 = new double[values4_3_3.length];
                    System.arraycopy(time, a60p, time4_3_3, 0, time4_3_3.length);
                    double[] medianV4pred = new double[time4_3_3.length];
                    double[] centerV4pred = new double[time4_3_3.length];
                    int pl = 0;
                    if (rights) {
                        pl = 1;
                    }
                    for (int r = 0; r < time4_3_3.length; r++) {
                        centerV4pred[r] = (xCenter[pl] * time4_3_3[r]) + yCenter[pl];
                        medianV4pred[r] = (xMedian[pl] * time4_3_3[r]) + yMedian[pl];
                    }
                    double centerR2_4 = MathRC4.getR2(values4_3_3, centerV4pred);
                    double medianR2_4 = MathRC4.getR2(values4_3_3, medianV4pred);
                    int apl = 1 - pl;
                    if (centerR2_3 > medianR2_3) {
                        formulas.add(new double[]{xCenter[apl], yCenter[apl], centerR2_3, 3});
                    } else {
                        formulas.add(new double[]{xMedian[apl], yMedian[apl], medianR2_3, 3});
                    }
                    if (centerR2_4 > medianR2_4) {
                        formulas.add(new double[]{xCenter[pl], yCenter[pl], centerR2_4, 4});
                    } else {
                        formulas.add(new double[]{xMedian[pl], yMedian[pl], medianR2_4, 4});
                    }
                    if (additionalDiagrams) {
                        XYChart chart6 = new XYChart(600, 600);
                        XYSeries series = chart6.addSeries(res.getString("RawData"), time4, values4);
                        series.setMarker(SeriesMarkers.NONE);
                        if (rights) {
                            chart6.addSeries(res.getString("Center") + " 3", time4_1_3, centerV3pred);
                            chart6.addSeries(res.getString("Median") + " 3", time4_1_3, medianV3pred);
                        } else {
                            chart6.addSeries(res.getString("Center") + " 3", time4_1_3, centerV3pred2);
                            chart6.addSeries(res.getString("Median") + " 3", time4_1_3, medianV3pred2);
                        }
                        chart6.addSeries(res.getString("Center") + " 4", time4_3_3, centerV4pred);
                        chart6.addSeries(res.getString("Median") + " 4", time4_3_3, medianV4pred);
                        showChart(chart6);
                    }
                    if (additionalDiagrams) {
                        showChart(chart4);
                        XYSeries series3 = chart.addSeries(res.getString("Center"), xCenter, yCenter);
                        series3.setMarkerColor(Color.RED);
                        XYSeries series4 = chart.addSeries(res.getString("Median"), xMedian, yMedian);
                        series4.setMarkerColor(Color.BLACK);
                        showChart(chart);
                    }
                    break;
                case "5":
                case "6":
                    int a5 = addition.get(i);
                    int b5;
                    try {
                        b5 = addition.get(i + 1);
                    } catch (Exception e) {
                        b5 = time.length - 1;
                    }
                    double[] values5 = new double[b5 - a5];
                    System.arraycopy(data, a5, values5, 0, values5.length);
                    double[] time5 = new double[b5 - a5];
                    System.arraycopy(time, a5, time5, 0, time5.length);
                    double[] res5 = MathRC4.getLine(time5, values5);
                    res5[3] = Integer.parseInt(arr[i]);
                    formulas.add(res5);
                    if (additionalDiagrams) {
                        XYChart chart6 = new XYChart(600, 600);
                        XYSeries series = chart6.addSeries(res.getString("RawData"), time5, values5);
                        series.setMarker(SeriesMarkers.NONE);
                        double[] yData2 = new double[time5.length];
                        for (int y = 0; y < yData2.length; y++) {
                            yData2[y] = (res5[0] * time5[y]) + res5[1];
                        }
                        XYSeries series2 = chart6.addSeries(res.getString("CalcData"), time5, yData2);
                        series2.setMarker(SeriesMarkers.NONE);
                        if (res5[2] > 0.95) {
                            series2.setLineColor(Color.GREEN);
                        } else {
                            series2.setLineColor(Color.RED);
                        }
                        showChart(chart6);
                    }
                    break;
                case "7":
                    int a7 = addition.get(i) + (consts[1] / timeStep);
                    int b7;
                    try {
                        b7 = addition.get(i + 1) - (consts[1] / timeStep);
                    } catch (Exception e) {
                        b7 = time.length - 1 - (consts[1] / timeStep);
                    }
                    double[] values7 = new double[b7 - a7];
                    System.arraycopy(data, a7, values7, 0, values7.length);
                    double[] time7 = new double[b7 - a7];
                    System.arraycopy(time, a7, time7, 0, time7.length);
                    ArrayList<double[]> marks7 = new ArrayList<>();
                    int duration7 = consts[0] / timeStep;
                    int end7 = values7.length - duration7 + 1;
                    XYChart chart47 = new XYChart(600, 600);
                    XYChart chart7 = new XYChartBuilder().width(800).height(600).build();
                    if (additionalDiagrams) {
                        XYSeries series = chart47.addSeries(res.getString("RawData"), time7, values7);
                        series.setMarker(SeriesMarkers.NONE);
                        chart7.getStyler().setMarkerSize(16);
                        chart7.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
                        chart7.getStyler().setChartTitleVisible(false);
                        chart7.getStyler().setLegendPosition(Styler.LegendPosition.InsideSW);
                    }
                    for (int e = 0; e < end7; e++) {
                        double[] timeTemp = new double[duration7];
                        System.arraycopy(time7, e, timeTemp, 0, timeTemp.length);
                        double[] valuesTemp = new double[duration7];
                        System.arraycopy(values7, e, valuesTemp, 0, valuesTemp.length);
                        double[] string = MathRC4.getLine(timeTemp, valuesTemp);
                        marks7.add(string);
                        if (additionalDiagrams) {
                            double[] yData2 = new double[timeTemp.length];
                            for (int y = 0; y < yData2.length; y++) {
                                yData2[y] = (string[0] * timeTemp[y]) + string[1];
                            }
                            XYSeries series2 = chart47.addSeries(String.valueOf(e), timeTemp, yData2);
                            series2.setLineColor(Color.GREEN);
                            series2.setMarker(SeriesMarkers.NONE);
                        }
                    }
                    DoublePoint[] points7 = new DoublePoint[marks7.size()];
                    for (int yyt = 0; yyt < marks7.size(); yyt++) {
                        points7[yyt] = new DoublePoint(new double[]{marks7.get(yyt)[0], marks7.get(yyt)[1]});
                    }
                    KMeansPlusPlusClusterer<DoublePoint> ghhh7 = new KMeansPlusPlusClusterer<>(3);
                    java.util.List<CentroidCluster<DoublePoint>> clusters7 = ghhh7.cluster(asList(points7));
                    LinkedHashMap<Integer, double[]> yd27 = new LinkedHashMap<>();
                    for (int g = 0; g < 3; g++) {
                        yd27.put(g, clusters7.get(g).getCenter().getPoint());
                    }
                    double[] xCenter7 = new double[3];//center data
                    double[] yCenter7 = new double[3];//center data
                    double[] xMedian7 = new double[3];//Median data
                    double[] yMedian7 = new double[3];//Median data
                    for (int hh = 0; hh < 3; hh++) {
                        xCenter7[hh] = yd27.get(hh)[0];
                        yCenter7[hh] = yd27.get(hh)[1];
                    }
                    for (int iw = 0; iw < clusters7.size(); iw++) {
                        ArrayList<Double> xX;
                        ArrayList<Double> yY;
                        xX = new ArrayList<>();
                        yY = new ArrayList<>();
                        for (int k = 0; k < clusters7.get(iw).getPoints().size(); k++) {
                            xX.add(clusters7.get(iw).getPoints().get(k).getPoint()[0]);
                            yY.add(clusters7.get(iw).getPoints().get(k).getPoint()[1]);
                        }
                        if (additionalDiagrams) {
                            chart7.addSeries(res.getString("Cluster") + " " + iw, xX, yY);
                        }
                        Collections.sort(xX);
                        Collections.sort(yY);
                        xMedian7[iw] = xX.get(xX.size() / 2);
                        yMedian7[iw] = yY.get(yY.size() / 2);
                    }
                    int b30p7 = (a7 + ((values7.length * consts[2]) / 100));
                    double[] values7_1_3 = new double[b30p7 - a7];
                    System.arraycopy(data, a7, values7_1_3, 0, values7_1_3.length);
                    double[] time7_1_3 = new double[values7_1_3.length];
                    System.arraycopy(time, a7, time7_1_3, 0, time7_1_3.length);
                    double[] medianV3pred7 = new double[time7_1_3.length];
                    double[] centerV3pred7 = new double[time7_1_3.length];
                    double[] medianV3pred27 = new double[time7_1_3.length];
                    double[] centerV3pred27 = new double[time7_1_3.length];
                    double[] medianV3pred37 = new double[time7_1_3.length];
                    double[] centerV3pred37 = new double[time7_1_3.length];
                    for (int r = 0; r < time7_1_3.length; r++) {
                        centerV3pred7[r] = (xCenter7[0] * time7_1_3[r]) + yCenter7[0];
                        centerV3pred27[r] = (xCenter7[1] * time7_1_3[r]) + yCenter7[1];
                        centerV3pred37[r] = (xCenter7[2] * time7_1_3[r]) + yCenter7[2];
                        medianV3pred7[r] = (xMedian7[0] * time7_1_3[r]) + yMedian7[0];
                        medianV3pred27[r] = (xMedian7[1] * time7_1_3[r]) + yMedian7[1];
                        medianV3pred37[r] = (xMedian7[2] * time7_1_3[r]) + yMedian7[2];
                    }
                    double centerR2_317 = MathRC4.getR2(values7_1_3, centerV3pred7);
                    double medianR2_317 = MathRC4.getR2(values7_1_3, medianV3pred7);
                    double centerR2_327 = MathRC4.getR2(values7_1_3, centerV3pred27);
                    double medianR2_327 = MathRC4.getR2(values7_1_3, medianV3pred27);
                    double centerR2_337 = MathRC4.getR2(values7_1_3, centerV3pred37);
                    double medianR2_337 = MathRC4.getR2(values7_1_3, medianV3pred37);
                    double[] tupe = new double[]{centerR2_317, medianR2_317, centerR2_327, medianR2_327, centerR2_337, medianR2_337};
                    double max = 0;
                    int case7 = -1;
                    for (int y = 0; y < 6; y++) {
                        if (tupe[y] > max) {
                            max = tupe[y];
                            case7 = y;
                        }
                    }
                    int pl7 = 0;
                    switch (case7) {
                        case 0:
                            formulas.add(new double[]{xCenter7[0], yCenter7[0], centerR2_317, 3});
                            break;
                        case 1:
                            formulas.add(new double[]{xMedian7[0], yMedian7[0], medianR2_317, 3});
                            break;
                        case 2:
                            formulas.add(new double[]{xCenter7[1], yCenter7[1], centerR2_327, 3});
                            pl7 = 1;
                            break;
                        case 3:
                            formulas.add(new double[]{xMedian7[1], yMedian7[1], medianR2_327, 3});
                            pl7 = 1;
                            break;
                        case 4:
                            formulas.add(new double[]{xCenter7[2], yCenter7[2], centerR2_337, 3});
                            pl7 = 2;
                            break;
                        case 5:
                            formulas.add(new double[]{xMedian7[2], yMedian7[2], medianR2_337, 3});
                            pl7 = 2;
                            break;
                    }
                    int a60p7 = a7 + ((values7.length * consts[4]) / 100);
                    double[] values7_3_3 = new double[b7 - a60p7];
                    System.arraycopy(data, a60p7, values7_3_3, 0, values7_3_3.length);
                    double[] time7_3_3 = new double[values7_3_3.length];
                    System.arraycopy(time, a60p7, time7_3_3, 0, time7_3_3.length);
                    double[] medianVdpred7 = new double[time7_3_3.length];
                    double[] centerVdpred7 = new double[time7_3_3.length];
                    double[] medianVdpred27 = new double[time7_3_3.length];
                    double[] centerVdpred27 = new double[time7_3_3.length];
                    double[] medianVdpred37 = new double[time7_3_3.length];
                    double[] centerVdpred37 = new double[time7_3_3.length];
                    for (int r = 0; r < time7_3_3.length; r++) {
                        centerVdpred7[r] = (xCenter7[0] * time7_3_3[r]) + yCenter7[0];
                        medianVdpred7[r] = (xMedian7[0] * time7_3_3[r]) + yMedian7[0];
                        centerVdpred27[r] = (xCenter7[1] * time7_3_3[r]) + yCenter7[1];
                        medianVdpred27[r] = (xMedian7[1] * time7_3_3[r]) + yMedian7[1];
                        centerVdpred37[r] = (xCenter7[2] * time7_3_3[r]) + yCenter7[2];
                        medianVdpred37[r] = (xMedian7[2] * time7_3_3[r]) + yMedian7[2];
                    }
                    double centerR2_d17 = MathRC4.getR2(values7_3_3, centerVdpred7);
                    double medianR2_d17 = MathRC4.getR2(values7_3_3, medianVdpred7);
                    double centerR2_d27 = MathRC4.getR2(values7_3_3, centerVdpred27);
                    double medianR2_d27 = MathRC4.getR2(values7_3_3, medianVdpred27);
                    double centerR2_d37 = MathRC4.getR2(values7_3_3, centerVdpred37);
                    double medianR2_d37 = MathRC4.getR2(values7_3_3, medianVdpred37);
                    double[] tuped = new double[]{centerR2_d17, medianR2_d17, centerR2_d27, medianR2_d27, centerR2_d37, medianR2_d37};
                    double maxd = -100;
                    int case7d = -1;
                    for (int y = 0; y < 6; y++) {
                        if (tuped[y] > maxd) {
                            maxd = tuped[y];
                            case7d = y;
                        }
                    }
                    double[] formulaVd = new double[0];
                    /*In the end*/
                    int pld = 0;
                    switch (case7d) {
                        case 0:
                            formulaVd = new double[]{xCenter7[0], yCenter7[0], centerR2_d17, 6};
                            break;
                        case 1:
                            formulaVd = new double[]{xMedian7[0], yMedian7[0], medianR2_d17, 6};
                            break;
                        case 2:
                            formulaVd = new double[]{xCenter7[1], yCenter7[1], centerR2_d27, 6};
                            pld = 1;
                            break;
                        case 3:
                            formulaVd = new double[]{xMedian7[1], yMedian7[1], medianR2_d27, 6};
                            pld = 1;
                            break;
                        case 4:
                            formulaVd = new double[]{xCenter7[2], yCenter7[2], centerR2_d37, 6};
                            pld = 2;
                            break;
                        case 5:
                            formulaVd = new double[]{xMedian7[2], yMedian7[2], medianR2_d37, 6};
                            pld = 2;
                            break;
                    }
                    int due4state = 3 - pl7 - pld;
                    System.out.println(due4state + " " + pl7 + " " + pld);
                    int aCenter = (int) Math.round((getCross(formulas.get(formulas.size() - 1), new double[]{xCenter7[due4state], yCenter7[due4state]}) * 1000) / timeStep);
                    int aMedian = (int) Math.round((getCross(formulas.get(formulas.size() - 1), new double[]{xMedian7[due4state], yMedian7[due4state]}) * 1000) / timeStep);
                    int bCenter = (int) Math.round((getCross(new double[]{xCenter7[due4state], yCenter7[due4state]}, formulaVd) * 1000) / timeStep);
                    int bMedian = (int) Math.round((getCross(new double[]{xMedian7[due4state], yMedian7[due4state]}, formulaVd) * 1000) / timeStep);
                    double[] values7_2_3C = new double[bCenter - aCenter];
                    double[] values7_2_3M = new double[bMedian - aMedian];
                    System.arraycopy(data, aCenter, values7_2_3C, 0, values7_2_3C.length);
                    System.arraycopy(data, aMedian, values7_2_3M, 0, values7_2_3M.length);
                    double[] time7_2_3C = new double[values7_2_3C.length];
                    double[] time7_2_3M = new double[values7_2_3M.length];
                    System.arraycopy(time, aCenter, time7_2_3C, 0, time7_2_3C.length);
                    System.arraycopy(time, aMedian, time7_2_3M, 0, time7_2_3M.length);
                    double[] medianV4pred7 = new double[time7_2_3M.length];
                    double[] centerV4pred7 = new double[time7_2_3C.length];
                    for (int r = 0; r < time7_2_3C.length; r++) {
                        centerV4pred7[r] = (xCenter7[due4state] * time7_2_3C[r]) + yCenter7[due4state];
                    }
                    for (int r = 0; r < time7_2_3M.length; r++) {
                        medianV4pred7[r] = (xMedian7[due4state] * time7_2_3M[r]) + yMedian7[due4state];
                    }
                    double medianV4R2 = 0;
                    double centerV4R2 = 0;
                    try {
                        medianV4R2 = MathRC4.getR2(values7_2_3M, medianV4pred7);
                        centerV4R2 = MathRC4.getR2(values7_2_3C, centerV4pred7);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (centerV4R2 > medianV4R2) {
                        formulas.add(new double[]{xCenter7[due4state], yCenter7[due4state], centerV4R2, 4});
                    } else {
                        formulas.add(new double[]{xMedian7[due4state], yCenter7[due4state], medianV4R2, 4});
                    }
                    formulas.add(formulaVd);
                    if (additionalDiagrams) {
                        XYChart chart6 = new XYChart(600, 600);
                        XYSeries series = chart6.addSeries(res.getString("RawData") + " ", time7, values7);
                        series.setMarker(SeriesMarkers.NONE);
                        chart6.addSeries(res.getString("Center") + " 1 " + case7, time7_1_3, centerV3pred7);
                        chart6.addSeries(res.getString("Median") + " 3 1 " + case7, time7_1_3, medianV3pred7);
                        chart6.addSeries(res.getString("Center") + " 3 2 " + case7, time7_1_3, centerV3pred27);
                        chart6.addSeries(res.getString("Median") + " 3 2 " + case7, time7_1_3, medianV3pred27);
                        chart6.addSeries(res.getString("Center") + " 3 3 " + case7, time7_1_3, centerV3pred37);
                        chart6.addSeries(res.getString("Median") + " 3 3 " + case7, time7_1_3, medianV3pred37);
                        chart6.addSeries(res.getString("Center") + " d 1 " + case7d, time7_3_3, centerVdpred7);
                        chart6.addSeries(res.getString("Median") + " d 1 " + case7d, time7_3_3, medianVdpred7);
                        chart6.addSeries(res.getString("Center") + " d 2 " + case7d, time7_3_3, centerVdpred27);
                        chart6.addSeries(res.getString("Median") + " d 2 " + case7d, time7_3_3, medianVdpred27);
                        chart6.addSeries(res.getString("Center") + " d 3 " + case7d, time7_3_3, centerVdpred37);
                        chart6.addSeries(res.getString("Median") + " d 3 " + case7d, time7_3_3, medianVdpred37);
                        chart6.addSeries(res.getString("Center") + " 4", time7_2_3C, centerV4pred7);
                        chart6.addSeries(res.getString("Median") + " 4 ", time7_2_3M, medianV4pred7);
                        showChart(chart6);
                    }
                    break;
                case "9":
                    int a9 = addition.get(i) - (5000 / timeStep);
                    int b9 = addition.get(i) + (5000 / timeStep);
                    if (b9 > (time.length - 1)) {
                        b9 = time.length - 1;
                    }
                    double[] values9 = new double[b9 - a9];
                    double[] values1Pred9 = new double[b9 - a9];
                    System.arraycopy(data, a9, values9, 0, values9.length);
                    double sum9 = 0;
                    for (double aValues9 : values9) {
                        sum9 = sum9 + aValues9;
                    }
                    double highMark9 = sum9 / values9.length;
                    for (int i2 = 0; i2 < values1Pred9.length; i2++) {
                        values1Pred9[i] = highMark9;
                    }
                    formulas.add(new double[]{0.0, highMark9, MathRC4.getR2(values9, values1Pred9), 9});
                    break;
            }
        }
        modulate(formulas, (double[]) fileData[0], time, timeStep, consts);
        if (fading) {
            ArrayList<double[]> pairs = new ArrayList<>();
            for (int h = 0; h < formulas.size(); h++) {
                if (formulas.get(h)[3] == 3) {
                    int ff = h + 1;
                    if (ff < formulas.size() & formulas.get(ff)[3] == 4) {
                        double a = getCross(new double[]{formulas.get(h)[0], formulas.get(h)[1]}, new double[]{formulas.get(h - 1)[0], formulas.get(h - 1)[1]});
                        double b;
                        if (formulas.size() == ff + 1) {
                            b = data.length - 1;
                        } else {
                            b = (1000 / timeStep) * getCross(new double[]{formulas.get(ff)[0], formulas.get(ff)[1]}, new double[]{formulas.get(ff + 1)[0], formulas.get(ff + 1)[1]});
                        }
                        a = a * (1000 / timeStep);
                        pairs.add(new double[]{formulas.get(h)[0], formulas.get(h)[1], formulas.get(ff)[0], formulas.get(ff)[1], a, b});
                    }
                }
            }
            for (double[] pair : pairs) {
                double[] dataPR = new double[(int) (pair[5] - pair[4])];
                double[] timePR = new double[(int) (pair[5] - pair[4])];
                for (int ih = 0; ih < dataPR.length; ih++) {
                    dataPR[ih] = data[(int) (ih + pair[4])];
                }
                for (int ih = 0; ih < timePR.length; ih++) {
                    timePR[ih] = time[(int) (ih + pair[4])];
                }
                XYChart charta = new XYChart(600, 600);
                XYSeries series = charta.addSeries("Raw data ", timePR, dataPR);
                series.setMarker(SeriesMarkers.NONE);
                double wesa = (getCross(new double[]{pair[0], pair[1]}, new double[]{pair[2], pair[3]}) * (1000 / timeStep));
                double[] yData2 = new double[(int) (wesa - pair[4])];
                double[] timePR2 = new double[(int) (wesa - pair[4])];
                for (int y = 0; y < yData2.length; y++) {
                    yData2[y] = (pair[0] * timePR[y]) + pair[1];
                    timePR2[y] = timePR[y];
                }
                XYSeries series2 = charta.addSeries("F3 data", timePR2, yData2);
                series2.setMarker(SeriesMarkers.NONE);
                double[] yData3 = new double[(int) (pair[5] - wesa)];
                double[] timePR3 = new double[(int) (pair[5] - wesa)];
                for (int y = 0; y < yData3.length; y++) {
                    yData3[y] = (pair[2] * timePR[y + yData2.length]) + pair[3];
                    timePR3[y] = timePR[y + yData2.length];
                }
                XYSeries series3 = charta.addSeries("F4 data", timePR3, yData3);
                series3.setMarker(SeriesMarkers.NONE);
                double[] yData4 = getSMA(dataPR, 29);
                XYSeries series4 = charta.addSeries("Enlarged data", timePR, yData4);
                series4.setMarker(SeriesMarkers.NONE);
                showChart(charta);
                //fixme
                double[] minusD = new double[yData4.length];
                for (int i = 1; i < yData4.length; i++) {
                    minusD[i] = yData4[i] - yData4[i - 1];
                }
                minusD[0] = minusD[1];
                XYChart chartb = new XYChart(600, 600);
                XYSeries seriesb1 = chartb.addSeries("Raw data  ", timePR, minusD);
                seriesb1.setMarker(SeriesMarkers.NONE);
                double[] enl0 = getSMA(minusD, 25);
                XYSeries seriesb2 = chartb.addSeries("Pre-enlarged data ", timePR, enl0);
                seriesb2.setMarker(SeriesMarkers.NONE);
                double[] enl = getSMA(enl0, 25);
                XYSeries seriesb25 = chartb.addSeries("Enlarged data ", timePR, enl);
                seriesb25.setMarker(SeriesMarkers.NONE);
                double[] minA = new double[enl.length];
                double[] maxA = new double[enl.length];
                double min = stream(enl).min().getAsDouble();
                double max = stream(enl).max().getAsDouble();
                for (int i = 0; i < enl.length; i++) {
                    minA[i] = min;
                    maxA[i] = max;
                }
                XYSeries seriesb3 = chartb.addSeries("Min data ", timePR, minA);
                seriesb3.setMarker(SeriesMarkers.NONE);
                XYSeries seriesb4 = chartb.addSeries("Max data ", timePR, maxA);
                seriesb4.setMarker(SeriesMarkers.NONE);
                showChart(chartb);
            }
        }
        XYChart chart = new XYChart(600, 600);
        XYSeries series = null;
        if (preSMA) {
            XYSeries seriesS = chart.addSeries(res.getString("RawData"), time, dataPre);
            series = chart.addSeries(res.getString("RawSMAData"), time, data);
            seriesS.setMarker(SeriesMarkers.NONE);
        } else {
            series = chart.addSeries(res.getString("RawData"), time, data);
        }
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        series.setMarker(SeriesMarkers.NONE);
        double[] yData2 = new double[time.length];
        double step = timeStep / 1000.0;
        for (int y = 0; y < formulas.size(); y++) {
            double begin = 0;
            double end;
            if (y != 0) {
                begin = getCross(formulas.get(y - 1), formulas.get(y));
                double a_begin = Math.round((begin * 1000) / timeStep);
                begin = a_begin * step;
            }
            if (y == (formulas.size() - 1)) {
                end = time[time.length - 1] + 0.2;
            } else {
                end = getCross(formulas.get(y), formulas.get(y + 1)) + 0.2;
            }
            for (double a = begin; a < end; a = a + step) {
                int ch2 = (int) Math.round((a * 5.0));
                if (ch2 < yData2.length) {
                    yData2[ch2] = (formulas.get(y)[0] * a) + formulas.get(y)[1];
                }
            }
        }
        XYSeries series2 = chart.addSeries(res.getString("CalcData"), time, yData2);
        if (export) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("RC4");
            for (int i = 0; i < time.length; i++) {
                HSSFRow row = sheet.createRow(i);
                HSSFCell cell = row.createCell(0);
                cell.setCellValue(time[i]);
                cell = row.createCell(1);
                cell.setCellValue(data[i]);
                cell = row.createCell(2);
                cell.setCellValue(yData2[i]);
            }
            try {
                FileOutputStream os = new FileOutputStream(path + ".xls");
                wb.write(os);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        series2.setMarker(SeriesMarkers.NONE);
        StringBuilder finalText = new StringBuilder();
        /* Text and data formatting*/
        finalText.append("\n").append(res.getString("Results")).append("\n");
        BufferedReader br;
        int[] xt2 = new int[0];
        try {
            br = new BufferedReader(new FileReader("data.ini"));
            String line;
            xt2 = new int[4];
            int ss2 = 0;
            while ((line = br.readLine()) != null) {
                try {
                    xt2[ss2] = Integer.parseInt(line);
                } catch (Exception e3) {
                    xt2[ss2] = 0;
                }
                ss2++;
            }
        } catch (IOException ignored2) {
        }
        double timeVar;
        if (preCalibr) {
            timeVar = xt2[3] / (-1000.0);
        } else {
            double begin = 1;
            double end = 0;
            for (double[] d : formulas) {
                if (d[3] == 1.0) {
                    begin = d[1];
                }
                if (d[3] == 9.0) {
                    end = d[1];
                }
            }
            double periodD = Math.abs(begin - end) * (-1000);
            double internal = xt2[0] * xt2[1];
            timeVar = (internal / periodD);
        }
        double v2 = 1000;
        double v3 = -1;
        double tf = 0;
        double[] copyText = new double[4];
        for (int i = 0; i < formulas.size(); i++) {
            switch ((int) formulas.get(i)[3]) {
                case 2:
                    v2 = formulas.get(i)[0];
                    if (copyText[0] == 0.0) {
                        copyText[0] = v2 * timeVar;
                    }
                    finalText.append(res.getString("V2")).append(" - ").append(String.format("%.4f", v2 * timeVar)).append("\n");
                    break;
                case 3:
                    v3 = formulas.get(i)[0];
                    finalText.append(res.getString("V3")).append(" - ").append(String.format("%.4f", formulas.get(i)[0] * timeVar)).append("\n");
                    tf = getCross(formulas.get(i + 1), formulas.get(i)) - getCross(formulas.get(i - 1), formulas.get(i));
                    if (copyText[1] == 0.0) {
                        copyText[1] = v3 * timeVar;
                    }
                    if (copyText[3] == 0.0) {
                        copyText[3] = tf;
                    }
                    break;
                case 4:
                    finalText.append(res.getString("V4")).append(" - ").append(String.format("%.4f", formulas.get(i)[0] * timeVar)).append("\n");
                    finalText.append(res.getString("TimePhosph")).append(" - ").append(String.format("%.2f", tf)).append("\n");
                    finalText.append(res.getString("RcL")).append(" - ").append(String.format("%.2f", v3 / v2)).append("\n");
                    finalText.append(res.getString("RcC")).append(" - ").append(String.format("%.2f", v3 / formulas.get(i)[0])).append("\n");
                    double adpo = (xt2[2] * 1.0) / (v3 * timeVar * tf);
                    finalText.append(res.getString("AdpO")).append(" - ").append(String.format("%.2f", adpo)).append("\n");
                    if (copyText[2] == 0.0) {
                        copyText[2] = formulas.get(i)[0] * timeVar;
                    }
                    break;
                case 6:
                    finalText.append(res.getString("Vd")).append(" - ").append(String.format("%.4f", formulas.get(i)[0] * timeVar)).append("\n");
                    break;
                default:
                    break;
            }
        }
        finalText.append("\n").append(path);
        String textToClipboard = copyText[0] + "\t" + copyText[1] + "\t" + copyText[2] + "\t" + copyText[3];
        return new Object[]{chart, finalText.toString(), textToClipboard};
    }

    private static void modulate(ArrayList<double[]> formulas, double[] data, double[] time, int timeStep, int[] consts) {
        /*Preparing begin and end of line*/
        double step = timeStep / 1000.0;
        for (int y = 0; y < formulas.size(); y++) {
            // System.out.println(formulas.get(y)[0] + " " + formulas.get(y)[1] + " " + formulas.get(y)[2] + " " + formulas.get(y)[3]);
            if (formulas.get(y)[3] > 2.0) {
                if (formulas.get(y)[3] < 9.0) {
                    double begin = 0;
                    double end;
                    if (y != 0) {
                        begin = getCross(formulas.get(y - 1), formulas.get(y));
                        double a_begin = Math.round((begin * 1000) / timeStep);
                        begin = a_begin * step;
                    }
                    if (y == (formulas.size() - 1)) {
                        end = time[time.length - 1] + 0.2;
                    } else {
                        end = getCross(formulas.get(y), formulas.get(y + 1)) + 0.2;
                    }
            /* Reducing line*/
                    double width = ((end - begin) * ((100.0 - consts[5]) / 2.0)) / 100.0;
                    begin = begin + width;
                    end = end - width;
                    double[] resultedformulas = new double[3];
                    resultedformulas[1] = formulas.get(y)[1];
                    resultedformulas[2] = formulas.get(y)[2];
                    resultedformulas[0] = formulas.get(y)[0];
                    ArrayList<Double> dataF = new ArrayList<>();
                    for (double a2 = begin; a2 < end; a2 = a2 + step) {
                        int ch2 = (int) Math.round((a2 * 5.0));
                        dataF.add(data[ch2]);
                    }
                    double[] dataFR = new double[dataF.size()];
                    for (int s = 0; s < dataF.size(); s++) {
                        dataFR[s] = dataF.get(s);
                    }
                    for (double aDelta1 : delta) {
                        for (double aDelta : delta) {
                            double coefA = formulas.get(y)[0] * aDelta1;
                            double coefB = formulas.get(y)[1] * aDelta;
                            ArrayList<Double> dataC = new ArrayList<>();
                            for (double a2 = begin; a2 < end; a2 = a2 + step) {
                                dataC.add((coefA * a2) + coefB);
                            }
                            double[] dataCR = new double[dataC.size()];
                            for (int s = 0; s < dataC.size(); s++) {
                                dataCR[s] = dataC.get(s);
                            }
                            double rr = getR2(dataCR, dataFR);
                            // System.out.println("For " + aDelta1 + " a and " + aDelta + " b R2 is " + rr);
                            if (rr > resultedformulas[2]) {
                                resultedformulas[0] = coefA;
                                resultedformulas[1] = coefB;
                                resultedformulas[2] = rr;
                            }
                        }
                    }
                    // System.out.println("def fin: " + formulas.get(y)[0] + " " + formulas.get(y)[1] + " " + formulas.get(y)[2] + " " + formulas.get(y)[3]);
                    formulas.get(y)[0] = resultedformulas[0];
                    formulas.get(y)[1] = resultedformulas[1];
                    formulas.get(y)[2] = resultedformulas[2];
                    //  System.out.println("Final: " + formulas.get(y)[0] + " " + formulas.get(y)[1] + " " + formulas.get(y)[2] + " " + formulas.get(y)[3]);
                }
            }
        }
    }

    @org.jetbrains.annotations.Contract(pure = true)
    private static double getCross(double[] doubles, double[] doubles1) {
        return (doubles1[1] - doubles[1]) / (doubles[0] - doubles1[0]);
    }

    static void showChart(XYChart chart) {
        JFrame fr = new JFrame();
        XChartPanel ch = new XChartPanel<>(chart);
        fr.add(ch);
        fr.setVisible(true);
        fr.setPreferredSize(new Dimension(500, 500));
        fr.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        fr.pack();
    }

    private static double[] getSMA(double[] res, int window) {
        double[] alt = new double[res.length];
        int h = window / 2;
        for (int i = h; i < res.length - h; i++) {
            double sum = 0;
            for (int y = (i - h); y < (i + h + 1); y++) {
                sum = sum + res[y];
            }
            alt[i] = sum / window;
        }
        for (int i = 0; i < h + 1; i++) {
            double sum = 0;
            int g = 0;
            for (int y = 0; y < (i + h + 1); y++) {
                sum = sum + res[y];
                g++;
            }
            alt[i] = sum / g;
        }
        for (int i = res.length - h; i < res.length; i++) {
            double sum = 0;
            int g = 0;
            for (int y = (i - h); y < res.length; y++) {
                sum = sum + res[y];
                g++;
            }
            alt[i] = sum / g;
        }
        return alt;
    }
}

