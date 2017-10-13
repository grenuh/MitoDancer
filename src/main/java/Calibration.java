import org.knowm.xchart.XChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Dialog class
 * Used for establish calibration data
 */
class Calibration extends JFrame {
    private OxyFile oFile;
    private JCheckBox[] chboxes;

    /**
     * Simple constructor
     *
     * @param selectedFile file to reading
     */
    Calibration(OxyFile selectedFile) {
        oFile = selectedFile;
        ResourceBundle res = ResourceBundle.getBundle("data");
        setTitle(res.getString("Calibration"));
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(6, 6, 6, 6);
        JLabel lb1 = new JLabel(res.getString("SelectCalibr"));
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        p.add(lb1, cs);
        JPanel chboxesPanel = new JPanel();
        chboxes = new JCheckBox[oFile.getAdditionSize()];
        for (int i = 0; i < oFile.getAdditionSize(); i++) {
            int gg = i + 1;
            JCheckBox chb = new JCheckBox(String.valueOf(gg));
            chboxes[i] = chb;
            chboxesPanel.add(chb);
        }
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        p.add(chboxesPanel, cs);
        JButton jb1 = new JButton(res.getString("OK"));
        jb1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Integer> calibr = new ArrayList<>();
                for (int i = 0; i < chboxes.length; i++) {
                    if (chboxes[i].isSelected()) {
                        calibr.add(i);
                    }
                }
                BufferedReader br;
                try {
                    br = new BufferedReader(new FileReader("data.ini"));
                    String line;
                    int[] xt2 = new int[4];
                    int ss2 = 0;
                    while ((line = br.readLine()) != null) {
                        try {
                            xt2[ss2] = Integer.parseInt(line);
                        } catch (Exception e3) {
                            xt2[ss2] = 0;
                        }
                        ss2++;
                    }
                    oFile.getMagicNumber(calibr, xt2);
                    FileWriter fw = new FileWriter("data.ini");
                    fw.write(Integer.toString(xt2[0]));
                    fw.write(System.lineSeparator());
                    fw.write(Integer.toString(xt2[1]));
                    fw.write(System.lineSeparator());
                    fw.write(Integer.toString(xt2[2]));
                    fw.write(System.lineSeparator());
                    fw.write(Integer.toString(xt2[3]));
                    fw.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                dispose();
                new SecondWindow(oFile);
                dispose();
            }
        });
        cs.gridx = 0;
        cs.gridy = 2;
        cs.gridwidth = 1;
        p.add(jb1, cs);
        p.setPreferredSize(new Dimension(250, 400));
        setLayout(null);
        p.setBounds(0, 0, 250, 400);
        add(p);
        XChartPanel ch = oFile.getChart();
        ch.setBounds(250, 0, 600, 600);
        add(ch);
        setPreferredSize(new Dimension(900, 700));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }
}