package trafficsimulation;


/**
 * Error to handle when several emergency vehicle are tried to spawn on the first edge: EM_IN
 */
class EmergencyDistanceException extends Exception {
    public EmergencyDistanceException(String message) {
        super(message);
    }
}
