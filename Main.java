
import facade.ParkingFacade;
import gui.MainGUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("========================================");
        System.out.println("  PARKING LOT MANAGEMENT SYSTEM");
        System.out.println("  CCP6224 - Object-Oriented Analysis");
        System.out.println("========================================\n");
        
        ParkingFacade facade = ParkingFacade.getInstance();
        facade.initializeSystem();
        
        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            gui.setVisible(true);
        });
    }
}