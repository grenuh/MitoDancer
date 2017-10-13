import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Visual class
 * Show first window (witn d-n-d functional)
 */
public class FirstWindow extends JFrame {
    private FirstWindow() {
        final ResourceBundle res = ResourceBundle.getBundle("data");
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
        menubar.add(menuA);
        setJMenuBar(menubar);
        JLabel myLabel = new JLabel(res.getString("DragFile"), SwingConstants.CENTER);
        DragDropListener myDragDropListener = new DragDropListener(this);
        new DropTarget(myLabel, myDragDropListener);
        this.getContentPane().add(BorderLayout.CENTER, myLabel);
        setTitle(res.getString("Title"));
        setPreferredSize(new Dimension(640, 480));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    /**
     * Enter entry
     *
     * @param args none
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new FirstWindow();
    }

}

/**
 * DragDropListener for implementing drag--n-drop functional
 */

class DragDropListener implements DropTargetListener {
    private FirstWindow jf;

    DragDropListener(FirstWindow firstWindow) {
        jf = firstWindow;
    }

    /**
     * One released function
     * Get files and get them to next step
     *
     * @param event event of dropping filtered files
     */
    @Override
    public void drop(DropTargetDropEvent event) {
        event.acceptDrop(DnDConstants.ACTION_COPY);
        Transferable transferable = event.getTransferable();
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (DataFlavor flavor : flavors)
            try {
                if (flavor.isFlavorJavaFileListType()) {
                    Object g = transferable.getTransferData(flavor);
                    //noinspection unchecked
                    java.util.List<ArrayList<File>> ar = (java.util.List<ArrayList<File>>) g;
                    //noinspection ForLoopReplaceableByForEach
                    for (int gg = 0; gg < ar.size(); gg++) {
                        Object f = ar.get(gg);
                        File f2 = null;
                        try {
                            //noinspection ConstantConditions
                            f2 = (File) f;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String extension = "rc4";
                        String path = f2.getAbsolutePath().toLowerCase();
                        if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
                            new SecondWindow(f2);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        event.dropComplete(true);
        jf.dispose();
    }

    /**
     * Empty action
     *
     * @param event null
     */
    @Override
    public void dragEnter(DropTargetDragEvent event) {
    }

    /**
     * Empty action
     *
     * @param event null
     */
    @Override
    public void dragExit(DropTargetEvent event) {
    }

    /**
     * Empty action
     *
     * @param event null
     */
    @Override
    public void dragOver(DropTargetDragEvent event) {
    }

    /**
     * Empty action
     *
     * @param event null
     */
    @Override
    public void dropActionChanged(DropTargetDragEvent event) {
    }
}

/**
 * Filter of file extensions
 */
class ExtensionFileFilter extends FileFilter {
    /**
     * Empty constructor
     */
    ExtensionFileFilter() {
    }

    /**
     * simple getter
     * @return
     */
    public String getDescription() {
        //TODO   Extend or replace by your class!
        return "RC4";
    }

    /**
     * Acceptance functions
     * @param file checked file
     * @return if file is that description
     */
    public boolean accept(File file) {
        String extension = "rc4";
        if (file.isDirectory()) {
            return true;
        } else {
            String path = file.getAbsolutePath().toLowerCase();
            if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
                return true;
            }
        }
        return false;
    }
}
