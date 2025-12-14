package trafficsimulation.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class TrafficSimulationAppFX extends Application {

    @Override
    public void start(Stage stage) {

        MapView mapView = new MapView();
        ControlPanel controlPanel = new ControlPanel(null, mapView); // backend later

        BorderPane root = new BorderPane();
        root.setCenter(mapView);
        root.setRight(controlPanel);

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Real-Time Traffic Simulation");
        stage.setScene(scene);
        stage.show();
    }
}
