import gui.MainGUI;
import database.DatabaseManager;
import facade.ParkingFacade;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        // Run GUI on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("========================================");
                    System.out.println("  PARKING LOT MANAGEMENT SYSTEM");
                    System.out.println("  CCP6224 - Object-Oriented Analysis");
                    System.out.println("========================================\n");
                    
                    // Get facade instance first
                    ParkingFacade facade = ParkingFacade.getInstance();
                    
                    // ✅ Initialize the parking lot with default spots (5 floors, 3 rows, 10 spots each)
                    facade.getParkingLot().initializeDefault();
                    
                    // Initialize database connection
                    DatabaseManager db = DatabaseManager.getInstance();
                    db.connect();
                    
                    if (!db.isConnected()) {
                        System.out.println("\n⚠️ WARNING: Running in OFFLINE MODE");
                        System.out.println("   Database features will be disabled");
                        System.out.println("   Data will NOT persist after restart\n");
                    } else {
                        System.out.println("✅ Database connected - Data will persist\n");
                        
                        // Load any existing parked vehicles from database
                        facade.loadParkedVehiclesFromDB();
                    }
                    
                    // Start the GUI
                    System.out.println("Starting GUI...");
                    MainGUI gui = new MainGUI();
                    gui.setVisible(true);
                    
                    System.out.println("✅ Application started successfully");
                    
                } catch (Exception e) {
                    System.err.println("\n❌ Failed to start application:");
                    System.err.println("   " + e.getMessage());
                    e.printStackTrace();
                    
                    // Show error dialog
                    JOptionPane.showMessageDialog(
                        null,
                        "Failed to start application:\n" + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }
}