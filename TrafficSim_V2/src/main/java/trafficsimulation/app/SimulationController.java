package trafficsimulation.app;

import trafficsimulation.Vehicle;
import trafficsimulation.export.CsvStatsExporter;
import trafficsimulation.export.PdfStatsExporter;
import trafficsimulation.export.StatsExporter;
import trafficsimulation.stats.SimulationStatsSnapshot;
import trafficsimulation.stats.StatsManager;
import trafficsimulation.util.AppLogger;
import trafficsimulation.export.VehiclePositionCsvExporter;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SimulationController {

    private static final Logger LOG =
            AppLogger.getLogger(SimulationController.class);

    private final StatsManager statsManager = new StatsManager(); // Statistics handler
    private long simulationStep = 0; // Simulation time step counter

    // Store all snapshots here, so export has data
    private final List<SimulationStatsSnapshot> history = new ArrayList<>();



    public SimulationStatsSnapshot collectStats(List<Vehicle> vehicles) {


        // Calculate density per edge
        Map<String, Integer> densityPerEdge =
                statsManager.calculateDensityPerEdge(vehicles);

        // Collect global statistics
        SimulationStatsSnapshot snapshot =
                statsManager.collectStats(vehicles, simulationStep);

        statsManager.collectEdgeStats(vehicles, simulationStep);

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

        try {
            new VehiclePositionCsvExporter().export(simulationStep, vehicles, "vehicle_positions.csv");
        } catch (IOException e) {
            LOG.severe("Vehicle position export failed: " + e.getMessage());
        }


        simulationStep++; // Increase simulation step
        return snapshot;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }



    public void exportStatsToCsv(String filePath) {
        StatsExporter exporter = new CsvStatsExporter();
        try {
            LOG.info("Exporting CSV. History size = " + statsManager.getHistory().size());
            exporter.export(statsManager.getHistory(), filePath);
            LOG.info("CSV export successful: " + filePath);
        } catch (IOException e) {
            LOG.severe("CSV export failed: " + e.getMessage());
        }
    }


    public void exportStatsToPdf(String filePath) {
        StatsExporter pdfExporter = new PdfStatsExporter();
        try {
            pdfExporter.export(history, filePath);
            LOG.info("PDF export successful: " + filePath);
        } catch (IOException e) {
            LOG.severe("PDF export failed: " + e.getMessage());
        }
    }

}
