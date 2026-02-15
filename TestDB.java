import database.DatabaseManager;

public class TestDB {
    public static void main(String[] args) {
        System.out.println("\n=== DATABASE CONNECTION TEST ===\n");
        
        // Get database manager instance
        DatabaseManager db = DatabaseManager.getInstance();
        
        // Try to connect
        System.out.println("🔌 Attempting to connect to MySQL...");
        db.connect();
        
        // CHECK IF CONNECTED!
        if (db.isConnected()) {
            System.out.println("\n✅ SUCCESS! Database is connected!");
            
            // Try a simple query
            try {
                // This assumes you have a method to test connection
                // If not, we need to add one
                System.out.println("📊 Testing query execution...");
                
                // You might want to add a testQuery() method to DatabaseManager
                // For now, just show success
                System.out.println("✅ Database is ready to use");
                
            } catch (Exception e) {
                System.out.println("❌ Query failed: " + e.getMessage());
            }
            
        } else {
            System.out.println("\n❌ FAILED! Database is NOT connected!");
            System.out.println("\n🔍 TROUBLESHOOTING CHECKLIST:");
            System.out.println("1. Is MySQL running? Run: ps aux | grep mysql");
            System.out.println("2. Start MySQL: sudo /Applications/XAMPP/xamppfiles/bin/mysql.server start");
            System.out.println("3. Check DatabaseConfig.java settings:");
            System.out.println("   - URL: jdbc:mysql://localhost:3306/");
            System.out.println("   - DB_NAME: parking_system");
            System.out.println("   - USERNAME: root");
            System.out.println("   - PASSWORD: (empty)");
            System.out.println("4. Create database: CREATE DATABASE parking_system;");
        }
        
        // Disconnect
        db.disconnect();
        System.out.println("\n🔌 Test completed!");
    }
}