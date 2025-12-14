package trafficsimulation.app; // Application controller package

import trafficsimulation.Vehicle; // Vehicle wrapper
import trafficsimulation.stats.StatsManager; // Statistics manager
import trafficsimulation.stats.SimulationStatsSnapshot; // Stats snapshot

import java.util.List;
import java.util.Map;

public class SimulationController { // Controls simulation statistics flow

    private final StatsManager statsManager = new StatsManager(); // Statistics handler
    private long simulationStep = 0; // Simulation time step counter

    public SimulationStatsSnapshot collectStats(List<Vehicle> vehicles) {

        Map<String, Integer> densityPerEdge =
                statsManager.calculateDensityPerEdge(vehicles); // Density per edge

        SimulationStatsSnapshot snapshot =
                statsManager.collectStats(vehicles, simulationStep); // Global stats

        System.out.println(
                "Step " + simulationStep +
                        " | AvgSpeed: " + snapshot.getAverageSpeed() +
                        " | Vehicles: " + snapshot.getVehicleCount()
        ); // Console debug output

        for (Map.Entry<String, Integer> entry : densityPerEdge.entrySet()) {
            System.out.println(
                    "   Edge " + entry.getKey() + ": " + entry.getValue()
            ); // Print edge density
        }

        simulationStep++; // Increase simulation step
        return snapshot; // Return statistics snapshot
    }

    public StatsManager getStatsManager() {
        return statsManager; // Provide access to statistics manager
    }
}
