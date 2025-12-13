package com.team.trafficsimulation.app;

import java.util.logging.Logger;
import com.team.trafficsimulation.stats.StatsManager;
import com.team.trafficsimulation.stats.SimulationStatsSnapshot;

public class SimulationController {
    private final StatsManager statsManager = new StatsManager();

    // z.B. später in der Simulationsschleife:
    private void collectStatsDummy() {
        // Dummywerte – später durch echte Werte ersetzen
        long step = 0L;
        double avgSpeed = 0.0;
        double density = 0.0;
        int vehicleCount = 0;

        SimulationStatsSnapshot snapshot =
                new SimulationStatsSnapshot(step, avgSpeed, density, vehicleCount);

        statsManager.addSnapshot(snapshot);
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }
}
