package trafficsimulation.stats;

/**
 * VehiclePositionSnapshot represents the state of a single vehicle
 * at a specific simulation step.
 *
 * It stores:
 * - simulation step
 * - vehicle ID
 * - current edge ID
 * - current speed
 */

public class VehiclePositionSnapshot {

    private final long step;
    private final String vehicleId;
    private final String edgeId;
    private final double speed;

    public VehiclePositionSnapshot(long step, String vehicleId, String edgeId, double speed) {
        this.step = step;
        this.vehicleId = vehicleId;
        this.edgeId = edgeId;
        this.speed = speed;
    }

    public long getStep() {
        return step;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getEdgeId() {
        return edgeId;
    }

    public double getSpeed() {
        return speed;
    }
}
