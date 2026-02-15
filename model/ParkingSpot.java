package model;

import vehicle.Vehicle;

public class ParkingSpot {
    private String spotId;
    private ParkingSpotType type;
    private SpotStatus status;
    private Vehicle currentVehicle;
    private double hourlyRate;
    private int floorNum;
    private int rowNum;
    
    public ParkingSpot(String spotId, ParkingSpotType type, double hourlyRate, int floorNum, int rowNum) {
        this.spotId = spotId;
        this.type = type;
        this.hourlyRate = hourlyRate;
        this.floorNum = floorNum;
        this.rowNum = rowNum;
        this.status = SpotStatus.AVAILABLE;
        this.currentVehicle = null;
    }
    
    public void assignVehicle(Vehicle vehicle) {
        this.currentVehicle = vehicle;
        this.status = SpotStatus.OCCUPIED;
    }
    
    public Vehicle removeVehicle() {
        Vehicle vehicle = this.currentVehicle;
        this.currentVehicle = null;
        this.status = SpotStatus.AVAILABLE;
        return vehicle;
    }
    
    public boolean isAvailable() {
        return status == SpotStatus.AVAILABLE;
    }
    
    public String getSpotId() {
        return spotId;
    }
    
    public ParkingSpotType getType() {
        return type;
    }
    
    public double getHourlyRate() {
        return hourlyRate;
    }
    
    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }
    
    public int getFloorNum() {
        return floorNum;
    }
    
    public int getRowNum() {
        return rowNum;
    }
    
    public SpotStatus getStatus() {
        return status;
    }
    
    public void setStatus(SpotStatus status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return spotId + " (" + type + ") - RM" + String.format("%.2f", hourlyRate) + "/hr";
    }
}