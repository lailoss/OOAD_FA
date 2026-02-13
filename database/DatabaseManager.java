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
    
    private DatabaseManager() {
        try {
            Class.forName(DatabaseConfig.DRIVER);
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
        try {
            connection = DriverManager.getConnection(
                DatabaseConfig.URL + DatabaseConfig.DB_NAME + "?useSSL=false&serverTimezone=UTC",
                DatabaseConfig.USERNAME, 
                DatabaseConfig.PASSWORD
            );
            System.out.println("✅ MySQL Database connected successfully!");
            System.out.println("📁 Database: " + DatabaseConfig.DB_NAME);
            System.out.println("🔌 Host: localhost:3306");
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            System.err.println("💡 Make sure XAMPP/WAMP MySQL is running!");
            System.err.println("💡 Also ensure database '" + DatabaseConfig.DB_NAME + "' exists");
            System.err.println("💡 Import schema.sql using phpMyAdmin");
        }
    }
    
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database disconnected");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error disconnecting: " + e.getMessage());
        }
    }
    
    // ============ VEHICLE OPERATIONS ============
    
    public void saveVehicle(Vehicle vehicle) {
        String sql = "INSERT INTO vehicles (license_plate, vehicle_type, has_handicapped_card) VALUES (?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE vehicle_type = ?, has_handicapped_card = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, vehicle.getLicensePlate());
            pstmt.setString(2, vehicle.getVehicleType().name());
            pstmt.setBoolean(3, vehicle.hasHandicappedCard());
            pstmt.setString(4, vehicle.getVehicleType().name());
            pstmt.setBoolean(5, vehicle.hasHandicappedCard());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Error saving vehicle: " + e.getMessage());
        }
    }
    
    // ============ PARKING SPOT OPERATIONS ============
    
    public void updateParkingSpot(ParkingSpot spot) {
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
        } catch (SQLException e) {
            System.err.println("❌ Error updating spot: " + e.getMessage());
        }
    }
    
    // ============ TICKET OPERATIONS ============
    
    public void saveTicket(Ticket ticket) {
        String sql = "INSERT INTO tickets (ticket_id, license_plate, spot_id, entry_time, is_active) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ticket.getTicketId());
            pstmt.setString(2, ticket.getVehicle().getLicensePlate());
            pstmt.setString(3, ticket.getSpot().getSpotId());
            pstmt.setTimestamp(4, Timestamp.valueOf(ticket.getEntryTime()));
            pstmt.setBoolean(5, true);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Error saving ticket: " + e.getMessage());
        }
    }
    
    public Ticket getActiveTicket(String licensePlate) {
        String sql = "SELECT * FROM tickets WHERE license_plate = ? AND is_active = true ORDER BY entry_time DESC LIMIT 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, licensePlate);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // For full implementation, you would reconstruct the Ticket object
                // For this assignment, we'll rely on in-memory objects
                return null;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting ticket: " + e.getMessage());
        }
        return null;
    }
    
    public void deactivateTicket(String ticketId) {
        String sql = "UPDATE tickets SET is_active = false, exit_time = ? WHERE ticket_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, ticketId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Error deactivating ticket: " + e.getMessage());
        }
    }
    
    // ============ FINE OPERATIONS ============
    
    public void saveFine(Fine fine) {
        String sql = "INSERT INTO fines (fine_id, license_plate, amount, reason, is_paid) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, fine.getFineId());
            pstmt.setString(2, fine.getLicensePlate());
            pstmt.setDouble(3, fine.getAmount());
            pstmt.setString(4, fine.getReason());
            pstmt.setBoolean(5, fine.isPaid());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Error saving fine: " + e.getMessage());
        }
    }
    
    public List<Fine> getUnpaidFines(String licensePlate) {
        List<Fine> fines = new ArrayList<>();
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
        } catch (SQLException e) {
            System.err.println("❌ Error getting fines: " + e.getMessage());
        }
        
        return fines;
    }
    
    public void markFineAsPaid(String fineId) {
        String sql = "UPDATE fines SET is_paid = true, paid_date = ? WHERE fine_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, fineId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Error marking fine as paid: " + e.getMessage());
        }
    }
    
    // ============ RECEIPT OPERATIONS ============
    
    public void saveReceipt(Receipt receipt) {
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
        } catch (SQLException e) {
            System.err.println("❌ Error saving receipt: " + e.getMessage());
        }
    }
    
    // ============ REPORT OPERATIONS ============
    
    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total FROM receipts";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting revenue: " + e.getMessage());
        }
        
        return 0.0;
    }
    
    public Map<String, List<Fine>> getAllUnpaidFines() {
        Map<String, List<Fine>> allFines = new HashMap<>();
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
        } catch (SQLException e) {
            System.err.println("❌ Error getting unpaid fines: " + e.getMessage());
        }
        
        return allFines;
    }
    
    public FineSchemeType getActiveFineScheme() {
        String sql = "SELECT scheme_type FROM fine_schemes WHERE is_active = true LIMIT 1";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return FineSchemeType.valueOf(rs.getString("scheme_type"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting active fine scheme: " + e.getMessage());
        }
        
        return FineSchemeType.FIXED;
    }
    
    public void setActiveFineScheme(FineSchemeType schemeType) {
        String sql = "UPDATE fine_schemes SET is_active = false";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            
            sql = "UPDATE fine_schemes SET is_active = true WHERE scheme_type = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, schemeType.name());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("❌ Error setting fine scheme: " + e.getMessage());
        }
    }
}