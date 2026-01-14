package trafficsimulation.export;

import trafficsimulation.stats.EdgeStatsSnapshot;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class EdgeCsvStatsExporter {

    public void export(List<EdgeStatsSnapshot> snapshots, String path) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write("step,edgeId,vehicleCount\n");

            for (EdgeStatsSnapshot s : snapshots) {
                writer.write(
                        s.getSimulationStep() + "," +
                                s.getEdgeId() + "," +
                                s.getVehicleCount() + "\n"
                );
            }
        }
    }
}
