package model;

import vehicle.Vehicle;
import vehicle.VehicleType;
import payment.FineScheme;
import payment.FixedFineScheme;
import java.util.*;

public class ParkingLot {
    private List<Floor> floors;
    private double totalRevenue;
    private FineScheme fineScheme;
    private static ParkingLot instance;
    
    private ParkingLot() {
        this.floors = new ArrayList<>();
        this.totalRevenue = 0.0;
    }
    
    public static synchronized ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }
    
    public void initializeDefault() {
        for (int f = 1; f <= 5; f++) {
            Floor floor = new Floor(f);
            
            for (int r = 1; r <= 3; r++) {
                List<ParkingSpot> rowSpots = new ArrayList<>();
                
                for (int s = 1; s <= 10; s++) {
                    String spotId = "F" + f + "-R" + r + "-S" + s;
                    ParkingSpotType type;
                    double rate;
                    
                    if (f == 1 && r == 1 && s <= 2) {
                        type = ParkingSpotType.HANDICAPPED;
                        rate = 2.0;
                    } else if (f == 5 && r == 3 && s >= 8) {
                        type = ParkingSpotType.RESERVED;
                        rate = 10.0;
                    } else if (s <= 3) {
                        type = ParkingSpotType.COMPACT;
                        rate = 2.0;
                    } else {
                        type = ParkingSpotType.REGULAR;
                        rate = 5.0;
                    }
                    
                    ParkingSpot spot = new ParkingSpot(spotId, type, rate, f, r);
                    rowSpots.add(spot);
                }
                
                floor.addRow(r, rowSpots);
            }
            
            floors.add(floor);
        }
        System.out.println("✅ Parking lot initialized: 5 floors, 3 rows, 10 spots per row");
        System.out.println("✅ Total spots: " + getTotalSpots());
    }
    
    public void addFloor(Floor floor) {
        floors.add(floor);
    }
    
    public Floor getFloor(int floorNumber) {
        for (Floor floor : floors) {
            if (floor.getFloorNumber() == floorNumber) {
                return floor;
            }
        }
        return null;
    }
    
    public List<ParkingSpot> getAllSpots() {
        List<ParkingSpot> allSpots = new ArrayList<>();
        for (Floor floor : floors) {
            allSpots.addAll(floor.getSpots());
        }
        return allSpots;
    }
    
    public List<ParkingSpot> getAvailableSpotsByVehicle(VehicleType vehicleType) {
        List<ParkingSpot> available = new ArrayList<>();
        
        for (ParkingSpot spot : getAllSpots()) {
            if (spot.isAvailable()) {
                switch (vehicleType) {
                    case MOTORCYCLE:
                        if (spot.getType() == ParkingSpotType.COMPACT) {
                            available.add(spot);
                        }
                        break;
                    case CAR:
                        if (spot.getType() == ParkingSpotType.COMPACT || 
                            spot.getType() == ParkingSpotType.REGULAR) {
                            available.add(spot);
                        }
                        break;
                    case SUV:
                        if (spot.getType() == ParkingSpotType.REGULAR) {
                            available.add(spot);
                        }
                        break;
                    case HANDICAPPED:
                        available.add(spot);
                        break;
                }
            }
        }
        
        return available;
    }
    
    public ParkingSpot getSpot(String spotId) {
        for (ParkingSpot spot : getAllSpots()) {
            if (spot.getSpotId().equals(spotId)) {
                return spot;
            }
        }
        return null;
    }
    
    public double getOccupancyRate() {
        int total = getAllSpots().size();
        int occupied = 0;
        
        for (ParkingSpot spot : getAllSpots()) {
            if (spot.getStatus() == SpotStatus.OCCUPIED) {
                occupied++;
            }
        }
        
        return total > 0 ? (occupied * 100.0 / total) : 0;
    }
    
    public int getTotalSpots() {
        return getAllSpots().size();
    }
    
    public int getOccupiedSpots() {
        int occupied = 0;
        for (ParkingSpot spot : getAllSpots()) {
            if (spot.getStatus() == SpotStatus.OCCUPIED) {
                occupied++;
            }
        }
        return occupied;
    }
    
    public double getTotalRevenue() {
        return totalRevenue;
    }
    
    public void addRevenue(double amount) {
        this.totalRevenue += amount;
    }
    
    public void setFineScheme(FineScheme scheme) {
        this.fineScheme = scheme;
        System.out.println("✅ Fine scheme changed to: " + scheme.getName());
    }
    
    public FineScheme getFineScheme() {
        if (fineScheme == null) {
            fineScheme = new FixedFineScheme();
        }
        return fineScheme;
    }

    // In ParkingLot.java, add this method:
    public void setTotalRevenue(double revenue) {
        this.totalRevenue = revenue;
    }
}