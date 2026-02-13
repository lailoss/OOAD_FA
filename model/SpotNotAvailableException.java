package model;

public class SpotNotAvailableException extends Exception {
    public SpotNotAvailableException(String spotId) {
        super("Spot " + spotId + " is not available");
    }
}