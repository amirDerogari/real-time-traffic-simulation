package trafficsimulation.gui;

import trafficsimulation.SimulationManager;
import trafficsimulation.Vehicle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class ControlPanel extends VBox {

    private final SimulationManager simManager;
    private final MapView mapView;

    private Timeline timeline;
    private boolean running = false;

    public ControlPanel(SimulationManager simManager, MapView mapView) {
        this.simManager = simManager;
        this.mapView = mapView;

        setPadding(new Insets(12));
        setSpacing(10);
        setPrefWidth(280);
        setStyle("-fx-background-color: #3c3f41;");

        Label title = new Label("Controls");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Button startBtn = new Button("Start Simulation");
        startBtn.setMaxWidth(Double.MAX_VALUE);

        Button stopBtn = new Button("Stop Simulation");
        stopBtn.setMaxWidth(Double.MAX_VALUE);

        startBtn.setOnAction(e -> startSimulation());
        stopBtn.setOnAction(e -> stopSimulation());

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(title, new Separator(), startBtn, stopBtn, spacer);
    }

    private void startSimulation() {
        if (running) return;
        running = true;

        // Start SUMO on a background thread so the JavaFX UI thread doesn't freeze
        new Thread(simManager::startSimulation, "SUMO-Start-Thread").start();

        // Step + redraw 10 times per second
        timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void tick() {
        // If SUMO not connected yet, do nothing (prevents spam)
        if (!simManager.isConnected()) return;

        try {
            simManager.step();
            List<Vehicle> vehicles = simManager.getVehicles();
            mapView.renderVehicles(vehicles);
            List<trafficsimulation.TrafficLight> tls = simManager.getAllTrafficLights();
            mapView.renderTrafficLights(tls);

        } catch (Exception ex) {
            // If SUMO closes unexpectedly, stop spamming
            System.err.println("Tick failed: " + ex.getMessage());
            stopSimulation();
        }
    }


    private void stopSimulation() {
        if (!running) return;
        running = false;

        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }

        // Close SUMO / TraCI connection
        simManager.closeSimulation();
    }
}
