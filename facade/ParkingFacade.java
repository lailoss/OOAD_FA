package facade;

import model.*;
import vehicle.*;
import payment.*;
import database.DatabaseManager;
import java.time.LocalDateTime;
import java.util.*;
import java.time.format.DateTimeFormatter;

public class ParkingFacade {
    private static ParkingFacade instance;
    private ParkingLot parkingLot;
    private DatabaseManager dbManager;
    
    private ParkingFacade() {
        this.parkingLot = ParkingLot.getInstance();
        this.dbManager = DatabaseManager.getInstance();
        this.parkingLot.setFineScheme(new FixedFineScheme());
    }
    
    public static synchronized ParkingFacade getInstance() {
        if (instance == null) {
            instance = new ParkingFacade();
        }
        return instance;
    }
    
    public void initializeSystem() {
        parkingLot.initializeDefault();
        dbManager.connect();
        
        // CRITICAL FIX: Load existing data from database
        if (dbManager.isConnected()) {
            System.out.println("\n🔄 Loading existing data from database...");
            loadParkedVehiclesFromDB();  // Load occupied spots
            loadRevenueFromDB();          // Load revenue
            loadFineSchemeFromDB();       // Load fine scheme
        }
        
        System.out.println("✅ Parking System Initialized with MySQL\n");
    }
    
    // ============ DATABASE LOADING METHODS ============
    
    /**
     * Loads all currently parked vehicles from the database into the in‑memory parking lot.
     */
    public void loadParkedVehiclesFromDB() {
        List<ParkingSpot> occupiedSpots = dbManager.getOccupiedSpots();
        int loadedCount = 0;
        
        for (ParkingSpot spot : occupiedSpots) {
            ParkingSpot existingSpot = parkingLot.getSpot(spot.getSpotId());
            if (existingSpot != null && spot.getCurrentVehicle() != null) {
                existingSpot.assignVehicle(spot.getCurrentVehicle());
                existingSpot.setStatus(SpotStatus.OCCUPIED);
                loadedCount++;
                
                // Debug output
                System.out.println("   📍 Spot " + spot.getSpotId() + 
                    ": " + spot.getCurrentVehicle().getLicensePlate());
            }
        }
        
        System.out.println("✅ Loaded " + loadedCount + " parked vehicles from database");
    }
    
    /**
     * Load revenue from database
     */
    private void loadRevenueFromDB() {
        double revenue = dbManager.getTotalRevenue();
        parkingLot.setTotalRevenue(revenue);
        System.out.println("💰 Loaded revenue: RM" + String.format("%.2f", revenue));
    }
    
    /**
     * Load fine scheme from database
     */
    private void loadFineSchemeFromDB() {
        FineSchemeType schemeType = dbManager.getActiveFineScheme();
        setFineScheme(schemeType);
        System.out.println("⚙️ Loaded fine scheme: " + schemeType);
    }
    
    // ============ PARKING OPERATIONS ============
    
    public List<ParkingSpot> getAvailableSpots(VehicleType vehicleType) {
        return parkingLot.getAvailableSpotsByVehicle(vehicleType);
    }
    
    public ParkingSpot getSpot(String spotId) {
        return parkingLot.getSpot(spotId);
    }
    
    public void freeSpot(String spotId) {
        ParkingSpot spot = getSpot(spotId);
        if (spot != null) {
            spot.removeVehicle();
            spot.setStatus(SpotStatus.AVAILABLE);
            dbManager.updateParkingSpot(spot);
        }
    }
    
    // ============ VEHICLE OPERATIONS ============
    
    public Ticket parkVehicle(String licensePlate, VehicleType type, 
                              boolean hasHandicappedCard, String spotId) {
        ParkingSpot spot = getSpot(spotId);
        if (spot == null || !spot.isAvailable()) {
            throw new IllegalStateException("Spot not available");
        }
        
        Vehicle vehicle = VehicleFactory.createVehicle(licensePlate, type, hasHandicappedCard);
        vehicle.setEntryTime(LocalDateTime.now());
        
        spot.assignVehicle(vehicle);
        spot.setStatus(SpotStatus.OCCUPIED);
        
        Ticket ticket = new Ticket(vehicle, spot);
        
        dbManager.saveVehicle(vehicle);
        dbManager.saveTicket(ticket);
        dbManager.updateParkingSpot(spot);
        
        System.out.println("✅ Vehicle parked: " + licensePlate + " in spot " + spotId);
        
        return ticket;
    }
    
    public Receipt exitVehicle(String licensePlate) {
        ParkingSpot occupiedSpot = null;
        Vehicle vehicle = null;
        
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (spot.getStatus() == SpotStatus.OCCUPIED && 
                spot.getCurrentVehicle() != null &&
                spot.getCurrentVehicle().getLicensePlate().equals(licensePlate)) {
                occupiedSpot = spot;
                vehicle = spot.getCurrentVehicle();
                break;
            }
        }
        
        if (vehicle == null || occupiedSpot == null) {
            throw new IllegalStateException("No vehicle found with plate: " + licensePlate);
        }
        
        vehicle.setExitTime(LocalDateTime.now());
        long hours = vehicle.calculateDuration(); // Now using CEILING rounding
        
        double parkingFee = hours * occupiedSpot.getHourlyRate();
        
        if (vehicle.hasHandicappedCard() && occupiedSpot.getType() == ParkingSpotType.HANDICAPPED) {
            parkingFee = 0.0;
        } else if (vehicle instanceof HandicappedVehicle) {
            parkingFee = hours * 2.0;
        }
        
        Ticket ticket = new Ticket(vehicle, occupiedSpot);
        
        List<Fine> unpaidFines = dbManager.getUnpaidFines(licensePlate);
        double totalFines = unpaidFines.stream().mapToDouble(Fine::getAmount).sum();
        
        if (hours > 24) {
            long overstayHours = hours - 24;
            FineScheme scheme = parkingLot.getFineScheme();
            double fineAmount = scheme.calculateFine(overstayHours);
            
            Fine overstayFine = new Fine(licensePlate, fineAmount, 
                "Overstaying (" + hours + " hours) - " + scheme.getName());
            
            dbManager.saveFine(overstayFine);
            unpaidFines.add(overstayFine);
            totalFines += fineAmount;
        }
        
        // FIXED: Reserved spot fine with EXACT requirement wording
        if (occupiedSpot.getType() == ParkingSpotType.RESERVED) {
            // TODO: Add VIP check here when implemented
            // boolean isVIP = checkIfVIP(licensePlate);
            // if (!isVIP) {
                Fine reservedFine = new Fine(licensePlate, 100.0, 
                    "Vehicle stays in reserved spot without a reservation");
                dbManager.saveFine(reservedFine);
                unpaidFines.add(reservedFine);
                totalFines += 100.0;
            // }
        }
                
        Receipt receipt = new Receipt(ticket);
        receipt.setParkingFee(parkingFee);
        receipt.setDurationHours(hours);
        receipt.setExitTime(LocalDateTime.now());
        
        for (Fine fine : unpaidFines) {
            receipt.addFine(fine);
        }
        
        receipt.calculateTotal();
        
        freeSpot(occupiedSpot.getSpotId());
        
        dbManager.saveReceipt(receipt);
        parkingLot.addRevenue(receipt.getTotalAmount());
        
        System.out.println("✅ Vehicle exited: " + licensePlate + 
            " | Fee: RM" + String.format("%.2f", parkingFee) +
            " | Fines: RM" + String.format("%.2f", totalFines));
        
        return receipt;
    }
    
    // ============ DATABASE SYNC METHODS ============
    
    /**
     * Get current occupied vehicles from database
     */
    public List<Vehicle> getCurrentVehiclesFromDB() {
        List<Vehicle> vehicles = new ArrayList<>();
        
        try {
            List<ParkingSpot> occupiedSpots = dbManager.getOccupiedSpots();
            for (ParkingSpot spot : occupiedSpots) {
                if (spot.getCurrentVehicle() != null) {
                    vehicles.add(spot.getCurrentVehicle());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error getting vehicles from DB: " + e.getMessage());
        }
        
        return vehicles;
    }
    
    /**
     * Get occupancy stats from database
     */
    public Map<String, Integer> getOccupancyStatsFromDB() {
        Map<String, Integer> stats = new HashMap<>();
        
        try {
            List<ParkingSpot> occupiedSpots = dbManager.getOccupiedSpots();
            stats.put("total", parkingLot.getTotalSpots());
            stats.put("occupied", occupiedSpots.size());
            stats.put("available", parkingLot.getTotalSpots() - occupiedSpots.size());
            
        } catch (Exception e) {
            System.out.println("❌ Error getting stats from DB: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Get total revenue from database
     */
    public double getTotalRevenueFromDB() {
        try {
            return dbManager.getTotalRevenue();
        } catch (Exception e) {
            System.out.println("❌ Error getting revenue from DB: " + e.getMessage());
            return 0.0;
        }
    }
    
    // ============ ADDED: Get all unpaid fines map ============
    /**
     * Get all unpaid fines grouped by license plate
     */
    public Map<String, List<Fine>> getAllUnpaidFines() {
        return dbManager.getAllUnpaidFines();
    }
    
    // ============ PAYMENT OPERATIONS ============
    
    public Payment processPayment(double amount, PaymentMethod method, Object paymentDetails) {
        Payment payment = null;
        
        if (method == PaymentMethod.CASH) {
            double cashTendered = (double) paymentDetails;
            payment = new CashPayment(amount, cashTendered);
        } else if (method == PaymentMethod.CARD) {
            payment = new CardPayment(amount, "1234567890123456", "Customer", "1225", "123");
        }
        
        if (payment != null) {
            payment.processPayment();
        }
        
        return payment;
    }
    
    public void payFines(String licensePlate, List<Fine> fines) {
        for (Fine fine : fines) {
            if (!fine.isPaid()) {
                fine.markPaid();
                dbManager.markFineAsPaid(fine.getFineId());
            }
        }
    }
    
    // ============ FINE MANAGEMENT ============
    
    public List<Fine> getUnpaidFines(String licensePlate) {
        return dbManager.getUnpaidFines(licensePlate);
    }
    
    public void setFineScheme(FineSchemeType schemeType) {
        FineScheme scheme;
        switch (schemeType) {
            case FIXED:
                scheme = new FixedFineScheme();
                break;
            case PROGRESSIVE:
                scheme = new ProgressiveFineScheme();
                break;
            case HOURLY:
                scheme = new HourlyFineScheme();
                break;
            default:
                scheme = new FixedFineScheme();
        }
        parkingLot.setFineScheme(scheme);
        dbManager.setActiveFineScheme(schemeType);
    }

    public FineScheme getCurrentFineScheme() {
        return parkingLot.getFineScheme();
    }
    
    // ============ REPORTING ============
    
    public String getOccupancyReport() {
        double rate = parkingLot.getOccupancyRate();
        int total = parkingLot.getTotalSpots();
        int occupied = parkingLot.getOccupiedSpots();
        
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("                    OCCUPANCY REPORT\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append("Total Spots: ").append(total).append("\n");
        sb.append("Occupied: ").append(occupied).append("\n");
        sb.append("Available: ").append(total - occupied).append("\n");
        sb.append("Occupancy Rate: ").append(String.format("%.1f", rate)).append("%\n");
        sb.append("=".repeat(60)).append("\n");
        
        return sb.toString();
    }
    
    public String getRevenueReport() {
        double revenue = parkingLot.getTotalRevenue();
        double dbRevenue = dbManager.getTotalRevenue();
        
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("                    REVENUE REPORT\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append("Total Revenue: RM").append(String.format("%.2f", revenue)).append("\n");
        sb.append("(Database): RM").append(String.format("%.2f", dbRevenue)).append("\n");
        sb.append("=".repeat(60)).append("\n");
        
        return sb.toString();
    }
    
    public String getFineReport() {
        Map<String, List<Fine>> unpaidFines = dbManager.getAllUnpaidFines();
        
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("                    FINE REPORT\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append("Current Fine Scheme: ").append(parkingLot.getFineScheme().getName()).append("\n");
        sb.append("-".repeat(60)).append("\n");
        
        if (unpaidFines.isEmpty()) {
            sb.append("No outstanding fines.\n");
        } else {
            sb.append("Outstanding Fines:\n\n");
            double totalOutstanding = 0;
            
            for (Map.Entry<String, List<Fine>> entry : unpaidFines.entrySet()) {
                sb.append("License Plate: ").append(entry.getKey()).append("\n");
                for (Fine fine : entry.getValue()) {
                    sb.append("  • ").append(fine.getReason())
                      .append(": RM").append(String.format("%.2f", fine.getAmount()))
                      .append("\n");
                    totalOutstanding += fine.getAmount();
                }
                sb.append("\n");
            }
            sb.append("-".repeat(60)).append("\n");
            sb.append("Total Outstanding: RM").append(String.format("%.2f", totalOutstanding)).append("\n");
        }
        
        sb.append("=".repeat(60)).append("\n");
        return sb.toString();
    }
    
    public String getCurrentVehiclesReport() {
        List<ParkingSpot> occupiedSpots = new ArrayList<>();
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            if (spot.getStatus() == SpotStatus.OCCUPIED) {
                occupiedSpots.add(spot);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("              VEHICLES CURRENTLY PARKED\n");
        sb.append("=".repeat(60)).append("\n");
        
        if (occupiedSpots.isEmpty()) {
            sb.append("No vehicles currently parked.\n");
        } else {
            for (ParkingSpot spot : occupiedSpots) {
                Vehicle v = spot.getCurrentVehicle();
                sb.append("Spot: ").append(spot.getSpotId()).append("\n");
                sb.append("  Plate: ").append(v.getLicensePlate()).append("\n");
                sb.append("  Type: ").append(v.getVehicleType()).append("\n");
                sb.append("  Entry: ").append(v.getEntryTime()).append("\n");
                sb.append("-".repeat(40)).append("\n");
            }
        }
        
        sb.append("=".repeat(60)).append("\n");
        return sb.toString();
    }
    
    public ParkingLot getParkingLot() {
        return parkingLot;
    }
}