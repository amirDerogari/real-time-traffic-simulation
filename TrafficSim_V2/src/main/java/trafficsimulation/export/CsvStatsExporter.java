package trafficsimulation.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import trafficsimulation.stats.SimulationStatsSnapshot;
import trafficsimulation.util.AppLogger;

public class CsvStatsExporter implements StatsExporter {

    private static final Logger LOG =
            AppLogger.getLogger(CsvStatsExporter.class); // Logger for this class (used to log info, warnings, and errors)

    @Override
    public void export(List<SimulationStatsSnapshot> snapshots,
                       String targetPath) throws IOException {
        


        LOG.info("CSV-Export gestartet: " + targetPath); // Log start

        try (FileWriter writer = new FileWriter(targetPath)) { // Auto-close writer

            writer.write("step,averageSpeed,vehicleDensity,vehicleCount\n"); // CSV header

            for (SimulationStatsSnapshot snapshot : snapshots) { // Iterate snapshots
                String line = snapshot.getSimulationStep() + "," +
                        snapshot.getAverageSpeed() + "," +
                        snapshot.getVehicleDensity() + "," +
                        snapshot.getVehicleCount() + "\n"; // Build CSV row
                writer.write(line); // Write row
            }
        }

        LOG.info("CSV-Export beendet.");
    }
}
