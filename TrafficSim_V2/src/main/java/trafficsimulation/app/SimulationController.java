package trafficsimulation.app;

import trafficsimulation.Vehicle;
import trafficsimulation.export.CsvStatsExporter;
import trafficsimulation.export.PdfStatsExporter;
import trafficsimulation.export.StatsExporter;
import trafficsimulation.stats.SimulationStatsSnapshot;
import trafficsimulation.stats.StatsManager;
import trafficsimulation.util.AppLogger;
import trafficsimulation.export.VehiclePositionCsvExporter;
import trafficsimulation.stats.VehiclePositionSnapshot;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The SimulationController is responsible for collecting statistics
 * during each simulation step.
 *
 * It coordinates:
 * - collecting vehicle data
 * - creating statistic snapshots
 * - storing history
 * - exporting data to CSV and PDF files
 *
 * This class does NOT calculate statistics itself.
 * It delegates calculations to the StatsManager.
 */


public class SimulationController {

    private static final Logger LOG =
            AppLogger.getLogger(SimulationController.class);

    private final StatsManager statsManager = new StatsManager();
    private long simulationStep = 0;

    private final List<VehiclePositionSnapshot> vehiclePositionHistory = new ArrayList<>();
    private long lastVehiclePositionsRecordedStep = -1;

    // Store all snapshots here, so export has data
    private final List<SimulationStatsSnapshot> history = new ArrayList<>();

    /**
     * Collects statistics for the current simulation step.
     *
     * @param vehicles list of all vehicles in the current simulation step
     * @return a snapshot containing statistics of this step
     */

    public SimulationStatsSnapshot collectStats(List<Vehicle> vehicles) {

        // Calculate density per edge
        Map<String, Integer> densityPerEdge =
                statsManager.calculateDensityPerEdge(vehicles);

        // Collect global statistics
        SimulationStatsSnapshot snapshot =
                statsManager.collectStats(vehicles, simulationStep);

        // Save snapshot into history list
        history.add(snapshot);

        LOG.info(
                "Step " + simulationStep +
                        " | AvgSpeed: " + snapshot.getAverageSpeed() +
                        " | Vehicles: " + snapshot.getVehicleCount()
        );

        for (Map.Entry<String, Integer> entry : densityPerEdge.entrySet()) {
            LOG.fine(
                    "Edge " + entry.getKey() +
                            " | Vehicle count: " + entry.getValue()
            );
        }

        // Store vehicle positions history (one record per step)
        if (simulationStep != lastVehiclePositionsRecordedStep) {
            for (Vehicle v : vehicles) {
                String edgeId = v.getEdgeId();
                if (edgeId == null) {
                    edgeId = "";
                }

                vehiclePositionHistory.add(
                        new VehiclePositionSnapshot(
                                simulationStep,
                                v.getId(),
                                edgeId,
                                v.getSpeed()
                        )
                );
            }
            lastVehiclePositionsRecordedStep = simulationStep;
        }

        simulationStep++; // Increase simulation step
        return snapshot;
    }

    /**
     * Exports collected simulation statistics to a CSV file.
     *
     * @param filePath path of the target CSV file
     */

    public void exportStatsToCsv(String filePath) {
        StatsExporter exporter = new CsvStatsExporter();
        try {
            LOG.info("Export path (CSV): " + new java.io.File(filePath).getAbsolutePath());
            LOG.info("Exporting CSV. History size = " + statsManager.getHistory().size());
            exporter.export(history, filePath);
            LOG.info("CSV export successful: " + filePath);
        } catch (IOException e) {
            LOG.severe("CSV export failed: " + e.getMessage());
        }
    }

    /**
     * Exports collected simulation statistics to a PDF file.
     *
     * @param filePath path of the target PDF file
     */

    public void exportStatsToPdf(String filePath) {
        StatsExporter pdfExporter = new PdfStatsExporter();
        try {
            LOG.info("Export path (PDF): " + new java.io.File(filePath).getAbsolutePath());
            pdfExporter.export(history, filePath);
            LOG.info("PDF export successful: " + filePath);
        } catch (IOException e) {
            LOG.severe("PDF export failed: " + e.getMessage());
        }
    }

    /**
     * Exports vehicle position data to a CSV file.
     *
     * @param filePath path of the target CSV file
     */


    public void exportVehiclePositionsToCsv(String filePath) {
        VehiclePositionCsvExporter exporter = new VehiclePositionCsvExporter();
        try {
            LOG.info("Export path (Vehicle CSV): " + new java.io.File(filePath).getAbsolutePath());
            exporter.export(vehiclePositionHistory, filePath);
            LOG.info("Vehicle positions export successful: " + filePath + " (rows=" + vehiclePositionHistory.size() + ")");
        } catch (IOException e) {
            LOG.severe("Vehicle positions export failed: " + e.getMessage());
        }
    }

}
