package trafficsimulation.app;

import trafficsimulation.Vehicle;
import trafficsimulation.stats.StatsManager;
import trafficsimulation.stats.SimulationStatsSnapshot;
import trafficsimulation.util.AppLogger;


import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SimulationController {

    private static final Logger LOG =
            AppLogger.getLogger(SimulationController.class);

    private final StatsManager statsManager = new StatsManager(); // Statistics handler
    private long simulationStep = 0; // Simulation time step counter


    public SimulationStatsSnapshot collectStats(List<Vehicle> vehicles) {

        // Calculate density per edge
        Map<String, Integer> densityPerEdge =
                statsManager.calculateDensityPerEdge(vehicles);

        // Collect global statistics
        SimulationStatsSnapshot snapshot =
                statsManager.collectStats(vehicles, simulationStep);


        LOG.info(
                "Step " + simulationStep +
                        " | AvgSpeed: " + snapshot.getAverageSpeed() +
                        " | Vehicles: " + snapshot.getVehicleCount()
        );

        for (Map.Entry<String, Integer> entry : densityPerEdge.entrySet()) {
            LOG.fine(
                    "Edge " + entry.getKey() +
                            " | Vehicle count: " + entry.getValue()
            );
        }

        simulationStep++; // Increase simulation step
        return snapshot;
    }


    public StatsManager getStatsManager() {
        return statsManager;
    }
}
