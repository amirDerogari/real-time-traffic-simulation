package trafficsimulation.stats;

import trafficsimulation.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * StatsManager is responsible for calculating simulation statistics.
 *
 * It computes:
 * - average speed
 * - minimum and maximum speed
 * - vehicle count
 * - vehicle density per edge
 *
 * It also stores a history of all generated statistic snapshots.
 */


public class StatsManager {

    private final List<SimulationStatsSnapshot> history = new ArrayList<>();

    /**
     * Calculates statistics for a given simulation step.
     *
     * @param vehicles list of all vehicles
     * @param simulationStep current simulation step
     * @return a statistics snapshot for this step
     */


    public SimulationStatsSnapshot collectStats(
            List<Vehicle> vehicles,
            long simulationStep
    ) {
        int vehicleCount = vehicles.size();

        double averageSpeed = 0.0;
        double minSpeed = 0.0;
        double maxSpeed = 0.0;

        if (vehicleCount > 0) {
            double speedSum = 0.0;
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
            averageSpeed = speedSum / vehicleCount;
        }



        SimulationStatsSnapshot snapshot =
                new SimulationStatsSnapshot(
                        simulationStep,
                        averageSpeed,
                        vehicleCount,
                        minSpeed,
                        maxSpeed
                );

        history.add(snapshot); // Store snapshot in history
        return snapshot;
    }

    /**
     * Returns an unmodifiable view of the collected statistics history.
     *
     * @return read-only list of statistic snapshots
     */


    public List<SimulationStatsSnapshot> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * Calculates how many vehicles are present on each edge.
     *
     * @param vehicles list of all vehicles
     * @return a map with edge IDs as keys and vehicle counts as values
     */

    public Map<String, Integer> calculateDensityPerEdge(List<Vehicle> vehicles) {

        Map<String, Integer> densityPerEdge = new HashMap<>();

        for (Vehicle v : vehicles) {
            String edgeId = v.getEdgeId();


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

        return densityPerEdge;
    }

    public double getOverallAverageSpeed() {
        if (history.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (SimulationStatsSnapshot s : history) {
            sum += s.getAverageSpeed();
        }
        return sum / history.size();
    }

}
