package trafficsimulation.gui;

import trafficsimulation.SimulationManager;
import trafficsimulation.gui.net.NetworkLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class TrafficSimulationAppFX extends Application {

    @Override
    public void start(Stage stage) {

        // 1) Map
        MapView mapView = new MapView();
        mapView.setNetwork(NetworkLoader.loadFromResources("beispiel2.net.xml"));

        // 2) Backend manager (Caspar)
        String configPath = "src/main/resources/beispiel2config.sumocfg";
        SimulationManager simManager = new SimulationManager(configPath);

        // 3) Control panel now takes SimulationManager
        ControlPanel controlPanel = new ControlPanel(simManager, mapView);

        BorderPane root = new BorderPane();
        root.setCenter(mapView);
        root.setRight(controlPanel);

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Real-Time Traffic Simulation");
        stage.setScene(scene);
        stage.show();
    }
}
