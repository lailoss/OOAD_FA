package vehicle;

import model.ParkingSpotType;
import java.time.LocalDateTime;
import java.time.Duration;

public abstract class Vehicle {
    protected String licensePlate;
    protected VehicleType vehicleType;
    protected LocalDateTime entryTime;
    protected LocalDateTime exitTime;
    protected boolean hasHandicappedCard;
    
    public Vehicle(String licensePlate, VehicleType type, boolean hasHandicappedCard) {
        this.licensePlate = licensePlate;
        this.vehicleType = type;
        this.hasHandicappedCard = hasHandicappedCard;
        this.entryTime = null;
        this.exitTime = null;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public VehicleType getVehicleType() {
        return vehicleType;
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }
    
    public LocalDateTime getExitTime() {
        return exitTime;
    }
    
    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }
    
    public boolean hasHandicappedCard() {
        return hasHandicappedCard;
    }
    
    public long calculateDuration() {
        if (entryTime == null || exitTime == null) {
            return 0;
        }
        
        Duration duration = Duration.between(entryTime, exitTime);
        long minutes = duration.toMinutes();
        long hours = minutes / 60;
        
        if (minutes % 60 > 0) {
            hours++;
        }
        
        return hours;
    }
    
    public abstract boolean canParkIn(ParkingSpotType spotType);
}