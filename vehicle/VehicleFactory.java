package vehicle;

public class VehicleFactory {
    
    public static Vehicle createVehicle(String licensePlate, VehicleType type, boolean hasHandicappedCard) {
        switch (type) {
            case MOTORCYCLE:
                return new Motorcycle(licensePlate, hasHandicappedCard);
            case CAR:
                return new Car(licensePlate, hasHandicappedCard);
            case SUV:
                return new SUV(licensePlate, hasHandicappedCard);
            case HANDICAPPED:
                return new HandicappedVehicle(licensePlate, type);
            default:
                throw new IllegalArgumentException("Unknown vehicle type: " + type);
        }
    }
}