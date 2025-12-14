package trafficsimulation.stats;

public class SimulationStatsSnapshot {
    private final long simulationStep;
    private final double averageSpeed;
    private final double vehicleDensity;
    private final int vehicleCount;

    public SimulationStatsSnapshot(long simulationStep,
                                   double averageSpeed,
                                   double vehicleDensity,
                                   int vehicleCount) {
        this.simulationStep = simulationStep;
        this.averageSpeed = averageSpeed;
        this.vehicleDensity = vehicleDensity;
        this.vehicleCount = vehicleCount;
    }

    public long getSimulationStep() {
        return simulationStep;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public double getVehicleDensity() {
        return vehicleDensity;
    }

    public int getVehicleCount() {
        return vehicleCount;
    }
}
