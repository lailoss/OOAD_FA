package model;

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
        for (int floor = 1; floor <= 5; floor++) {
            Floor newFloor = new Floor(floor);
            
            for (int row = 1; row <= 3; row++) {
                List<ParkingSpot> rowSpots = new ArrayList<>();
                
                for (int spot = 1; spot <= 10; spot++) {
                    String spotId = "F" + floor + "-R" + row + "-S" + spot;
                    ParkingSpotType type;
                    double rate;
                    
                    // Assign spot types based on position
                    if (floor == 1 && row == 1 && spot <= 2) {
                        type = ParkingSpotType.HANDICAPPED;
                        rate = 2.0;
                    } else if (floor == 5 && row == 3 && spot >= 8) {
                        type = ParkingSpotType.RESERVED;
                        rate = 10.0;
                    } else if (spot <= 3) {
                        type = ParkingSpotType.COMPACT;
                        rate = 2.0;
                    } else {
                        type = ParkingSpotType.REGULAR;
                        rate = 5.0;
                    }
                    
                    ParkingSpot newSpot = new ParkingSpot(spotId, type, rate, floor, row);
                    rowSpots.add(newSpot);
                }
                
                newFloor.addRow(row, rowSpots);
            }
            
            this.floors.add(newFloor);
        }
        
        System.out.println("✅ Parking lot initialized: 5 floors, 3 rows, 10 spots per row");
        System.out.println("✅ Total spots: " + this.getTotalSpots());
    }
    
    public void addFloor(Floor floor) {
        this.floors.add(floor);
    }
    
    public Floor getFloor(int floorNumber) {
        for (Floor floor : this.floors) {
            if (floor.getFloorNumber() == floorNumber) {
                return floor;
            }
        }
        return null;
    }
    
    public List<ParkingSpot> getAllSpots() {
        List<ParkingSpot> allSpots = new ArrayList<>();
        for (Floor floor : this.floors) {
            allSpots.addAll(floor.getSpots());
        }
        return allSpots;
    }
    
    /**
     * FIXED: Now ALL vehicle types can see RESERVED spots!
     */
    public List<ParkingSpot> getAvailableSpotsByVehicle(VehicleType vehicleType) {
        List<ParkingSpot> available = new ArrayList<>();
        
        for (ParkingSpot spot : this.getAllSpots()) {
            if (spot.isAvailable()) {
                switch (vehicleType) {
                    case MOTORCYCLE:
                        if (spot.getType() == ParkingSpotType.COMPACT ||
                            spot.getType() == ParkingSpotType.RESERVED) {
                            available.add(spot);
                        }
                        break;
                        
                    case CAR:
                        if (spot.getType() == ParkingSpotType.COMPACT || 
                            spot.getType() == ParkingSpotType.REGULAR ||
                            spot.getType() == ParkingSpotType.RESERVED) {
                            available.add(spot);
                        }
                        break;
                        
                    case SUV:
                        if (spot.getType() == ParkingSpotType.REGULAR ||
                            spot.getType() == ParkingSpotType.RESERVED) {
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
        for (ParkingSpot spot : this.getAllSpots()) {
            if (spot.getSpotId().equals(spotId)) {
                return spot;
            }
        }
        return null;
    }
    
    public double getOccupancyRate() {
        int total = this.getAllSpots().size();
        int occupied = 0;
        
        for (ParkingSpot spot : this.getAllSpots()) {
            if (spot.getStatus() == SpotStatus.OCCUPIED) {
                occupied++;
            }
        }
        
        return total > 0 ? (occupied * 100.0 / total) : 0.0;
    }
    
    public int getTotalSpots() {
        return this.getAllSpots().size();
    }
    
    public int getOccupiedSpots() {
        int occupied = 0;
        for (ParkingSpot spot : this.getAllSpots()) {
            if (spot.getStatus() == SpotStatus.OCCUPIED) {
                occupied++;
            }
        }
        return occupied;
    }
    
    public double getTotalRevenue() {
        return this.totalRevenue;
    }
    
    public void addRevenue(double amount) {
        this.totalRevenue += amount;
    }
    
    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public void setFineScheme(FineScheme scheme) {
        this.fineScheme = scheme;
        System.out.println("✅ Fine scheme changed to: " + scheme.getName());
    }
    
    public FineScheme getFineScheme() {
        if (this.fineScheme == null) {
            this.fineScheme = new FixedFineScheme();
        }
        return this.fineScheme;
    }
}