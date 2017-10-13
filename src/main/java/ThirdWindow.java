import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.internal.chartpart.Chart;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.ResourceBundle;

/**
 * Window for vieing results
 */
class ThirdWindow extends JFrame {
    ThirdWindow(final OxyFile oFile, final Object[] objectsFromCalculate) throws HeadlessException, FileNotFoundException {
        final ResourceBundle res = ResourceBundle.getBundle("data");
        setTitle(res.getString("Title"));
        JTextArea textArea = new JTextArea((String) objectsFromCalculate[1]);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        JPanel p = new JPanel();
        p.add(textArea);
        setLayout(null);
        p.setBounds(0, 0, 250, 400);
        add(p);
        XChartPanel ch = new XChartPanel<>((Chart) objectsFromCalculate[0]);
        ch.setBounds(250, 0, 600, 600);
        add(ch);
        JButton backButton = new JButton(res.getString("Back"));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new SecondWindow(oFile);
                } finally {
                    dispose();
                }
            }
        });
        backButton.setBounds(10, 410, 100, 30);
        add(backButton);
        final JButton excelButton = new JButton(res.getString("CopyExcel"));
        excelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String str = (String) objectsFromCalculate[2];
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Clipboard clipboard = toolkit.getSystemClipboard();
                StringSelection strSel = new StringSelection(str);
                clipboard.setContents(strSel, null);
            }
        });
        excelButton.setBounds(120, 410, 100, 30);
        add(excelButton);
        setPreferredSize(new Dimension(900, 700));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }
}