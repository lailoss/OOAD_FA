package model;

import java.util.*;

public class Floor {
    private int floorNumber;
    private Map<Integer, List<ParkingSpot>> rows;
    private int totalSpots;
    
    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.rows = new HashMap<>();
        this.totalSpots = 0;
    }
    
    public void addRow(int rowNumber, List<ParkingSpot> spots) {
        rows.put(rowNumber, spots);
        totalSpots += spots.size();
    }
    
    public List<ParkingSpot> getSpots() {
        List<ParkingSpot> allSpots = new ArrayList<>();
        for (List<ParkingSpot> rowSpots : rows.values()) {
            allSpots.addAll(rowSpots);
        }
        return allSpots;
    }
    
    public List<ParkingSpot> getSpotsByRow(int rowNumber) {
        return rows.getOrDefault(rowNumber, new ArrayList<>());
    }
    
    public int getFloorNumber() {
        return floorNumber;
    }
    
    public int getAvailableCount() {
        int count = 0;
        for (ParkingSpot spot : getSpots()) {
            if (spot.isAvailable()) {
                count++;
            }
        }
        return count;
    }
    
    public int getOccupiedCount() {
        int count = 0;
        for (ParkingSpot spot : getSpots()) {
            if (spot.getStatus() == SpotStatus.OCCUPIED) {
                count++;
            }
        }
        return count;
    }
    
    public int getTotalSpots() {
        return totalSpots;
    }
}