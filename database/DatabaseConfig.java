package database;

public class DatabaseConfig {
    // MySQL Configuration - PORT 3307
    public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String URL = "jdbc:mysql://localhost:3307/";  // ← CHANGED TO 3307!
    public static final String DB_NAME = "parking_system";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "";
    
    // Connection parameters
    public static final String CONNECTION_STRING = URL + DB_NAME + 
        "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
}