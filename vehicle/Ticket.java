package vehicle;

import model.ParkingSpot;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    private String ticketId;
    private Vehicle vehicle;
    private ParkingSpot spot;
    private LocalDateTime entryTime;
    private boolean isActive;
    
    public Ticket(Vehicle vehicle, ParkingSpot spot) {
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = vehicle.getEntryTime();
        this.isActive = true;
        this.ticketId = generateTicketId();
    }
    
    private String generateTicketId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String timestamp = entryTime.format(formatter);
        return "T-" + vehicle.getLicensePlate() + "-" + timestamp;
    }
    
    public String getTicketId() {
        return ticketId;
    }
    
    public Vehicle getVehicle() {
        return vehicle;
    }
    
    public ParkingSpot getSpot() {
        return spot;
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    @Override
    public String toString() {
        return "Ticket ID: " + ticketId + "\n" +
               "License Plate: " + vehicle.getLicensePlate() + "\n" +
               "Spot: " + spot.getSpotId() + "\n" +
               "Entry Time: " + entryTime.toString();
    }
}