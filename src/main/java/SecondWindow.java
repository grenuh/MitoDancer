import org.knowm.xchart.XChartPanel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ResourceBundle;

/**
 * Main operation window
 */
class SecondWindow extends JFrame {
    private OxyFile oxyfile;
    final JCheckBox chb2;
    final JCheckBox chb3;
    final JTextField jt1;

    /**
     * Constructor for backwarding
     *
     * @param selectedFile analysed file
     */
    SecondWindow(File selectedFile) {
        this(new Record4File(selectedFile));
    }

    /**
     * Typical constructor
     *
     * @param oxFile data for analysis
     */
    SecondWindow(final OxyFile oxFile) {
        final ResourceBundle res = ResourceBundle.getBundle("data");
        oxyfile = oxFile;
        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu(res.getString("File"));
        JMenuItem itm = new JMenuItem(res.getString("Open"));
        menu.add(itm);
        itm.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(".");
                FileFilter jpegFilter = new ExtensionFileFilter();
                fileChooser.addChoosableFileFilter(jpegFilter);
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    new SecondWindow(selectedFile);
                    dispose();
                }
            }
        }));
        itm = new JMenuItem(res.getString("Adjustment"));
        itm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Adjustment();
            }
        });
        menu.add(itm);
        menu.add(new JSeparator());
        itm = new JMenuItem(res.getString("Exit"));
        itm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
                InputEvent.ALT_MASK));
        itm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        menu.add(itm);
        JMenu menuB = new JMenu(res.getString("Edit"));
        JMenuItem itmB1 = new JMenuItem(res.getString("Rupture"));
        itmB1.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Rupture(oxyfile);
                dispose();
            }
        }));
        JMenuItem itmB2 = new JMenuItem(res.getString("Calibration"));
        itmB2.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Calibration(oxyfile);
                dispose();
            }
        }));
        menuB.add(itmB1);
        menuB.add(itmB2);
        JMenu menuA = new JMenu(res.getString("About"));
        JMenuItem itm2 = new JMenuItem(res.getString("About"));
        itm2.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, res.getString("AboutText"), res.getString("About"), JOptionPane.INFORMATION_MESSAGE);
            }
        }));
        menuA.add(itm2);
        menubar.add(menu);
        menubar.add(menuB);
        menubar.add(menuA);
        setJMenuBar(menubar);
        setTitle(res.getString("Title"));
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.insets = new Insets(6, 6, 6, 6);
        JLabel lb1 = new JLabel(res.getString("Adjustment"));
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        p.add(lb1, cs);
        final JCheckBox chb1 = new JCheckBox(res.getString("AddDiagr"));
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        p.add(chb1, cs);
        chb2 = new JCheckBox(res.getString("PreCalibr"));
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 1;
        p.add(chb2, cs);
        chb3 = new JCheckBox(res.getString("Fading(exp)"));
        cs.gridx = 0;
        cs.gridy = 2;
        cs.gridwidth = 1;
        p.add(chb3, cs);
        final JCheckBox chb4 = new JCheckBox(res.getString("Export"));
        cs.gridx = 1;
        cs.gridy = 2;
        cs.gridwidth = 1;
        p.add(chb4, cs);
        final JCheckBox chb5 = new JCheckBox(res.getString("SMA"));
        cs.gridx = 0;
        cs.gridy = 3;
        cs.gridwidth = 1;
        p.add(chb5, cs);
        final JCheckBox chb6 = new JCheckBox(res.getString("Old_calc"));
        cs.gridx = 1;
        cs.gridy = 3;
        cs.gridwidth = 1;
        p.add(chb6, cs);
        JLabel lb2 = new JLabel(res.getString("Markers"));
        cs.gridx = 0;
        cs.gridy = 4;
        cs.gridwidth = 1;
        p.add(lb2, cs);
        jt1 = new JTextField();
        cs.gridx = 0;
        cs.gridy = 5;
        cs.gridwidth = 2;
        p.add(jt1, cs);
        JLabel lb3 = new JLabel(
                "<html><table border=\"0\">\n<tr>\n" +
                        "<td>1 - " + res.getString("UpR") + "</td>\n<td>6 - " +
                        res.getString("Vd") + "</td>\n</tr>\n<tr>\n" +
                        "<td>2 - " + res.getString("V2") + "</td>\n<td>7 - " +
                        res.getString("V3") + "+" + res.getString("V4") +
                        "+" + res.getString("V") + "</td>\n</tr>\n<tr>\n" +
                        "<td></td>\n<td></td>\n</tr>\n<tr>\n<td>4 - " +
                        res.getString("V2") + "+" + res.getString("V3") +
                        "</td>\n<td>9 - " + res.getString("DwR") + "</td>\n" +
                        "</tr>\n<tr>\n<td>5 - " + res.getString("V3") + " " +
                        res.getString("OLS") + "</td>\n<td></td>\n</tr>\n" +
                        "</table></html>");
        cs.gridx = 0;
        cs.gridy = 6;
        cs.gridwidth = 2;
        p.add(lb3, cs);
        JButton jb1 = new JButton(res.getString("OK"));
        jb1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ent = jt1.getText();
                if (ent.length() != oxyfile.getAdditionSize()) {
                    JOptionPane.showMessageDialog(null, res.getString("ErrorMark"), res.getString("Error"), JOptionPane.ERROR_MESSAGE);
                } else {
                    if (ent.charAt(0) != '1') {
                        JOptionPane.showMessageDialog(null, res.getString("ErrorBegin"), res.getString("Error"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        Object[] formulas;
                        try {

                            if (!chb2.isSelected()) {

                                if (ent.charAt(ent.length() - 1) != '9') {
                                    JOptionPane.showMessageDialog(null, res.getString("ErrorBegin"), res.getString("Error"), JOptionPane.ERROR_MESSAGE);
                                } else {
                                    formulas = MathRC4.calculate(ent, chb1.isSelected(), chb2.isSelected(), chb3.isSelected(), chb4.isSelected(),chb5.isSelected(), chb6.isSelected(), oxyfile.getData());
                                    try {
                                        new ThirdWindow(oxyfile, formulas);
                                    } catch (FileNotFoundException e1) {
                                        e1.printStackTrace();
                                    }
                                    dispose();
                                }
                            } else {

                                formulas = MathRC4.calculate(ent, chb1.isSelected(), chb2.isSelected(), chb3.isSelected(), chb4.isSelected(),chb5.isSelected(), chb6.isSelected(),  oxyfile.getData());
                                try {
                                    new ThirdWindow(oxyfile, formulas);
                                } catch (FileNotFoundException e1) {
                                    e1.printStackTrace();
                                }
                                dispose();
                            }
                        } catch (HeadlessException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
        cs.gridx = 0;
        cs.gridy = 7;
        cs.gridwidth = 1;
        p.add(jb1, cs);
        JTextArea textArea = new JTextArea(oxyfile.getPath());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        cs.gridx = 0;
        cs.gridy = 8;
        cs.gridwidth = 2;
        p.add(textArea, cs);
        p.setPreferredSize(new Dimension(250, 400));
        setLayout(null);
        p.setBounds(5, 5, 250, 400);
        add(p);
        XChartPanel ch = oxyfile.getChart();
        ch.setBounds(270, 0, 600, 600);
        add(ch);
        setPreferredSize(new Dimension(900, 700));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    // For test uses
    public static void main(String[] args) throws FileNotFoundException {
        File f = new File("test.rc4");
        SecondWindow dd = new SecondWindow(f);
        dd.chb3.setSelected(true);
        dd.chb2.setSelected(true);
        dd.jt1.setText("124");
    }
}
