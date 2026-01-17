package trafficsimulation.export;

import trafficsimulation.stats.VehiclePositionSnapshot;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class VehiclePositionCsvExporter {

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
