import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ResourceBundle;

/**
 * Dialog class
 * Used for establish experiment data
 */
class Adjustment extends JDialog {
    /**
     * Simple constructor
     */
    Adjustment() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(12, 12, 12, 12);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("data.ini"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        int[] xt = new int[4];
        int ss2 = 0;
        try {
            assert br != null;
            while ((line = br.readLine()) != null) {
                try {
                    xt[ss2] = Integer.parseInt(line);
                } catch (Exception e) {
                    xt[ss2] = 0;
                }
                ss2++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResourceBundle res = ResourceBundle.getBundle("data");
        JLabel lb = new JLabel(res.getString("Oxygen"));

        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        p.add(lb, cs);
        final JTextField jt1 = new JTextField(Integer.toString(xt[0]));

        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        p.add(jt1, cs);
        lb = new JLabel(res.getString("Volume"));

        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        p.add(lb, cs);
        final JTextField jt2 = new JTextField(Integer.toString(xt[1]));
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        p.add(jt2, cs);
        lb = new JLabel(res.getString("ADP"));
        cs.gridx = 0;
        cs.gridy = 2;
        cs.gridwidth = 1;
        p.add(lb, cs);
        final JTextField jt3 = new JTextField(Integer.toString(xt[2]));
        cs.gridx = 1;
        cs.gridy = 2;
        cs.gridwidth = 2;
        p.add(jt3, cs);
        lb = new JLabel(res.getString("Variable"));
        cs.gridx = 0;
        cs.gridy = 3;
        cs.gridwidth = 1;
        p.add(lb, cs);
        final JTextField jt4 = new JTextField(Integer.toString(xt[3]));
        cs.gridx = 1;
        cs.gridy = 3;
        cs.gridwidth = 2;
        p.add(jt4, cs);
        JButton btnOk = new JButton(res.getString("OK"));
        btnOk.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e2) {
                int[] xt2 = new int[4];
                try {
                    xt2[0] = Integer.parseInt(jt1.getText());
                } catch (Exception e) {
                    xt2[0] = 0;
                }
                try {
                    xt2[1] = Integer.parseInt(jt2.getText());
                } catch (Exception e) {
                    xt2[1] = 0;
                }
                try {
                    xt2[2] = Integer.parseInt(jt3.getText());
                } catch (Exception e) {
                    xt2[2] = 0;
                }
                try {
                    xt2[3] = Integer.parseInt(jt4.getText());
                } catch (Exception e) {
                    xt2[3] = 0;
                }
                try {
                    FileWriter fw = new FileWriter("data.ini");
                    fw.write(Integer.toString(xt2[0]));
                    fw.write(System.lineSeparator());
                    fw.write(Integer.toString(xt2[1]));
                    fw.write(System.lineSeparator());
                    fw.write(Integer.toString(xt2[2]));
                    fw.write(System.lineSeparator());
                    fw.write(Integer.toString(xt2[3]));
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dispose();
            }
        });
        JButton btnCancel = new JButton(res.getString("Cancel"));
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel bp = new JPanel();
        bp.add(btnOk);
        bp.add(btnCancel);
        getContentPane().add(p, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
        add(p);
        setTitle(res.getString("Adjustment"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }
}
