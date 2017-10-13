import org.knowm.xchart.XChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Dialog class
 * Used for change actions data
 */
class Rupture extends JFrame {
    private OxyFile oFile;
    private JCheckBox[] chboxes;

    /**
     * Simple constructor
     *
     * @param selectedFile file to updating
     */
    Rupture(OxyFile selectedFile) {
        final ResourceBundle res = ResourceBundle.getBundle("data");
        oFile = selectedFile;
        setTitle(res.getString("Rupture"));
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(6, 6, 6, 6);
        JLabel lb1 = new JLabel(res.getString("MarkerDel"));
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
        JLabel lb12 = new JLabel(res.getString("MarkerAdd"));
        cs.gridx = 0;
        cs.gridy = 2;
        cs.gridwidth = 1;
        p.add(lb12, cs);
        final JTextField jt1 = new JTextField();
        cs.gridx = 0;
        cs.gridy = 3;
        cs.gridwidth = 1;
        p.add(jt1, cs);
        JButton jb1 = new JButton(res.getString("OK"));
        jb1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Integer> removedAddition = new ArrayList<>();
                for (int i = 0; i < chboxes.length; i++) {
                    if (chboxes[i].isSelected()) {
                        removedAddition.add(i);
                    }
                }
                try {
                    int newAdd = 0;
                    try {
                        newAdd = Integer.parseInt(jt1.getText());
                    } catch (NumberFormatException e1) {
                        e1.printStackTrace();
                    }
                    oFile.modify(removedAddition, newAdd);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                new SecondWindow(oFile);
                dispose();
            }
        });
        cs.gridx = 0;
        cs.gridy = 4;
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