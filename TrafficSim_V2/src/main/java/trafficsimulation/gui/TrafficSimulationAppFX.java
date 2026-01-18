package trafficsimulation.gui;

import trafficsimulation.SimulationManager;
import trafficsimulation.app.SimulationController;
import trafficsimulation.gui.net.NetworkLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * JavaFX application entry point for the traffic simulation GUI.
 *
 * <p>This class wires together the GUI components (map view and control panel),
 * loads the SUMO network for rendering, initializes the simulation backend, and
 * triggers statistics export when the application closes.
 */
public class TrafficSimulationAppFX extends Application {

    /** Collects simulation statistics and provides export functionality. */
    private SimulationController simulationController;

    /**
     * Initializes and shows the main GUI window.
     *
     * <p>This method:
     * <ul>
     *   <li>creates the {@link MapView} and loads the SUMO network into it,</li>
     *   <li>creates the {@link SimulationManager} used to control SUMO/TraCI,</li>
     *   <li>creates the {@link SimulationController} used to collect/export statistics,</li>
     *   <li>builds the UI layout (map in center, controls on the right),</li>
     *   <li>exports statistics to CSV/PDF when the window is closed.</li>
     * </ul>
     *
     * @param stage the primary stage provided by JavaFX
     */
    @Override
    public void start(Stage stage) {

        MapView mapView = new MapView();
        mapView.setNetwork(NetworkLoader.loadFromResources("final_map.net.xml"));

        String configPath = "src/main/resources/final_map.sumocfg";
        SimulationManager simManager = new SimulationManager(configPath);

        simulationController = new SimulationController();

        ControlPanel controlPanel = new ControlPanel(simManager, mapView, simulationController); //right panel controlling simulation + rendering

        BorderPane root = new BorderPane();
        root.setCenter(mapView);
        root.setRight(controlPanel);

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Real-Time Traffic Simulation");
        stage.setScene(scene);
        stage.show();

        //CSV und PDF export on application close
        stage.setOnCloseRequest(event -> {
            simulationController.exportStatsToCsv("simulation_stats.csv");
            simulationController.exportStatsToPdf("simulation_stats.pdf");
            simulationController.exportVehiclePositionsToCsv("vehicle_positions.csv");
        });

    }
}
