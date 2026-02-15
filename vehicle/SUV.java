package vehicle;

import model.ParkingSpotType;

public class SUV extends Vehicle {
    
    public SUV(String licensePlate, boolean hasHandicappedCard) {
        super(licensePlate, VehicleType.SUV, hasHandicappedCard);
    }
    
    @Override
    public boolean canParkIn(ParkingSpotType spotType) {
        return spotType == ParkingSpotType.REGULAR;
    }
}