package vehicle;

import model.ParkingSpotType;

public class Car extends Vehicle {
    
    public Car(String licensePlate, boolean hasHandicappedCard) {
        super(licensePlate, VehicleType.CAR, hasHandicappedCard);
    }
    
    @Override
    public boolean canParkIn(ParkingSpotType spotType) {
        return spotType == ParkingSpotType.COMPACT || 
               spotType == ParkingSpotType.REGULAR;
    }
}