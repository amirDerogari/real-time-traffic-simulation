package trafficsimulation.stats;

/**
 * SimulationStatsSnapshot represents an immutable snapshot
 * of simulation statistics at a specific simulation step.
 *
 * It stores aggregated values like:
 * - average speed
 * - vehicle count
 * - minimum speed
 * - maximum speed
 */

public class SimulationStatsSnapshot {

    private final long simulationStep;
    private final double averageSpeed;
    private final int vehicleCount;
    private final double minSpeed;
    private final double maxSpeed;

    public SimulationStatsSnapshot(long simulationStep,
                                   double averageSpeed,
                                   int vehicleCount,
                                   double minSpeed,
                                   double maxSpeed) {
        this.simulationStep = simulationStep;
        this.averageSpeed = averageSpeed;
        this.vehicleCount = vehicleCount;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
    }

    public long getSimulationStep() { // Returns the simulation step
        return simulationStep;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public int getVehicleCount() { // Returns the number of vehicles
        return vehicleCount;
    }

    public double getMinSpeed() {return minSpeed;}

    public double getMaxSpeed() {return maxSpeed;}
}
