package trafficsimulation.export;

import trafficsimulation.stats.VehiclePositionSnapshot;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * VehiclePositionCsvExporter exports vehicle position data
 * into a CSV file.
 *
 * Each row represents the state of a vehicle
 * at a specific simulation step.
 */

public class VehiclePositionCsvExporter {

    /**
     * Exports vehicle position snapshots to a CSV file.
     *
     * @param snapshots list of vehicle position snapshots
     * @param path output file path
     * @throws IOException if writing fails
     */

    public void export(List<VehiclePositionSnapshot> snapshots, String path) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write("step,vehicleId,edgeId,speed\n");

            for (VehiclePositionSnapshot s : snapshots) {
                writer.write(
                        s.getStep() + "," +
                                s.getVehicleId() + "," +
                                s.getEdgeId() + "," +
                                s.getSpeed() + "\n"
                );
            }
        }
    }
}
