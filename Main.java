import gui.MainGUI;
import facade.ParkingFacade;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("========================================");
                System.out.println("  PARKING LOT MANAGEMENT SYSTEM");
                System.out.println("  CCP6224 - Object-Oriented Analysis");
                System.out.println("========================================\n");
                
                // Initialize facade (THIS WILL NOW LOAD DATA FROM DB)
                ParkingFacade facade = ParkingFacade.getInstance();
                facade.initializeSystem();
                
                // Start GUI
                MainGUI gui = new MainGUI();
                gui.setVisible(true);
                
            } catch (Exception e) {
                System.err.println("❌ Failed to start: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}