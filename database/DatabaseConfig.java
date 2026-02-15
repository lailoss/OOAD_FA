package database;

public class DatabaseConfig {
   public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
   public static final String URL = "jdbc:mysql://localhost:3306/";  // 3306!
   public static final String DB_NAME = "parking_system";
   public static final String USERNAME = "root";
   public static final String PASSWORD = "";
   public static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/parking_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"; // 3306!

   public DatabaseConfig() {
   }
}