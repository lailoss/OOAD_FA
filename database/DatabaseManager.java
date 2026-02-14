package database;

import vehicle.*;
import payment.*;
import model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private boolean isConnected = false;  // ADDED THIS LINE
    
    private DatabaseManager() {
        try {
            Class.forName(DatabaseConfig.DRIVER);
            System.out.println("✅ MySQL JDBC Driver loaded");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found!");
            System.err.println("📦 Please add mysql-connector-java-8.0.33.jar to project libraries");
        }
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public void connect() {
        // FORCE IPv4 for localhost
        System.setProperty("java.net.preferIPv4Stack", "true");
        
        try {
            String url = "jdbc:mysql://localhost:3306/" + DatabaseConfig.DB_NAME + 
                         "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            
            System.out.println("🔌 Connecting to: " + url);
            connection = DriverManager.getConnection(url, "root", "");
            isConnected = true;  // FIXED: Set connected to true
            System.out.println("✅ Connected to localhost!");
            
            // Test the connection
            testConnection();
            
        } catch (SQLException e) {
            isConnected = false;  // FIXED: Set connected to false on error
            System.out.println("\n❌ DATABASE CONNECTION FAILED!");
            System.out.println("========================================");
            System.out.println("Error: " + e.getMessage());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("========================================");
            System.out.println("💡 FIX THESE ISSUES:");
            System.out.println("1. Is XAMPP/WAMP running? Start MySQL");
            System.out.println("2. Did you create database? CREATE DATABASE parking_system;");
            System.out.println("3. Check credentials in DatabaseConfig.java");
            System.out.println("4. Is port 3306 free? (Check XAMPP)");
            System.out.println("========================================\n");
        }
    }
    
    // ADDED THIS METHOD
    public boolean isConnected() {
        return isConnected;
    }
    
    private void testConnection() throws SQLException {
        // Check if database exists and has tables
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet tables = meta.getTables(null, null, "parking_spots", null);
        
        if (tables.next()) {
            System.out.println("✅ Database 'parking_system' is ready");
            
            // Count parking spots
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM parking_spots");
            if (rs.next()) {
                int spots = rs.getInt("count");
                System.out.println("✅ " + spots + " parking spots loaded");
            }
            rs.close();
            stmt.close();
        } else {
            System.out.println("⚠️ Tables not found. Run schema.sql to create tables");
        }
        tables.close();
    }
    
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                isConnected = false;
                System.out.println("✅ Database disconnected");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error disconnecting: " + e.getMessage());
        }
    }
    
    // ============ VEHICLE OPERATIONS ============
    
    public void saveVehicle(Vehicle vehicle) {
        if (!isConnected) {
            System.out.println("⚠️ Not connected to database");
            return;
        }
        
        String sql = "INSERT INTO vehicles (license_plate, vehicle_type, has_handicapped_card) VALUES (?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE vehicle_type = ?, has_handicapped_card = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, vehicle.getLicensePlate());
            pstmt.setString(2, vehicle.getVehicleType().name());
            pstmt.setBoolean(3, vehicle.hasHandicappedCard());
            pstmt.setString(4, vehicle.getVehicleType().name());
            pstmt.setBoolean(5, vehicle.hasHandicappedCard());
            pstmt.executeUpdate();
            System.out.println("✅ Vehicle saved: " + vehicle.getLicensePlate());
        } catch (SQLException e) {
            System.err.println("❌ Error saving vehicle: " + e.getMessage());
        }
    }
    
    // ============ PARKING SPOT OPERATIONS ============
    
    public void updateParkingSpot(ParkingSpot spot) {
        if (!isConnected) {
            System.out.println("⚠️ Not connected to database");
            return;
        }
        
        String sql = "UPDATE parking_spots SET status = ?, current_vehicle = ? WHERE spot_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, spot.getStatus().name());
            
            if (spot.getCurrentVehicle() != null) {
                pstmt.setString(2, spot.getCurrentVehicle().getLicensePlate());
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }
            
            pstmt.setString(3, spot.getSpotId());
            pstmt.executeUpdate();
            System.out.println("✅ Spot updated: " + spot.getSpotId() + " - " + spot.getStatus());
        } catch (SQLException e) {
            System.err.println("❌ Error updating spot: " + e.getMessage());
        }
    }
    
    // ============ TICKET OPERATIONS ============
    
    public void saveTicket(Ticket ticket) {
        if (!isConnected) {
            System.out.println("⚠️ Not connected to database");
            return;
        }
        
        String sql = "INSERT INTO tickets (ticket_id, license_plate, spot_id, entry_time, is_active) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ticket.getTicketId());
            pstmt.setString(2, ticket.getVehicle().getLicensePlate());
            pstmt.setString(3, ticket.getSpot().getSpotId());
            pstmt.setTimestamp(4, Timestamp.valueOf(ticket.getEntryTime()));
            pstmt.setBoolean(5, true);
            pstmt.executeUpdate();
            System.out.println("✅ Ticket saved: " + ticket.getTicketId());
        } catch (SQLException e) {
            System.err.println("❌ Error saving ticket: " + e.getMessage());
        }
    }
    
    public Ticket getActiveTicket(String licensePlate) {
        if (!isConnected) {
            System.out.println("⚠️ Not connected to database");
            return null;
        }
        
        String sql = "SELECT * FROM tickets WHERE license_plate = ? AND is_active = true ORDER BY entry_time DESC LIMIT 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, licensePlate);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // For full implementation, you would reconstruct the Ticket object
                // For this assignment, we rely on in-memory objects in ParkingFacade
                return null;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting ticket: " + e.getMessage());
        }
        return null;
    }
    
    public void deactivateTicket(String ticketId) {
        if (!isConnected) {
            System.out.println("⚠️ Not connected to database");
            return;
        }
        
        String sql = "UPDATE tickets SET is_active = false, exit_time = ? WHERE ticket_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, ticketId);
            pstmt.executeUpdate();
            System.out.println("✅ Ticket deactivated: " + ticketId);
        } catch (SQLException e) {
            System.err.println("❌ Error deactivating ticket: " + e.getMessage());
        }
    }
    
    // ============ FINE OPERATIONS ============
    
    public void saveFine(Fine fine) {
        if (!isConnected) {
            System.out.println("⚠️ Not connected to database");
            return;
        }
        
        String sql = "INSERT INTO fines (fine_id, license_plate, amount, reason, is_paid) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, fine.getFineId());
            pstmt.setString(2, fine.getLicensePlate());
            pstmt.setDouble(3, fine.getAmount());
            pstmt.setString(4, fine.getReason());
            pstmt.setBoolean(5, fine.isPaid());
            pstmt.executeUpdate();
            System.out.println("✅ Fine saved: RM" + fine.getAmount() + " - " + fine.getReason());
        } catch (SQLException e) {
            System.err.println("❌ Error saving fine: " + e.getMessage());
        }
    }
    
    public List<Fine> getUnpaidFines(String licensePlate) {
        List<Fine> fines = new ArrayList<>();
        
        if (!isConnected) {
            System.out.println("⚠️ Not connected to database");
            return fines;
        }
        
        String sql = "SELECT * FROM fines WHERE license_plate = ? AND is_paid = false";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, licensePlate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Fine fine = new Fine(
                    rs.getString("license_plate"),
                    rs.getDouble("amount"),
                    rs.getString("reason")
                );
                fine.setFineId(rs.getString("fine_id"));
                fines.add(fine);
            }
            if (!fines.isEmpty()) {
                System.out.println("✅ Found " + fines.size() + " unpaid fines for " + licensePlate);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting fines: " + e.getMessage());
        }
        
        return fines;
    }
    
    public void markFineAsPaid(String fineId) {
        if (!isConnected) {
            System.out.println("⚠️ Not connected to database");
            return;
        }
        
        String sql = "UPDATE fines SET is_paid = true, paid_date = ? WHERE fine_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, fineId);
            pstmt.executeUpdate();
            System.out.println("✅ Fine marked as paid: " + fineId);
        } catch (SQLException e) {
            System.err.println("❌ Error marking fine as paid: " + e.getMessage());
        }
    }
    
    // ============ RECEIPT OPERATIONS ============
    
    public void saveReceipt(Receipt receipt) {
        if (!isConnected) {
            System.out.println("⚠️ Not connected to database");
            return;
        }
        
        String sql = "INSERT INTO receipts (receipt_id, ticket_id, exit_time, duration_hours, parking_fee, total_fines, total_amount) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, receipt.getReceiptId());
            pstmt.setString(2, receipt.getTicket().getTicketId());
            pstmt.setTimestamp(3, Timestamp.valueOf(receipt.getExitTime()));
            pstmt.setLong(4, receipt.getDurationHours());
            pstmt.setDouble(5, receipt.getParkingFee());
            pstmt.setDouble(6, receipt.getTotalFines());
            pstmt.setDouble(7, receipt.getTotalAmount());
            pstmt.executeUpdate();
            System.out.println("✅ Receipt saved: " + receipt.getReceiptId());
            System.out.println("💰 Amount: RM" + String.format("%.2f", receipt.getTotalAmount()));
        } catch (SQLException e) {
            System.err.println("❌ Error saving receipt: " + e.getMessage());
        }
    }
    
    // ============ REPORT OPERATIONS ============
    
    public double getTotalRevenue() {
        if (!isConnected) {
            System.out.println("⚠️ Not connected to database");
            return 0.0;
        }
        
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total FROM receipts";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                double revenue = rs.getDouble("total");
                System.out.println("💰 Total revenue from DB: RM" + String.format("%.2f", revenue));
                return revenue;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting revenue: " + e.getMessage());
        }
        
        return 0.0;
    }
    
    public Map<String, List<Fine>> getAllUnpaidFines() {
        Map<String, List<Fine>> allFines = new HashMap<>();
        
        if (!isConnected) {
            System.out.println("⚠️ Not connected to database");
            return allFines;
        }
        
        String sql = "SELECT * FROM fines WHERE is_paid = false ORDER BY license_plate";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                String plate = rs.getString("license_plate");
                Fine fine = new Fine(
                    plate,
                    rs.getDouble("amount"),
                    rs.getString("reason")
                );
                fine.setFineId(rs.getString("fine_id"));
                
                allFines.computeIfAbsent(plate, k -> new ArrayList<>()).add(fine);
            }
            System.out.println("✅ Retrieved unpaid fines: " + allFines.size() + " vehicles");
        } catch (SQLException e) {
            System.err.println("❌ Error getting unpaid fines: " + e.getMessage());
        }
        
        return allFines;
    }
    
    public FineSchemeType getActiveFineScheme() {
        if (!isConnected) {
            System.out.println("⚠️ Not connected to database");
            return FineSchemeType.FIXED;
        }
        
        String sql = "SELECT scheme_type FROM fine_schemes WHERE is_active = true LIMIT 1";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                FineSchemeType type = FineSchemeType.valueOf(rs.getString("scheme_type"));
                System.out.println("✅ Active fine scheme: " + type);
                return type;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting active fine scheme: " + e.getMessage());
        }
        
        return FineSchemeType.FIXED; // Default
    }
    
    public void setActiveFineScheme(FineSchemeType schemeType) {
        if (!isConnected) {
            System.out.println("⚠️ Not connected to database");
            return;
        }
        
        String sql1 = "UPDATE fine_schemes SET is_active = false";
        String sql2 = "UPDATE fine_schemes SET is_active = true WHERE scheme_type = ?";
        
        try {
            connection.setAutoCommit(false);
            
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(sql1);
            }
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql2)) {
                pstmt.setString(1, schemeType.name());
                pstmt.executeUpdate();
            }
            
            connection.commit();
            connection.setAutoCommit(true);
            System.out.println("✅ Active fine scheme changed to: " + schemeType);
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("❌ Rollback failed: " + ex.getMessage());
            }
            System.err.println("❌ Error setting fine scheme: " + e.getMessage());
        }
    }
}