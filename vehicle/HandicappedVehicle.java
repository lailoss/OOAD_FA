package vehicle;

import model.ParkingSpotType;

public class HandicappedVehicle extends Vehicle {
    
    public HandicappedVehicle(String licensePlate, VehicleType type) {
        super(licensePlate, type, true);
    }
    
    @Override
    public boolean canParkIn(ParkingSpotType spotType) {
        return true;
    }
    
    public double getDiscountedRate(double originalRate) {
        return 2.0;
    }
}