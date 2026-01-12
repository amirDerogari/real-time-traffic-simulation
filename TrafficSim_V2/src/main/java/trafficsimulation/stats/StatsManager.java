package trafficsimulation.stats;

import trafficsimulation.Vehicle;

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
        double minSpeed = 0.0;     // Minimum speed of vehicles
        double maxSpeed = 0.0;     // Maximum speed of vehicles

        if (vehicleCount > 0) {
            double speedSum = 0.0; // Sum of speeds
            minSpeed = Double.MAX_VALUE; // Start with very high value

            for (Vehicle v : vehicles) {
                double speed = v.getSpeed(); // Read live speed from SUMO
                speedSum += speed;

                if (speed < minSpeed) {
                    minSpeed = speed;
                }
                if (speed > maxSpeed) {
                    maxSpeed = speed;
                }
            }
            averageSpeed = speedSum / vehicleCount; // Calculate average speed
        }

        double density = vehicleCount;

        SimulationStatsSnapshot snapshot =
                new SimulationStatsSnapshot(
                        simulationStep,
                        averageSpeed,
                        density,
                        vehicleCount,
                        minSpeed,
                        maxSpeed
                );

        history.add(snapshot); // Store snapshot in history
        return snapshot; // Return snapshot
    }


    public List<SimulationStatsSnapshot> getHistory() {
        return Collections.unmodifiableList(history); // Return read-only history
    }

    public Map<String, Integer> calculateDensityPerEdge(List<Vehicle> vehicles) {

        Map<String, Integer> densityPerEdge = new HashMap<>();

        for (Vehicle v : vehicles) {
            String edgeId = v.getEdgeId(); // Current edge of vehicle

            if (edgeId == null || edgeId.isEmpty()) {
                continue; // Skip invalid edge IDs
            }

            int count;
            if(densityPerEdge.containsKey(edgeId)) {
                count = densityPerEdge.get(edgeId);
            } else {
                count= 0;
            }
            densityPerEdge.put(edgeId, count+1);
        }

        return densityPerEdge; // Return density per edge
    }
}
