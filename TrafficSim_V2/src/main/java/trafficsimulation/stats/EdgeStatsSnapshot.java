package trafficsimulation.stats;

public class EdgeStatsSnapshot {

    private final long simulationStep;
    private final String edgeId;
    private final int vehicleCount;

    public EdgeStatsSnapshot(long simulationStep, String edgeId, int vehicleCount) {
        this.simulationStep = simulationStep;
        this.edgeId = edgeId;
        this.vehicleCount = vehicleCount;
    }

    public long getSimulationStep() {
        return simulationStep;
    }

    public String getEdgeId() {
        return edgeId;
    }

    public int getVehicleCount() {
        return vehicleCount;
    }
}
