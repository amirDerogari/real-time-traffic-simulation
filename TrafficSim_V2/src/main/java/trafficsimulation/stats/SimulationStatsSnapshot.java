package trafficsimulation.stats;

public class SimulationStatsSnapshot {

    private final long simulationStep;    // Simulation time step or tick
    private final double averageSpeed;    // Average speed of all vehicles
    private final double vehicleDensity;  // Vehicle density in the simulation
    private final int vehicleCount;       // Total number of vehicles
    private final double minSpeed;
    private final double maxSpeed;

    public SimulationStatsSnapshot(long simulationStep,
                                   double averageSpeed,
                                   double vehicleDensity,
                                   int vehicleCount,
                                   double minSpeed,
                                   double maxSpeed) {
        this.simulationStep = simulationStep;
        this.averageSpeed = averageSpeed;
        this.vehicleDensity = vehicleDensity;
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

    public double getVehicleDensity() {
        return vehicleDensity;
    }

    public int getVehicleCount() { // Returns the number of vehicles
        return vehicleCount;
    }

    public double getMinSpeed() {return minSpeed;}

    public double getMaxSpeed() {return maxSpeed;}
}
