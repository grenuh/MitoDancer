import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * Example class (based on real equipment)
 */
class Record4File implements OxyFile {
    final private ResourceBundle res = ResourceBundle.getBundle("data");
    private String head;
    private Channel[] channels = new Channel[4];
    private ArrayList<Integer> addition = new ArrayList<>();
    private int mksStep;
    private String pathFile;
    private String pathName;
    private String modified;

    /**
     * Constructor
     *
     * @param rcFile file with data
     */
    Record4File(File rcFile) {
        pathFile = rcFile.getPath().substring(0, (rcFile.getPath().length() - rcFile.getName().length()));
        pathName = rcFile.getName().substring(0, (rcFile.getName().length() - 4));
        ArrayList<String> entries = new ArrayList<>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(rcFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert scanner != null;
        while (scanner.hasNext()) {
            entries.add(scanner.nextLine());
        }
        channels[0] = new Channel(entries.get(1));
        channels[1] = new Channel(entries.get(2));
        channels[2] = new Channel(entries.get(3));
        channels[3] = new Channel(entries.get(4));
        head = entries.get(5);
        String tmpD[] = entries.get(6).split("\t");
        ArrayList<ArrayList<Double>> tempList = new ArrayList<>();
        for (int i = 2; i < tmpD.length; i++) {
            tempList.add(new ArrayList<Double>());
        }
        int checkAddition = 0;
        String mks1[] = entries.get(6).replace(",", ".").split("\t");
        String mks2[] = entries.get(7).replace(",", ".").split("\t");
        mksStep = (int) ((Double.valueOf(mks2[0]) - Double.valueOf(mks1[0])) * 1000);
        for (int i = 6; i < entries.size(); i++) {
            String tmpData[] = entries.get(i).replace(",", ".").split("\t");
            int th = Integer.parseInt(tmpData[tmpData.length - 1]);
            if (th > checkAddition) {
                checkAddition++;
                addition.add(i - 7);
            }
            switch (tmpData.length) {
                case 3:
                    tempList.get(0).add(Double.valueOf(tmpData[1]));
                    break;
                case 4:
                    tempList.get(0).add(Double.valueOf(tmpData[1]));
                    tempList.get(1).add(Double.valueOf(tmpData[2]));
                    break;
                case 5:
                    tempList.get(0).add(Double.valueOf(tmpData[1]));
                    tempList.get(1).add(Double.valueOf(tmpData[2]));
                    tempList.get(2).add(Double.valueOf(tmpData[3]));
                    break;
                case 6:
                    tempList.get(0).add(Double.valueOf(tmpData[1]));
                    tempList.get(1).add(Double.valueOf(tmpData[2]));
                    tempList.get(2).add(Double.valueOf(tmpData[3]));
                    tempList.get(3).add(Double.valueOf(tmpData[4]));
                    break;
            }
        }
        int t = 0;
        if (channels[0].active) {
            channels[0].data = new double[tempList.get(t).size()];
            for (int i = 0; i < channels[0].data.length; i++) {
                channels[0].data[i] = tempList.get(t).get(i);
            }
            t++;
        }
        if (channels[1].active) {
            channels[1].data = new double[tempList.get(t).size()];
            for (int i = 0; i < channels[1].data.length; i++) {
                channels[1].data[i] = tempList.get(t).get(i);
            }
            t++;
        }
        if (channels[2].active) {
            channels[2].data = new double[tempList.get(t).size()];
            for (int i = 0; i < channels[2].data.length; i++) {
                channels[2].data[i] = tempList.get(t).get(i);
            }
            t++;
        }
        if (channels[3].active) {
            channels[3].data = new double[tempList.get(t).size()];
            for (int i = 0; i < channels[3].data.length; i++) {
                channels[3].data[i] = tempList.get(t).get(i);
            }
        }
        modified = "";
    }

    private String toStringFile() {
        StringBuilder sb = new StringBuilder();
        String NAME = "Record4";
        sb.append(NAME).append("\r\n");
        sb.append(channels[0]).append(channels[1]);
        sb.append(channels[2]).append(channels[3]);
        sb.append(head).append("\r\n");
        int length = 0;
        if (channels[0].active) {
            length = channels[0].data.length;
        }
        if (channels[1].active) {
            length = channels[1].data.length;
        }
        if (channels[2].active) {
            length = channels[2].data.length;
        }
        if (channels[3].active) {
            length = channels[3].data.length;
        }
        double adTime = (double) mksStep / 1000f;
        if (length > 0) {
            int add = 0;
            int pos = 0;
            double time = 0f;
            for (int i = 0; i < length; i++) {
                sb.append(String.format("%.1f", time)).append("\t");
                if (channels[0].active) {
                    sb.append(String.format("%." + channels[0].accuracy + "f", channels[0].data[i])).append("\t");
                }
                if (channels[1].active) {
                    sb.append(String.format("%." + channels[1].accuracy + "f", channels[1].data[i])).append("\t");
                }
                if (channels[2].active) {
                    sb.append(String.format("%." + channels[2].accuracy + "f", channels[2].data[i])).append("\t");
                }
                if (channels[3].active) {
                    sb.append(String.format("%." + channels[3].accuracy + "f", channels[3].data[i])).append("\t");
                }
                if (pos < addition.size()) {
                    if (addition.get(pos) == i) {
                        add++;
                        pos++;
                    }
                }
                sb.append(add).append("\r\n");
                time = time + adTime;
            }
        }
        return sb.toString();
    }

    public XChartPanel<XYChart> getChart() {
        double[] xData = new double[0];
        for (int i = 0; i < 4; i++) {
            if (channels[i].active) {
                xData = new double[channels[i].data.length];
            }
        }
        for (int i = 0; i < xData.length; i++) {
            xData[i] = ((double) (i * mksStep)) / 1000.0;
        }
        XYChart chart = new XYChart(600, 600);
        double[] xData2 = new double[0];
        double[] yData2 = new double[0];
        for (int i = 0; i < 4; i++) {
            if (channels[i].active) {
                XYSeries series = chart.addSeries(res.getString("Channel") + " " + i, xData, channels[i].data);
                series.setMarker(SeriesMarkers.NONE);
                xData2 = new double[addition.size()];
                yData2 = new double[addition.size()];
                for (int y = 0; y < addition.size(); y++) {
                    yData2[y] = channels[i].data[addition.get(y)];
                    xData2[y] = (addition.get(y) * 200.0) / 1000.0;
                }
            }
        }
        chart.getStyler().setLegendVisible(false);
        if (xData2.length > 0) {
            XYSeries series = chart.addSeries(res.getString("Addition"), xData2, yData2);
            series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
            series.setMarker(SeriesMarkers.SQUARE);
            series.setMarkerColor(Color.RED);
        }
        return new XChartPanel<>(chart);
    }

    public int getAdditionSize() {
        return addition.size();
    }

    public void modify(ArrayList<Integer> removedAddition, int newAdd) throws IOException {
        if (removedAddition.size() > 0) {
            for (int i = removedAddition.size() - 1; i > -1; i--) {
                addition.remove(addition.get(removedAddition.get(i)));
            }
        }
        if (newAdd > -1) {
            addition.add(newAdd);
        }
        Collections.sort(addition);
        modified = String.valueOf(System.currentTimeMillis());
        FileWriter wrt2 = new FileWriter(pathFile + pathName + "." + modified + ".rc4");
        wrt2.append(toStringFile());
        wrt2.flush();
        wrt2.close();

    }

    public String getPath() {
        return pathFile + pathName + modified + ".rc4";
    }

    public void getMagicNumber(ArrayList<Integer> calibr, int[] xt2) {
        double begin;
        double end;
        double[] time = new double[channels[3].data.length];
        for (int i = 0; i < time.length; i++) {
            time[i] = (double) (i * mksStep) / 1000.0;
        }
        int a = addition.get(calibr.get(0)) - (5000 / mksStep);
        if (a < 0) {
            a = 0;
        }
        int b = addition.get(calibr.get(0));
        double[] values1 = new double[b - a];
        System.arraycopy(channels[3].data, a, values1, 0, values1.length);
        double sum = 0;
        for (double aValues1 : values1) {
            sum = sum + aValues1;
        }
        begin = sum / values1.length;
        int a9 = addition.get(calibr.get(1)) - (5000 / mksStep);
        int b9 = addition.get(calibr.get(1)) + (5000 / mksStep);
        if (b9 > (time.length - 1)) {
            b9 = time.length - 1;
        }
        double[] values9 = new double[b9 - a9];
        System.arraycopy(channels[3].data, a9, values9, 0, values9.length);
        double sum9 = 0;
        for (double aValues9 : values9) {
            sum9 = sum9 + aValues9;
        }
        end = sum9 / values9.length;
        double periodD = Math.abs(begin - end) * (-1000);
        double internal = xt2[0] * xt2[1];
        double timeVar = (internal / periodD);
        xt2[3] = (int) (timeVar * (-1000));

    }

    public Object[] getData() {
        return new Object[]{channels[3].data, mksStep, addition, getPath()};
    }
}

/**
 * Helper class
 * Depend only for internal structure of file
 */
class Channel {
    boolean active;
    int accuracy;
    double[] data;
    private int int1;
    private String name;
    private String coord;
    private int und;
    private int mkum;
    private double up;
    private double down;

    Channel(String str) {
        String[] data = str.replace(",", ".").split("\t");
        active = data[0].equals("TRUE");
        int1 = Integer.parseInt(data[1]);
        name = data[2];
        coord = data[3];
        accuracy = Integer.parseInt(data[4]);
        und = Integer.parseInt(data[5]);
        mkum = Integer.parseInt(data[6]);
        up = Double.parseDouble(data[7]);
        down = Double.parseDouble(data[8]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (active) {
            sb.append("TRUE");
        } else {
            sb.append("FALSE");
        }
        sb.append("\t").append(int1).append("\t").append(name);
        sb.append("\t").append(coord).append("\t").append(accuracy);
        sb.append("\t").append(und).append("\t").append(mkum);
        if (active) {
            sb.append("\t").append(up);
            sb.append("\t").append(down).append("\r\n");
        } else {
            sb.append("\t0\t1\r\n");
        }
        int index = sb.indexOf(".");
        while (index != -1) {
            sb.replace(index, index + 1, ",");
            index += 1;
            index = sb.indexOf(".", index);
        }
        return sb.toString();
    }
}