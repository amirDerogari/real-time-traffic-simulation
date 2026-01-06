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
import trafficsimulation.app.SimulationController;

import java.util.List;

public class ControlPanel extends VBox { //JavaFX vertical layout panel

    private final SimulationManager simManager; //reference to the backend, so the panel can call startSimulation(), step(), closeSimulation(), etc
    private final MapView mapView; //reference to the map, so the panel can tell it to render vehicles/traffic lights each tick
    private final SimulationController simulationController;

    private Timeline timeline; //JavaFX timer that repeatedly runs update loop (tick)
    private boolean running = false; //basic state guard - prevent starting twice / stopping when already stopped

    //build the right-side control UI and wire it to the simulation
    public ControlPanel(SimulationManager simManager, MapView mapView, SimulationController simulationController) {
        this.simManager = simManager; //store backend reference
        this.mapView = mapView; //store map reference
        this.simulationController = simulationController;

        setPadding(new Insets(12)); //inner padding around the panel
        setSpacing(10); //vertical space between UI elements
        setPrefWidth(280); //preferred width of the panel
        setStyle("-fx-background-color: #3c3f41;"); //background color (dark)

        Label title = new Label("Controls");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;"); //title styling

        Button startBtn = new Button("Start Simulation"); //start button
        startBtn.setMaxWidth(Double.MAX_VALUE); //make button fill panel width

        Button stopBtn = new Button("Stop Simulation"); //stop button
        stopBtn.setMaxWidth(Double.MAX_VALUE); //make button fill panel width

        startBtn.setOnAction(e -> startSimulation()); //on click: start simulation + timeline
        stopBtn.setOnAction(e -> stopSimulation()); //on click: stop timeline + close simulation

        VBox spacer = new VBox(); // empty spacer region
        VBox.setVgrow(spacer, Priority.ALWAYS); //spacer expands to push buttons to top

        getChildren().addAll(title, new Separator(), startBtn, stopBtn, spacer); //add everything to this VBox
    }

    private void startSimulation() {
        if (running) return; //prevent starting twice
        running = true; //mark simulation as running

        // Start SUMO on a background thread so the JavaFX UI thread doesn't freeze
        new Thread(simManager::startSimulation, "SUMO-Start-Thread").start();

        // Step + redraw 10 times per second
        timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> tick())); //create a repeating timer, every 100ms call tick() (≈10 times/sec)
        timeline.setCycleCount(Timeline.INDEFINITE); //run forever until stopped
        timeline.play(); //start timer
    }

    private void tick() { //every 100ms from the Timeline
        if (!simManager.isConnected()) return; //If SUMO not connected yet, do nothing (prevent spam)

        try {
            simManager.step(); //advance SUMO by one step
            List<Vehicle> vehicles = simManager.getVehicles(); //fetch current vehicles from SUMO
            simulationController.collectStats(vehicles);
            mapView.renderVehicles(vehicles); //draw/update vehicles on the map
            List<trafficsimulation.TrafficLight> tls = simManager.getAllTrafficLights(); //fetch current traffic lights
            mapView.renderTrafficLights(tls); //update traffic light colors on the map

        }
        // If SUMO closes unexpectedly, stop spamming
        catch (Exception ex) {
            System.err.println("Tick failed: " + ex.getMessage());
            stopSimulation();
        }
    }


    private void stopSimulation() {
        if (!running) return; //if already stopped, do nothing
        running = false; //mark as not running

        if (timeline != null) { //if the update timer exists
            timeline.stop(); //stop periodic tick calls
            timeline = null; //drop reference
        }

        // Close SUMO / TraCI connection
        simManager.closeSimulation();
    }
}
