package vehicle;

import model.ParkingSpotType;

public class Motorcycle extends Vehicle {
    
    public Motorcycle(String licensePlate, boolean hasHandicappedCard) {
        super(licensePlate, VehicleType.MOTORCYCLE, hasHandicappedCard);
    }
    
    @Override
    public boolean canParkIn(ParkingSpotType spotType) {
        return spotType == ParkingSpotType.COMPACT;
    }
}