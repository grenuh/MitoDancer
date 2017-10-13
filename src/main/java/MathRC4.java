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

class MathRC4 {

    /**
     * Small obfuscated class
     * with all info - refer to grenuh@gmail.com
     */
    static private final ResourceBundle res = ResourceBundle.getBundle("data");
    static private double[] r = new double[]{0.95, 0.98, 0.99, 0.995, 1.0, 1.005, 1.01, 1.02, 1.05};

    private static double[] a(double[] b, double[] c) {
        RealMatrix d = null;
        if (b.length != c.length) {
            throw new IllegalArgumentException(String.format("The numbers of y and x values must be equal (%d != %d)", b.length, c.length));
        }
        double[][] e = new double[b.length][];
        for (int i = 0; i < b.length; i++) {
            e[i] = b(b[i]);
        }
        OLSMultipleLinearRegression f = new OLSMultipleLinearRegression();
        f.setNoIntercept(true);
        f.newSampleData(c, e);
        try {
            d = MatrixUtils.createColumnRealMatrix(f.estimateRegressionParameters());
        } catch (Exception k) {
            k.printStackTrace();
        }
        assert d != null;
        double g = d.preMultiply(b(0))[0];
        double h = d.preMultiply(b(1))[0] - g;
        double[] j = new double[c.length];
        for (int i = 0; i < c.length; i++) {
            j[i] = g + (h * c[i]);
        }
        return new double[]{h, g, c(c, j), -1};
    }

    static private double[] b(double a) {
        double[] b = new double[2];
        double c = 1;
        for (int i = 0; i <= 1; i++) {
            b[i] = c;
            c *= a;
        }
        return b;
    }

    static private double c(double[] a, double[] b) {
        double c = 0;
        double d = 0;
        for (int i = 0; i < a.length; i++) {
            double e = (a[i] - b[i]);
            c = c + (e * e);
            d = d + a[i];
        }
        d = d / a.length;
        double f = 0;
        for (double g : a) {
            double e = (g - d);
            f = f + (e * e);
        }
        return 1 - (c / f);
    }

    /**
     * Calculate all polarographical data from used info from user
     *
     * @param aa unkn
     * @param ab unkn
     * @param ac unkn
     * @param ad unkn
     * @param ae unkn
     * @param af unkn
     * @return calculated data
     */
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    static Object[] calculate(String aa, boolean ab, boolean ac, @SuppressWarnings("unused") boolean ad, boolean ae, Object[] af) {
        int[] ag = new int[0];
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("objects.dat"));
            ag = (int[]) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        double[] ah = (double[]) af[0];
        int ai = (int) af[1];
        ArrayList<Integer> aj = (ArrayList<Integer>) af[2];
        String ak = (String) af[3];
        ArrayList<double[]> al = new ArrayList<>();
        String[] am = aa.split("");
        double[] an = new double[ah.length];
        for (int i = 0; i < an.length; i++) {
            an[i] = (double) (i * ai) / 1000.0;
        }
        for (int i = 0; i < am.length; i++) {
            switch (am[i]) {
                case "1":
                    int a = aj.get(i) - (5000 / ai);
                    if (a < 0) {
                        a = 0;
                    }
                    int b = aj.get(i);
                    double[] ao = new double[b - a];
                    double[] ap = new double[b - a];
                    System.arraycopy(ah, a, ao, 0, ao.length);
                    double aq = 0;
                    for (double ar : ao) {
                        aq = aq + ar;
                    }
                    double ar = aq / ao.length;
                    for (int i2 = 0; i2 < ap.length; i2++) {
                        ap[i] = ar;
                    }
                    al.add(new double[]{0.0, ar, MathRC4.c(ao, ap), 1});
                    double as = ((ah[aj.get(i) - 1] - ah[aj.get(i)] + 9) / (an[aj.get(i) - 1] - an[aj.get(i)]));
                    double bb = ah[aj.get(i) - 1] - (as * an[aj.get(i) - 1]);
                    double[] at = new double[]{as, bb, 1.0, 0};
                    al.add(at);
                    break;
                case "2":
                    int a2 = aj.get(i);
                    int b2;
                    try {
                        b2 = aj.get(i + 1);
                    } catch (Exception e) {
                        b2 = an.length - 1;
                    }
                    int au = (a2 + b2) / 2;
                    double[] av = new double[b2 - au];
                    System.arraycopy(ah, au, av, 0, av.length);
                    double[] aw = new double[b2 - au];
                    System.arraycopy(an, au, aw, 0, aw.length);
                    double[] ax = MathRC4.a(aw, av);
                    ax[3] = 2;
                    al.add(ax);
                    if (ab) {
                        XYChart ay = new XYChart(600, 600);
                        XYSeries az = ay.addSeries("Raw data ", aw, av);
                        az.setMarker(SeriesMarkers.NONE);
                        double[] ba = new double[aw.length];
                        for (int y = 0; y < ba.length; y++) {
                            ba[y] = (ax[0] * aw[y]) + ax[1];
                        }
                        XYSeries bc = ay.addSeries("Calc data", aw, ba);
                        bc.setMarker(SeriesMarkers.NONE);
                        if (ax[2] > 0.95) {
                            bc.setLineColor(Color.GREEN);
                        } else {
                            bc.setLineColor(Color.RED);
                        }
                        f(ay);
                    }
                    break;
                case "4":
                    int bd = 2;
                    int a4 = aj.get(i) + (ag[1] / ai);
                    int b4;
                    try {
                        b4 = aj.get(i + 1) - (ag[1] / ai);
                    } catch (Exception e) {
                        b4 = an.length - 1 - (ag[1] / ai);
                    }
                    double[] be = new double[b4 - a4];
                    System.arraycopy(ah, a4, be, 0, be.length);
                    double[] bf = new double[b4 - a4];
                    System.arraycopy(an, a4, bf, 0, bf.length);
                    ArrayList<double[]> bg = new ArrayList<>();

                    int bh = ag[0] / ai;
                    int bi = be.length - bh + 1;
                    XYChart bj = new XYChart(600, 600);
                    XYChart bk = new XYChartBuilder().width(800).height(600).build();
                    if (ab) {
                        XYSeries series = bj.addSeries(res.getString("RawData"), bf, be);
                        series.setMarker(SeriesMarkers.NONE);
                        bk.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
                        bk.getStyler().setChartTitleVisible(false);
                        bk.getStyler().setLegendPosition(Styler.LegendPosition.InsideSW);
                        bk.getStyler().setMarkerSize(16);
                    }

                    for (int e = 0; e < bi; e++) {
                        double[] bl = new double[bh];
                        System.arraycopy(be, e, bl, 0, bl.length);
                        double[] bm = new double[bh];
                        System.arraycopy(bf, e, bm, 0, bm.length);
                        double[] bn = MathRC4.a(bm, bl);
                        bg.add(bn);
                        if (ab) {
                            double[] bo = new double[bm.length];
                            for (int y = 0; y < bo.length; y++) {
                                bo[y] = (bn[0] * bm[y]) + bn[1];
                            }
                            XYSeries bp = bj.addSeries(String.valueOf(e), bm, bo);
                            bp.setMarker(SeriesMarkers.NONE);
                            bp.setLineColor(Color.GREEN);
                        }
                    }
                    DoublePoint[] bl = new DoublePoint[bg.size()];
                    for (int yyt = 0; yyt < bg.size(); yyt++) {
                        bl[yyt] = new DoublePoint(new double[]{bg.get(yyt)[0], bg.get(yyt)[1]});
                    }
                    KMeansPlusPlusClusterer<DoublePoint> bm = new KMeansPlusPlusClusterer<>(bd);
                    java.util.List<CentroidCluster<DoublePoint>> bn = bm.cluster(Arrays.asList(bl));
                    LinkedHashMap<Integer, double[]> bo = new LinkedHashMap<>();
                    for (int g = 0; g < bd; g++) {
                        bo.put(g, bn.get(g).getCenter().getPoint());
                    }
                    double[] bp = new double[bd];
                    double[] bq = new double[bd];
                    double[] br = new double[bd];
                    double[] bs = new double[bd];
                    for (int hh = 0; hh < bd; hh++) {
                        bp[hh] = bo.get(hh)[0];
                        bq[hh] = bo.get(hh)[1];
                    }
                    for (int iw = 0; iw < bn.size(); iw++) {
                        ArrayList<Double> xX = new ArrayList<>();
                        ArrayList<Double> yY = new ArrayList<>();
                        for (int k = 0; k < bn.get(iw).getPoints().size(); k++) {
                            xX.add(bn.get(iw).getPoints().get(k).getPoint()[0]);
                            yY.add(bn.get(iw).getPoints().get(k).getPoint()[1]);
                        }
                        if (ab) {
                            bk.addSeries(res.getString("Cluster") + " " + iw, xX, yY);
                        }
                        Collections.sort(xX);
                        Collections.sort(yY);
                        br[iw] = xX.get(xX.size() / 2);
                        bs[iw] = yY.get(yY.size() / 2);
                    }
                    int bt = (a4 + ((be.length * ag[2]) / 100));
                    double[] bu = new double[bt - a4];
                    System.arraycopy(ah, a4, bu, 0, bu.length);
                    double[] bv = new double[bu.length];
                    System.arraycopy(an, a4, bv, 0, bv.length);
                    double[] bw = new double[bv.length];
                    double[] bz = new double[bv.length];
                    double[] ca = new double[bv.length];
                    double[] cb = new double[bv.length];
                    for (int r = 0; r < bv.length; r++) {
                        bz[r] = (bp[0] * bv[r]) + bq[0];
                        bw[r] = (br[0] * bv[r]) + bs[0];
                        cb[r] = (bp[1] * bv[r]) + bq[1];
                        ca[r] = (br[1] * bv[r]) + bs[1];
                    }
                    double bx = MathRC4.c(bu, bz);
                    double cc = MathRC4.c(bu, bw);
                    double cd = MathRC4.c(bu, cb);
                    double by = MathRC4.c(bu, ca);
                    boolean ci = (bx + cc) > (cd + by);
                    double ce;
                    double cf;
                    if (ci) {
                        ce = bx;
                        cf = cc;
                    } else {
                        ce = cd;
                        cf = by;
                    }
                    int cg = a4 + ((be.length * ag[3]) / 100);
                    double[] ch = new double[b4 - cg];
                    System.arraycopy(ah, cg, ch, 0, ch.length);
                    double[] cj = new double[ch.length];
                    System.arraycopy(an, cg, cj, 0, cj.length);
                    double[] ck = new double[cj.length];
                    double[] cl = new double[cj.length];
                    int pl = 0;
                    if (ci) {
                        pl = 1;
                    }
                    for (int r = 0; r < cj.length; r++) {
                        cl[r] = (bp[pl] * cj[r]) + bq[pl];
                        ck[r] = (br[pl] * cj[r]) + bs[pl];
                    }
                    double cn = MathRC4.c(ch, cl);
                    double cm = MathRC4.c(ch, ck);
                    int apl = 1 - pl;
                    if (ce > cf) {
                        al.add(new double[]{bp[apl], bq[apl], ce, 3});
                    } else {
                        al.add(new double[]{br[apl], bs[apl], cf, 3});
                    }
                    if (cn > cm) {
                        al.add(new double[]{bp[pl], bq[pl], cn, 4});
                    } else {
                        al.add(new double[]{br[pl], bs[pl], cm, 4});
                    }
                    if (ab) {
                        XYChart co = new XYChart(600, 600);
                        XYSeries cp = co.addSeries(res.getString("RawData"), bf, be);
                        cp.setMarker(SeriesMarkers.NONE);
                        if (ci) {
                            co.addSeries(res.getString("Center") + " 3", bv, bz);
                            co.addSeries(res.getString("Median") + " 3", bv, bw);
                        } else {
                            co.addSeries(res.getString("Center") + " 3", bv, cb);
                            co.addSeries(res.getString("Median") + " 3", bv, ca);
                        }
                        co.addSeries(res.getString("Center") + " 4", cj, cl);
                        co.addSeries(res.getString("Median") + " 4", cj, ck);
                        f(co);
                    }
                    if (ab) {
                        f(bj);
                        XYSeries cq = bk.addSeries(res.getString("Center"), bp, bq);
                        cq.setMarkerColor(Color.RED);
                        XYSeries cr = bk.addSeries(res.getString("Median"), br, bs);
                        cr.setMarkerColor(Color.BLACK);
                        f(bk);
                    }
                    break;
                case "5":
                case "6":
                    int a5 = aj.get(i);
                    int b5;
                    try {
                        b5 = aj.get(i + 1);
                    } catch (Exception e) {
                        b5 = an.length - 1;
                    }
                    double[] cs = new double[b5 - a5];
                    System.arraycopy(ah, a5, cs, 0, cs.length);
                    double[] ct = new double[b5 - a5];
                    System.arraycopy(an, a5, ct, 0, ct.length);
                    double[] cu = MathRC4.a(ct, cs);
                    cu[3] = Integer.parseInt(am[i]);
                    al.add(cu);
                    if (ab) {
                        XYChart cv = new XYChart(600, 600);
                        XYSeries cw = cv.addSeries(res.getString("RawData"), ct, cs);
                        cw.setMarker(SeriesMarkers.NONE);
                        double[] cx = new double[ct.length];
                        for (int y = 0; y < cx.length; y++) {
                            cx[y] = (cu[0] * ct[y]) + cu[1];
                        }
                        XYSeries cy = cv.addSeries(res.getString("CalcData"), ct, cx);
                        cy.setMarker(SeriesMarkers.NONE);
                        if (cu[2] > 0.95) {
                            cy.setLineColor(Color.GREEN);
                        } else {
                            cy.setLineColor(Color.RED);
                        }
                        f(cv);
                    }
                    break;
                case "7":
                    int a7 = aj.get(i) + (ag[1] / ai);
                    int b7;
                    try {
                        b7 = aj.get(i + 1) - (ag[1] / ai);
                    } catch (Exception e) {
                        b7 = an.length - 1 - (ag[1] / ai);
                    }
                    double[] cz = new double[b7 - a7];
                    System.arraycopy(ah, a7, cz, 0, cz.length);
                    double[] da = new double[b7 - a7];
                    System.arraycopy(an, a7, da, 0, da.length);
                    ArrayList<double[]> dc = new ArrayList<>();
                    int db = ag[0] / ai;
                    int dd = cz.length - db + 1;
                    XYChart de = new XYChart(600, 600);
                    XYChart df = new XYChartBuilder().width(800).height(600).build();
                    if (ab) {
                        XYSeries series = de.addSeries(res.getString("RawData"), da, cz);
                        series.setMarker(SeriesMarkers.NONE);
                        df.getStyler().setMarkerSize(16);
                        df.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
                        df.getStyler().setChartTitleVisible(false);
                        df.getStyler().setLegendPosition(Styler.LegendPosition.InsideSW);
                    }
                    for (int e = 0; e < dd; e++) {
                        double[] dg = new double[db];
                        System.arraycopy(da, e, dg, 0, dg.length);
                        double[] dh = new double[db];
                        System.arraycopy(cz, e, dh, 0, dh.length);
                        double[] di = MathRC4.a(dg, dh);
                        dc.add(di);
                        if (ab) {
                            double[] dj = new double[dg.length];
                            for (int y = 0; y < dj.length; y++) {
                                dj[y] = (di[0] * dg[y]) + di[1];
                            }
                            XYSeries dk = de.addSeries(String.valueOf(e), dg, dj);
                            dk.setLineColor(Color.GREEN);
                            dk.setMarker(SeriesMarkers.NONE);
                        }
                    }
                    DoublePoint[] dl = new DoublePoint[dc.size()];
                    for (int dm = 0; dm < dc.size(); dm++) {
                        dl[dm] = new DoublePoint(new double[]{dc.get(dm)[0], dc.get(dm)[1]});
                    }
                    KMeansPlusPlusClusterer<DoublePoint> dn = new KMeansPlusPlusClusterer<>(3);
                    java.util.List<CentroidCluster<DoublePoint>> dq = dn.cluster(Arrays.asList(dl));
                    LinkedHashMap<Integer, double[]> dp = new LinkedHashMap<>();
                    for (int g = 0; g < 3; g++) {
                        dp.put(g, dq.get(g).getCenter().getPoint());
                    }
                    double[] dr = new double[3];
                    double[] dt = new double[3];
                    double[] du = new double[3];
                    double[] dv = new double[3];
                    for (int hh = 0; hh < 3; hh++) {
                        dr[hh] = dp.get(hh)[0];
                        dt[hh] = dp.get(hh)[1];
                    }
                    for (int iw = 0; iw < dq.size(); iw++) {
                        ArrayList<Double> xX;
                        ArrayList<Double> yY;
                        xX = new ArrayList<>();
                        yY = new ArrayList<>();
                        for (int k = 0; k < dq.get(iw).getPoints().size(); k++) {
                            xX.add(dq.get(iw).getPoints().get(k).getPoint()[0]);
                            yY.add(dq.get(iw).getPoints().get(k).getPoint()[1]);
                        }
                        if (ab) {
                            df.addSeries(res.getString("Cluster") + " " + iw, xX, yY);
                        }
                        Collections.sort(xX);
                        Collections.sort(yY);
                        du[iw] = xX.get(xX.size() / 2);
                        dv[iw] = yY.get(yY.size() / 2);
                    }
                    double[] dw = new double[(a7 + ((cz.length * ag[2]) / 100)) - a7];
                    System.arraycopy(ah, a7, dw, 0, dw.length);
                    double[] dx = new double[dw.length];
                    System.arraycopy(an, a7, dx, 0, dx.length);
                    double[] ea = new double[dx.length];
                    double[] ec = new double[dx.length];
                    double[] dy = new double[dx.length];
                    double[] eb = new double[dx.length];
                    double[] dz = new double[dx.length];
                    double[] ed = new double[dx.length];
                    for (int r = 0; r < dx.length; r++) {
                        ec[r] = (dr[0] * dx[r]) + dt[0];
                        eb[r] = (dr[1] * dx[r]) + dt[1];
                        ed[r] = (dr[2] * dx[r]) + dt[2];
                        ea[r] = (du[0] * dx[r]) + dv[0];
                        dy[r] = (du[1] * dx[r]) + dv[1];
                        dz[r] = (du[2] * dx[r]) + dv[2];
                    }
                    double ee = MathRC4.c(dw, ec);
                    double eh = MathRC4.c(dw, ea);
                    double ef = MathRC4.c(dw, eb);
                    double ei = MathRC4.c(dw, dy);
                    double ej = MathRC4.c(dw, ed);
                    double eg = MathRC4.c(dw, dz);
                    double[] ek = new double[]{ee, eh, ef, ei, ej, eg};
                    double el = 0;
                    int em = -1;
                    for (int y = 0; y < 6; y++) {
                        if (ek[y] > el) {
                            el = ek[y];
                            em = y;
                        }
                    }
                    int pl7 = 0;
                    switch (em) {
                        case 0:
                            al.add(new double[]{dr[0], dt[0], ee, 3});
                            break;
                        case 1:
                            al.add(new double[]{du[0], dv[0], eh, 3});
                            break;
                        case 2:
                            al.add(new double[]{dr[1], dt[1], ef, 3});
                            pl7 = 1;
                            break;
                        case 3:
                            al.add(new double[]{du[1], dv[1], ei, 3});
                            pl7 = 1;
                            break;
                        case 4:
                            al.add(new double[]{dr[2], dt[2], ej, 3});
                            pl7 = 2;
                            break;
                        case 5:
                            al.add(new double[]{du[2], dv[2], eg, 3});
                            pl7 = 2;
                            break;
                    }
                    int en = a7 + ((cz.length * ag[4]) / 100);
                    double[] eo = new double[b7 - en];
                    System.arraycopy(ah, en, eo, 0, eo.length);
                    double[] ep = new double[eo.length];
                    System.arraycopy(an, en, ep, 0, ep.length);
                    double[] er = new double[ep.length];
                    double[] ew = new double[ep.length];
                    double[] eq = new double[ep.length];
                    double[] ey = new double[ep.length];
                    double[] es = new double[ep.length];
                    double[] fa = new double[ep.length];
                    for (int r = 0; r < ep.length; r++) {
                        ew[r] = (dr[0] * ep[r]) + dt[0];
                        er[r] = (du[0] * ep[r]) + dv[0];
                        ey[r] = (dr[1] * ep[r]) + dt[1];
                        eq[r] = (du[1] * ep[r]) + dv[1];
                        fa[r] = (dr[2] * ep[r]) + dt[2];
                        es[r] = (du[2] * ep[r]) + dv[2];
                    }
                    double fb = MathRC4.c(eo, ew);
                    double et = MathRC4.c(eo, er);
                    double ez = MathRC4.c(eo, ey);
                    double eu = MathRC4.c(eo, eq);
                    double ex = MathRC4.c(eo, fa);
                    double ev = MathRC4.c(eo, es);
                    double[] fc = new double[]{fb, et, ez, eu, ex, ev};
                    double fd = -100;
                    int fe = -1;
                    for (int y = 0; y < 6; y++) {
                        if (fc[y] > fd) {
                            fd = fc[y];
                            fe = y;
                        }
                    }
                    double[] ff = new double[0];
                    int fg = 0;
                    switch (fe) {
                        case 0:
                            ff = new double[]{dr[0], dt[0], fb, 6};
                            break;
                        case 1:
                            ff = new double[]{du[0], dv[0], et, 6};
                            break;
                        case 2:
                            ff = new double[]{dr[1], dt[1], ez, 6};
                            fg = 1;
                            break;
                        case 3:
                            ff = new double[]{du[1], dv[1], eu, 6};
                            fg = 1;
                            break;
                        case 4:
                            ff = new double[]{dr[2], dt[2], ex, 6};
                            fg = 2;
                            break;
                        case 5:
                            ff = new double[]{du[2], dv[2], ev, 6};
                            fg = 2;
                            break;
                    }
                    int fh = 3 - pl7 - fg;
                    System.out.println(fh + " " + pl7 + " " + fg);
                    int ds = (int) Math.round((e(al.get(al.size() - 1), new double[]{dr[fh], dt[fh]}) * 1000) / ai);
                    int fi = (int) Math.round((e(al.get(al.size() - 1), new double[]{du[fh], dv[fh]}) * 1000) / ai);
                    int fj = (int) Math.round((e(new double[]{dr[fh], dt[fh]}, ff) * 1000) / ai);
                    int fk = (int) Math.round((e(new double[]{du[fh], dv[fh]}, ff) * 1000) / ai);
                    double[] fl = new double[fj - ds];
                    double[] fm = new double[fk - fi];
                    System.arraycopy(ah, ds, fl, 0, fl.length);
                    System.arraycopy(ah, fi, fm, 0, fm.length);
                    double[] fn = new double[fl.length];
                    double[] fo = new double[fm.length];
                    System.arraycopy(an, ds, fn, 0, fn.length);
                    System.arraycopy(an, fi, fo, 0, fo.length);
                    double[] fp = new double[fo.length];
                    double[] fq = new double[fn.length];
                    for (int r = 0; r < fn.length; r++) {
                        fq[r] = (dr[fh] * fn[r]) + dt[fh];
                    }
                    for (int r = 0; r < fo.length; r++) {
                        fp[r] = (du[fh] * fo[r]) + dv[fh];
                    }
                    double fr = 0;
                    double fs = 0;
                    try {
                        fr = MathRC4.c(fm, fp);
                        fs = MathRC4.c(fl, fq);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fs > fr) {
                        al.add(new double[]{dr[fh], dt[fh], fs, 4});
                    } else {
                        al.add(new double[]{du[fh], dt[fh], fr, 4});
                    }
                    al.add(ff);
                    if (ab) {
                        XYChart chart6 = new XYChart(600, 600);
                        XYSeries series = chart6.addSeries(res.getString("RawData") + " ", da, cz);
                        series.setMarker(SeriesMarkers.NONE);
                        chart6.addSeries(res.getString("Center") + " 1 " + em, dx, ec);
                        chart6.addSeries(res.getString("Median") + " 3 1 " + em, dx, ea);
                        chart6.addSeries(res.getString("Center") + " 3 2 " + em, dx, eb);
                        chart6.addSeries(res.getString("Median") + " 3 2 " + em, dx, dy);
                        chart6.addSeries(res.getString("Center") + " 3 3 " + em, dx, ed);
                        chart6.addSeries(res.getString("Median") + " 3 3 " + em, dx, dz);
                        chart6.addSeries(res.getString("Center") + " d 1 " + fe, ep, ew);
                        chart6.addSeries(res.getString("Median") + " d 1 " + fe, ep, er);
                        chart6.addSeries(res.getString("Center") + " d 2 " + fe, ep, ey);
                        chart6.addSeries(res.getString("Median") + " d 2 " + fe, ep, eq);
                        chart6.addSeries(res.getString("Center") + " d 3 " + fe, ep, fa);
                        chart6.addSeries(res.getString("Median") + " d 3 " + fe, ep, es);
                        chart6.addSeries(res.getString("Center") + " 4", fn, fq);
                        chart6.addSeries(res.getString("Median") + " 4 ", fo, fp);
                        f(chart6);
                    }
                    break;
                case "9":
                    int a9 = aj.get(i) - (5000 / ai);
                    int b9 = aj.get(i) + (5000 / ai);
                    if (b9 > (an.length - 1)) {
                        b9 = an.length - 1;
                    }
                    double[] ft = new double[b9 - a9];
                    double[] fu = new double[b9 - a9];
                    System.arraycopy(ah, a9, ft, 0, ft.length);
                    double fv = 0;
                    for (double fx : ft) {
                        fv = fv + fx;
                    }
                    double fw = fv / ft.length;
                    for (int i2 = 0; i2 < fu.length; i2++) {
                        fu[i] = fw;
                    }
                    al.add(new double[]{0.0, fw, MathRC4.c(ft, fu), 9});
                    break;
            }
        }

        d(al, (double[]) af[0], an, ai, ag);

        XYChart fx = new XYChart(600, 600);
        XYSeries fy = fx.addSeries(res.getString("RawData"), an, ah);
        fx.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        fy.setMarker(SeriesMarkers.NONE);
        double[] fz = new double[an.length];
        double ga = ai / 1000.0;
        for (int y = 0; y < al.size(); y++) {
            double gb = 0;
            double gc;
            if (y != 0) {
                gb = e(al.get(y - 1), al.get(y));
                double gd = Math.round((gb * 1000) / ai);
                gb = gd * ga;
            }
            if (y == (al.size() - 1)) {
                gc = an[an.length - 1] + 0.2;
            } else {
                gc = e(al.get(y), al.get(y + 1)) + 0.2;
            }
            for (double a = gb; a < gc; a = a + ga) {
                int gd = (int) Math.round((a * 5.0));
                if (gd < fz.length) {
                    fz[gd] = (al.get(y)[0] * a) + al.get(y)[1];
                }
            }
        }
        XYSeries ge = fx.addSeries(res.getString("CalcData"), an, fz);
        if (ae) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet gf = wb.createSheet("RC4");
            for (int i = 0; i < an.length; i++) {
                HSSFRow gg = gf.createRow(i);
                HSSFCell gh = gg.createCell(0);
                gh.setCellValue(an[i]);
                gh = gg.createCell(1);
                gh.setCellValue(ah[i]);
                gh = gg.createCell(2);
                gh.setCellValue(fz[i]);
            }
            try {
                FileOutputStream os = new FileOutputStream(ak + ".xls");
                wb.write(os);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ge.setMarker(SeriesMarkers.NONE);
        StringBuilder gi = new StringBuilder();
        gi.append("\n").append(res.getString("Results")).append("\n");
        BufferedReader gj;
        int[] gl = new int[0];
        try {
            gj = new BufferedReader(new FileReader("data.ini"));
            String gk;
            gl = new int[4];
            int gm = 0;
            while ((gk = gj.readLine()) != null) {
                try {
                    gl[gm] = Integer.parseInt(gk);
                } catch (Exception e3) {
                    gl[gm] = 0;
                }
                gm++;
            }
        } catch (IOException ignored2) {
        }
        double gn;
        if (ac) {
            gn = gl[3] / (-1000.0);
        } else {
            double go = 1;
            double gp = 0;
            for (double[] d : al) {
                if (d[3] == 1.0) {
                    go = d[1];
                }
                if (d[3] == 9.0) {
                    gp = d[1];
                }
            }

            gn = ((gl[0] * gl[1]) / (Math.abs(go - gp) * (-1000)));
        }
        double v2 = 1000;
        double v3 = -1;
        double tf = 0;
        double[] gq = new double[4];
        for (int i = 0; i < al.size(); i++) {
            switch ((int) al.get(i)[3]) {
                case 2:
                    v2 = al.get(i)[0];
                    if (gq[0] == 0.0) {
                        gq[0] = v2 * gn;
                    }
                    gi.append(res.getString("V2")).append(" - ").append(String.format("%.4f", v2 * gn)).append("\n");
                    break;
                case 3:
                    v3 = al.get(i)[0];
                    gi.append(res.getString("V3")).append(" - ").append(String.format("%.4f", al.get(i)[0] * gn)).append("\n");
                    tf = e(al.get(i + 1), al.get(i)) - e(al.get(i - 1), al.get(i));
                    if (gq[1] == 0.0) {
                        gq[1] = v3 * gn;
                    }
                    if (gq[3] == 0.0) {
                        gq[3] = tf;
                    }
                    break;
                case 4:
                    gi.append(res.getString("V4")).append(" - ").append(String.format("%.4f", al.get(i)[0] * gn)).append("\n");
                    gi.append(res.getString("TimePhosph")).append(" - ").append(String.format("%.2f", tf)).append("\n");
                    gi.append(res.getString("RcL")).append(" - ").append(String.format("%.2f", v3 / v2)).append("\n");
                    gi.append(res.getString("RcC")).append(" - ").append(String.format("%.2f", v3 / al.get(i)[0])).append("\n");
                    double gs = (gl[2] * 1.0) / (v3 * gn * tf);
                    gi.append(res.getString("AdpO")).append(" - ").append(String.format("%.2f", gs)).append("\n");
                    if (gq[2] == 0.0) {
                        gq[2] = al.get(i)[0] * gn;
                    }
                    break;
                case 6:
                    gi.append(res.getString("Vd")).append(" - ").append(String.format("%.4f", al.get(i)[0] * gn)).append("\n");
                    break;
                default:
                    break;
            }
        }
        gi.append("\n").append(ak);
        String gr = gq[0] + "\t" + gq[1] + "\t" + gq[2] + "\t" + gq[3];
        return new Object[]{fx, gi.toString(), gr};
    }

    private static void d(ArrayList<double[]> a, double[] b, double[] c, int d, int[] e) {
        double f = d / 1000.0;
        for (int y = 0; y < a.size(); y++) {
            if (a.get(y)[3] > 2.0) {
                if (a.get(y)[3] < 9.0) {
                    double g = 0;
                    double h;
                    if (y != 0) {
                        g = e(a.get(y - 1), a.get(y));
                        double a_begin = Math.round((g * 1000) / d);
                        g = a_begin * f;
                    }
                    if (y == (a.size() - 1)) {
                        h = c[c.length - 1] + 0.2;
                    } else {
                        h = e(a.get(y), a.get(y + 1)) + 0.2;
                    }
                    double j = ((h - g) * ((100.0 - e[5]) / 2.0)) / 100.0;
                    g = g + j;
                    h = h - j;
                    double[] k = new double[3];
                    k[1] = a.get(y)[1];
                    k[2] = a.get(y)[2];
                    k[0] = a.get(y)[0];
                    ArrayList<Double> m = new ArrayList<>();
                    for (double n = g; n < h; n = n + f) {
                        int ch2 = (int) Math.round((n * 5.0));
                        m.add(b[ch2]);
                    }
                    double[] o = new double[m.size()];
                    for (int s = 0; s < m.size(); s++) {
                        o[s] = m.get(s);
                    }
                    for (double p : r) {
                        for (double t : r) {
                            double q = a.get(y)[0] * p;
                            double u = a.get(y)[1] * t;
                            ArrayList<Double> v = new ArrayList<>();
                            for (double w = g; w < h; w = w + f) {
                                v.add((q * w) + u);
                            }
                            double[] l = new double[v.size()];
                            for (int s = 0; s < v.size(); s++) {
                                l[s] = v.get(s);
                            }
                            double x = c(l, o);
                            if (x > k[2]) {
                                k[0] = q;
                                k[1] = u;
                                k[2] = x;
                            }
                        }
                    }
                    a.get(y)[0] = k[0];
                    a.get(y)[1] = k[1];
                    a.get(y)[2] = k[2];
                }
            }
        }
    }

    @org.jetbrains.annotations.Contract(pure = true)
    private static double e(double[] a, double[] b) {
        return (b[1] - a[1]) / (a[0] - b[0]);
    }

    static void f(XYChart a) {
        JFrame b = new JFrame();
        XChartPanel c = new XChartPanel<>(a);
        b.add(c);
        b.setVisible(true);
        b.setPreferredSize(new Dimension(500, 500));
        b.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        b.pack();
    }
}

