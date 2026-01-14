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
import javafx.scene.control.ComboBox;
import trafficsimulation.ScenarioMode;
import trafficsimulation.TrafficLight;
import trafficsimulation.MapScenario;
import trafficsimulation.gui.net.NetworkLoader;


import java.util.List;

public class ControlPanel extends VBox { //JavaFX vertical layout panel

    private SimulationManager simManager; //reference to the backend, so the panel can call startSimulation(), step(), closeSimulation(), etc
    private final MapView mapView; //reference to the map, so the panel can tell it to render vehicles/traffic lights each tick
    private Timeline timeline; //JavaFX timer that repeatedly runs update loop (tick)
    private boolean running = false; //basic state guard - prevent starting twice / stopping when already stopped
    private final ComboBox<ScenarioMode> scenarioBox = new ComboBox<>(); // dropdown to choose a scenario mode
    private final ComboBox<MapScenario> mapBox = new ComboBox<>(); // dropdown to choose which map configuration to load
    private boolean scenarioInitialized = false; // flag to prevent scenario logic from running before initial setup is finished (while still building the UI)
    private final Label selectedTlIdLabel = new Label("Selected TL: none"); // label that shows the currently selected traffic light ID
    private final Label selectedTlStateLabel = new Label("State: -"); // label that shows the selected traffic light’s current state


    //build the right-side control UI and wire it to the simulation
    public ControlPanel(SimulationManager simManager, MapView mapView) {
        this.simManager = simManager; //store backend reference
        this.mapView = mapView; //store map reference

        setPadding(new Insets(12)); //inner padding around the panel
        setSpacing(10); //vertical space between UI elements
        setPrefWidth(280); //preferred width of the panel
        setStyle("-fx-background-color: #3c3f41;"); //background color (dark)

        Label title = new Label("Controls");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;"); // title styling

        Label mapTitle = new Label("Map");
        mapTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;"); // title styling

        mapBox.getItems().addAll(MapScenario.BEISPIEL2, MapScenario.STAU); // add two map options to the ComboBox list
        mapBox.setValue(MapScenario.BEISPIEL2); // set the default selected option to BEISPIEL2
        mapBox.setMaxWidth(Double.MAX_VALUE); //make button fill panel width

        Button startBtn = new Button("Start Simulation"); //start button
        startBtn.setMaxWidth(Double.MAX_VALUE); //make button fill panel width

        Button stopBtn = new Button("Stop Simulation"); //stop button
        stopBtn.setMaxWidth(Double.MAX_VALUE); //make button fill panel width

        startBtn.setOnAction(e -> startSimulation()); //on click: start simulation + timeline
        stopBtn.setOnAction(e -> stopSimulation()); //on click: stop timeline + close simulation

        Label scenarioTitle = new Label("Scenario");
        scenarioTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;"); // title styling

        scenarioBox.getItems().addAll(ScenarioMode.NORMAL, ScenarioMode.GOOD_TRAFFIC, ScenarioMode.BAD_TRAFFIC); // add three scenario options to the ComboBox list
        scenarioBox.setValue(ScenarioMode.NORMAL); // set NORMAL as default scenario
        scenarioBox.setMaxWidth(Double.MAX_VALUE); //make button fill panel width

        scenarioBox.setOnAction(e -> simManager.setScenarioMode(scenarioBox.getValue())); // when user picks a scenario, send the selected ScenarioMode to the backend


        Label tlTitle = new Label("Traffic Light Info"); // title label for TL info section
        tlTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;"); // title styling

        selectedTlIdLabel.setStyle("-fx-text-fill: white;"); // title styling
        selectedTlStateLabel.setStyle("-fx-text-fill: white;"); // title styling


        VBox spacer = new VBox(); // empty spacer region
        VBox.setVgrow(spacer, Priority.ALWAYS); //spacer expands to push buttons to top

        getChildren().addAll(title, new Separator(), startBtn, stopBtn, new Separator(), mapTitle, mapBox, new Separator(), scenarioTitle, scenarioBox, new Separator(), tlTitle, selectedTlIdLabel, selectedTlStateLabel, spacer); //add everything to this VBox
    }

    private void startSimulation() {
        if (running) return; //prevent starting twice
        running = true; //mark simulation as running


        // choose config + net
        String configPath;
        String netFile;

        if (mapBox.getValue() == MapScenario.STAU) {
            configPath = "src/main/resources/stauconfig.sumocfg";
            netFile = "stau.net.xml";
        } else {
            configPath = "src/main/resources/beispiel2config.sumocfg";
            netFile = "beispiel2.net.xml";
        }

        // recreate backend
        simManager = new SimulationManager(configPath);

        // reload map
        mapView.setNetwork(NetworkLoader.loadFromResources(netFile));

        scenarioInitialized = false; // temporarily disable scenario-change handler so they don’t trigger during this reset
        simManager.resetScenario(); // reset the backend scenario settings back to a default/clean state
        simManager.setScenarioMode(scenarioBox.getValue()); // apply the currently selected ScenarioMode from the dropdown to the backend


        // Start SUMO on a background thread so the JavaFX UI thread doesn't freeze
        new Thread(simManager::startSimulation, "SUMO-Start-Thread").start();

        // Step + redraw 10 times per second
        timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> tick())); //create a repeating timer, every 100ms call tick() (≈10 times/sec)
        timeline.setCycleCount(Timeline.INDEFINITE); //run forever until stopped
        timeline.play(); //start timer
    }

    private void tick() { //every 100ms from the Timeline
        if (!simManager.isConnected()) return; //If SUMO not connected yet, do nothing (prevent spam)
        if (!scenarioInitialized) {
            List<TrafficLight> tls = simManager.getAllTrafficLights();
            if (tls.size() >= 2) {
                simManager.setScenarioTrafficLights(tls.get(0), tls.get(1)); // same as StressTest
                simManager.resetScenario();
                scenarioInitialized = true;
            }
        }


        try {
            simManager.step(); //advance SUMO by one step
            List<Vehicle> vehicles = simManager.getVehicles(); //fetch current vehicles from SUMO
            mapView.renderVehicles(vehicles); //draw vehicles on the map
            List<trafficsimulation.TrafficLight> tls = simManager.getAllTrafficLights(); //fetch current traffic lights
            mapView.renderTrafficLights(tls); //update traffic light colors on the map
            updateTrafficLightInfo(tls);


        }
        // If SUMO closes unexpectedly, stop spamming
        catch (Exception ex) {
            System.err.println("Tick failed: " + ex.getMessage());
            stopSimulation();
        }
    }


    // Change SUMO returned state for each TL to simple colors: Green, Yellow, Red
    private String summarizeTlState(String state) {
        if (state == null || state.isBlank()) return "UNKNOWN";

        char s = Character.toLowerCase(state.charAt(0));
        return switch (s) {
            case 'g' -> "GREEN";
            case 'y' -> "YELLOW";
            case 'r' -> "RED";
            default -> "UNKNOWN";
        };
    }


    private void updateTrafficLightInfo(List<trafficsimulation.TrafficLight> tls) {
        String selectedId = mapView.getSelectedTrafficLightId();

        if (selectedId == null) {
            selectedTlIdLabel.setText("Selected TL: none");
            selectedTlStateLabel.setText("State: -");
            return;
        }

        selectedTlIdLabel.setText("Selected TL: " + selectedId);

        // Find matching traffic light in current list
        String state = "-";
        for (trafficsimulation.TrafficLight tl : tls) {
            if (selectedId.equals(tl.getId())) {
                state = tl.getPhaseState();
                break;
            }
        }
        selectedTlStateLabel.setText("State: " + summarizeTlState(state));
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
