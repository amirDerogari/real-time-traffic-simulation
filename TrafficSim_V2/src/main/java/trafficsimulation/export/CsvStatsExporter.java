package trafficsimulation.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import trafficsimulation.stats.SimulationStatsSnapshot;
import trafficsimulation.util.AppLogger;

public class CsvStatsExporter implements StatsExporter {
    private static final Logger LOG = AppLogger.getLogger(CsvStatsExporter.class);

    @Override
    public void export(List<SimulationStatsSnapshot> snapshots, String targetPath) throws IOException {
        LOG.info("CSV-Export gestartet: " + targetPath);

        try (FileWriter writer = new FileWriter(targetPath)) {
            // Kopfzeile
            writer.write("step,averageSpeed,vehicleDensity,vehicleCount\n");

            // Noch Dummy: echte Werte sind schon verfügbar, aber Logik prüfen wir später
            for (SimulationStatsSnapshot snapshot : snapshots) {
                String line = snapshot.getSimulationStep() + "," +
                        snapshot.getAverageSpeed() + "," +
                        snapshot.getVehicleDensity() + "," +
                        snapshot.getVehicleCount() + "\n";
                writer.write(line);
            }
        }

        LOG.info("CSV-Export beendet.");
    }

}
