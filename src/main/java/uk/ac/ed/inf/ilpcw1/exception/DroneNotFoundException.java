package uk.ac.ed.inf.ilpcw1.exception;

public class DroneNotFoundException extends RuntimeException {
    public DroneNotFoundException(String message) {
        super(message);
    }
}
