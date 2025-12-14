package com.team.trafficsimulation.app;

import com.team.trafficsimulation.Vehicle;
import com.team.trafficsimulation.stats.StatsManager;
import com.team.trafficsimulation.stats.SimulationStatsSnapshot;

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

        simulationStep++;
        return snapshot;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }
}
