package trafficsimulation.stats;

public class SimulationStatsSnapshot {

    private final long simulationStep;    // Simulation time step or tick
    private final double averageSpeed;    // Average speed of all vehicles
    private final double vehicleDensity;  // Vehicle density in the simulation
    private final int vehicleCount;       // Total number of vehicles

    public SimulationStatsSnapshot(long simulationStep,
                                   double averageSpeed,
                                   double vehicleDensity,
                                   int vehicleCount) {
        this.simulationStep = simulationStep;
        this.averageSpeed = averageSpeed;
        this.vehicleDensity = vehicleDensity;
        this.vehicleCount = vehicleCount;
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
}
