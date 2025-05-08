package edu.ezip.ing1.pds;

import edu.ezip.ing1.pds.gui.MainTemplate;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainFrontEnd {

    public static void main(String[] args) {
        //new MainInterfaceFrame(); // notre old version

        SwingUtilities.invokeLater(() -> {
            try {
            
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
            
                MainTemplate mainWindow = new MainTemplate();
                mainWindow.setVisible(true);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
