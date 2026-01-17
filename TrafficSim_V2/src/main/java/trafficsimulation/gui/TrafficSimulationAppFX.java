package trafficsimulation.gui;

import trafficsimulation.SimulationManager;
import trafficsimulation.app.SimulationController;
import trafficsimulation.gui.net.NetworkLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

//full ap wiring
public class TrafficSimulationAppFX extends Application {

    private SimulationController simulationController;

    @Override
    public void start(Stage stage) {

        //Map
        MapView mapView = new MapView(); //create map view
        mapView.setNetwork(NetworkLoader.loadFromResources("final_map.net.xml")); //load road network + draw static map

        //Backend manager (Caspar)
        String configPath = "src/main/resources/final_map.sumocfg"; //path to SUMO config file
        SimulationManager simManager = new SimulationManager(configPath); //backend manager to start/step/close SUMO

        //Statistics controller (Caspar)
        simulationController = new SimulationController();

        //Control panel
        ControlPanel controlPanel = new ControlPanel(simManager, mapView); //right panel controlling simulation + rendering

        BorderPane root = new BorderPane(); //main layout container
        root.setCenter(mapView); //put map in center
        root.setRight(controlPanel); //put controls on the right

        Scene scene = new Scene(root, 1200, 800); //create scene with fixed window size
        stage.setTitle("Real-Time Traffic Simulation"); //window title
        stage.setScene(scene); //attach scene to stage
        stage.show(); //show window

        //CSV export on application close
        stage.setOnCloseRequest(event -> {
           simulationController.exportStatsToCsv("simulation_stats.csv");
      });
    }
}
