package com.team.trafficsimulation.stats;

import com.team.trafficsimulation.Vehicle; // Vehicle wrapper

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class StatsManager { // Manages calculation and storage of statistics

    private final List<SimulationStatsSnapshot> history = new ArrayList<>(); // Statistics history

    public SimulationStatsSnapshot collectStats(
            List<Vehicle> vehicles,
            long simulationStep
    ) {
        int vehicleCount = vehicles.size(); // Number of vehicles

        double averageSpeed = 0.0; // Default average speed
        if (vehicleCount > 0) {
            double speedSum = 0.0;
            for (Vehicle v : vehicles) {
                speedSum += v.getSpeed(); // Read live speed from SUMO
            }
            averageSpeed = speedSum / vehicleCount; // Calculate average speed
        }

        double density = vehicleCount; // Simple density

        SimulationStatsSnapshot snapshot =
                new SimulationStatsSnapshot(
                        simulationStep,
                        averageSpeed,
                        density,
                        vehicleCount
                );

        history.add(snapshot); // Store snapshot in history
        return snapshot;
    }

    public List<SimulationStatsSnapshot> getHistory() {
        return Collections.unmodifiableList(history); // Return read-only history
    }

    public Map<String, Integer> calculateDensityPerEdge(List<Vehicle> vehicles) {

        Map<String, Integer> densityPerEdge = new HashMap<>(); // Edge → count

        for (Vehicle v : vehicles) {
            String edgeId = v.getEdgeId(); // Current edge of vehicle

            if (edgeId == null || edgeId.isEmpty()) {
                continue; // Skip invalid edge IDs
            }

            densityPerEdge.put(
                    edgeId,
                    densityPerEdge.getOrDefault(edgeId, 0) + 1
            );
        }

        return densityPerEdge;
    }
}
