import database.DatabaseManager;

public class TestDB {
    public static void main(String[] args) {
        System.out.println("🔌 Testing MySQL Connection...");
        
        DatabaseManager db = DatabaseManager.getInstance();
        db.connect();
        
        System.out.println("✅ Test completed!");
        
        db.disconnect();
    }
}