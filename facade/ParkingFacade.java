package facade;

import model.*;
import vehicle.*;
import payment.*;
import database.DatabaseManager;
import java.time.LocalDateTime;
import java.util.*;

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
        System.out.println("✅ Parking System Initialized with MySQL");
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
        long hours = vehicle.calculateDuration();
        
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
        
        if (occupiedSpot.getType() == ParkingSpotType.RESERVED) {
            Fine reservedFine = new Fine(licensePlate, 100.0, 
                "Unauthorized use of reserved spot");
            dbManager.saveFine(reservedFine);
            unpaidFines.add(reservedFine);
            totalFines += 100.0;
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
        
        return receipt;
    }
    
    /**
 * Loads all currently parked vehicles from the database into the in‑memory parking lot.
 * This is called at application startup when a database connection is available.
 */
    // public void loadParkedVehiclesFromDB() {
    //     // Retrieve all occupied spots from the database (you need to implement this in DatabaseManager)
    //     List<ParkingSpot> occupiedSpots = dbManager.getOccupiedSpots();

    //     for (ParkingSpot spot : occupiedSpots) {
    //         // The spot already has its currentVehicle set from the database query
    //         // Add it to the parking lot's internal list of spots (or update existing spot)
    //         ParkingSpot existingSpot = parkingLot.getSpot(spot.getSpotId());
    //         if (existingSpot != null) {
    //             // Update the existing spot with the occupied status and vehicle
    //             existingSpot.assignVehicle(spot.getCurrentVehicle());
    //             existingSpot.setStatus(SpotStatus.OCCUPIED);
    //         } else {
    //             // If the spot wasn't in the parking lot, add it (should not happen in normal setup)
    //             parkingLot.addSpot(spot);
    //         }
    //     }

    //     System.out.println("✅ Loaded " + occupiedSpots.size() + " currently parked vehicles from database.");
    // }

    public void loadParkedVehiclesFromDB() {
    List<ParkingSpot> occupiedSpots = dbManager.getOccupiedSpots();
    for (ParkingSpot spot : occupiedSpots) {
        ParkingSpot existingSpot = parkingLot.getSpot(spot.getSpotId());
        if (existingSpot != null) {
            existingSpot.assignVehicle(spot.getCurrentVehicle());
            existingSpot.setStatus(SpotStatus.OCCUPIED);
        } else {
            System.err.println("Warning: Spot " + spot.getSpotId() + " not found in parking lot.");
        }
    }
    System.out.println("✅ Loaded " + occupiedSpots.size() + " currently parked vehicles from database.");
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