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
import javafx.scene.control.CheckBox;
import java.util.List;

/**
 * JavaFX control panel shown next to the map.
 *
 * <p>This panel provides:
 * <ul>
 *   <li>Start/Stop controls for the simulation,</li>
 *   <li>traffic controls such as enabling rush hour, spawning an emergency vehicle, and switching to the next traffic light phase,</li>
 *   <li>vehicle type visibility filters (cars, buses, emergency),</li>
 *   <li>vehicle count display,</li>
 *   <li>information about the currently selected traffic light on the map.</li>
 * </ul>
 *
 * <p>The simulation is advanced periodically using a JavaFX {@link Timeline}. Starting SUMO is executed on a
 * background thread to avoid freezing the JavaFX application thread.
 */
public class ControlPanel extends VBox {

    /** Backend reference used to start/stop/step the simulation and query vehicles/traffic lights. */
    private SimulationManager simManager;

    /** Reference to the map view, used for rendering vehicles, traffic lights, and reading the selected TL id. */
    private final MapView mapView;

    /** Reference to the simulation controller (kept for integration with statistics/export or future features). */
    private final SimulationController simulationController;

    /** JavaFX timer that triggers the periodic update loop (tick). */
    private Timeline timeline;

    /** True while the simulation is running (prevents starting twice). */
    private boolean running = false;

    /** UI label showing the currently selected traffic light id. */
    private final Label selectedTlIdLabel = new Label("Selected TL: none");

    /** UI label showing the current state of the selected traffic light. */
    private final Label selectedTlStateLabel = new Label("State: -");

    /** UI label for short status messages (e.g., errors or user guidance). */
    private final Label actionStatus = new Label("");

    /** UI label showing the current vehicle counts. */
    private final Label vehicleCountLabel = new Label("Cars: 0 | Buses: 0 | Emergency: 0");

    /**
     * Creates the control panel UI and wires all buttons/checkboxes to simulation actions.
     *
     * @param simManager backend simulation manager
     * @param mapView map view that renders the network, vehicles, and traffic lights
     * @param simulationController controller used for simulation-related coordination (e.g., statistics/export integration)
     */
    public ControlPanel(SimulationManager simManager, MapView mapView, SimulationController simulationController) {
        this.simManager = simManager;
        this.mapView = mapView;
        this.simulationController = simulationController;

        setPadding(new Insets(12));
        setSpacing(10);
        setPrefWidth(280);
        setStyle("-fx-background-color: #3c3f41;");

        Label title = new Label("Controls");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Button startBtn = new Button("Start Simulation");
        startBtn.setStyle("-fx-background-color: yellowgreen");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> startSimulation());

        Button stopBtn = new Button("Stop Simulation");
        stopBtn.setStyle("-fx-background-color: lightcoral");
        stopBtn.setMaxWidth(Double.MAX_VALUE);
        stopBtn.setOnAction(e -> stopSimulation());

        Label filterTitle = new Label("Filters");
        filterTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        CheckBox showCarsBox = new CheckBox("Show cars");
        CheckBox showBusesBox = new CheckBox("Show buses");
        CheckBox showEmergencyBox = new CheckBox("Show emergency");
        showCarsBox.setSelected(true);
        showBusesBox.setSelected(true);
        showEmergencyBox.setSelected(true);
        showCarsBox.setStyle("-fx-text-fill: white;");
        showBusesBox.setStyle("-fx-text-fill: white;");
        showEmergencyBox.setStyle("-fx-text-fill: white;");


        Runnable applyFilter = () -> mapView.setVehicleTypeVisibility(
                showCarsBox.isSelected(),
                showBusesBox.isSelected(),
                showEmergencyBox.isSelected()
        );

        showCarsBox.setOnAction(e -> applyFilter.run());
        showBusesBox.setOnAction(e -> applyFilter.run());
        showEmergencyBox.setOnAction(e -> applyFilter.run());

        // apply once at startup
        applyFilter.run();

        Label vehicleTitle = new Label("Vehicle counts");
        vehicleTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        vehicleCountLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: #2b2b2b;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 10 8 10;" +
                        "-fx-border-color: #777;" +
                        "-fx-border-radius: 6;" +
                        "-fx-border-width: 1;"
        );
        vehicleCountLabel.setMaxWidth(Double.MAX_VALUE);

        Label trafficcontrolTitle = new Label("Traffic Control");
        trafficcontrolTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Button rushHourBtn = new Button("Enable Rush Hour");
        rushHourBtn.setMaxWidth(Double.MAX_VALUE);
        rushHourBtn.setOnAction(e -> {
            if (!simManager.isConnected()) {
                actionStatus.setText("Start the simulation first.");
                return;
            }
            simManager.runRushHour();
            actionStatus.setText("Rush hour enabled.");
        });

        Button emergencyBtn = new Button("Spawn Emergency Vehicle");
        emergencyBtn.setMaxWidth(Double.MAX_VALUE);
        emergencyBtn.setOnAction(e -> {
            if (!simManager.isConnected()) {
                actionStatus.setText("Start the simulation first.");
                return;
            }
            simManager.spawnEmergencyVehicle();
            actionStatus.setText("Emergency vehicle spawned (or already exists).");
        });


        Button nextPhaseBtn = new Button("Next Phase");
        nextPhaseBtn.setMaxWidth(Double.MAX_VALUE);
        nextPhaseBtn.setOnAction(e -> {
            if (!simManager.isConnected()) {
                actionStatus.setText("Start the simulation first.");
                return;
            }
            String tlId = mapView.getSelectedTrafficLightId();
            if (tlId == null) {
                actionStatus.setText("Select a traffic light on the map first.");
                return;
            }

            /// TODO: Implement nextPhase(String tlId) in
            simManager.nextPhase(tlId);

            actionStatus.setText("Next phase for TL: " + tlId);
        });

        actionStatus.setStyle("-fx-text-fill: #cfcfcf;");

        Label tlTitle = new Label("Traffic Light Info");
        tlTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        selectedTlIdLabel.setStyle("-fx-text-fill: white;");
        selectedTlStateLabel.setStyle("-fx-text-fill: white;");

        Label statusTitle = new Label("Status");
        statusTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");


        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(title, startBtn, stopBtn, new Separator(),
                trafficcontrolTitle, rushHourBtn, emergencyBtn, nextPhaseBtn, new Separator(),
                filterTitle, showCarsBox, showBusesBox, showEmergencyBox, new Separator(),
                vehicleTitle, vehicleCountLabel, new Separator(),
                tlTitle, selectedTlIdLabel, selectedTlStateLabel, new Separator(),
                statusTitle, actionStatus, spacer);
    }

    /**
     * Starts the simulation and begins the periodic update loop.
     *
     * <p>SUMO startup is performed on a background thread so the JavaFX UI thread stays responsive.
     * The periodic stepping is performed by a {@link Timeline} that calls {@link #tick()} repeatedly.
     */
    private void startSimulation() {
        if (running) return;
        mapView.resetDynamicLayers();
        running = true;

        actionStatus.setText("Starting simulation...");

        // Start SUMO on a background thread so the JavaFX UI thread doesn't freeze
        new Thread(simManager::startSimulation, "SUMO-Start-Thread").start();

        // Step + redraw 10 times per second
        timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Advances the simulation by one step and updates the GUI.
     *
     * <p>This method is called periodically by the JavaFX {@link Timeline}. It steps the simulation,
     * fetches vehicles and traffic lights, triggers rendering in the {@link MapView}, and updates UI labels.
     */
    private void tick() {
        if (!simManager.isConnected())
            return;

        try {
            simManager.step(); //advance SUMO by one step
            List<Vehicle> vehicles = simManager.getVehicles(); //fetch current vehicles from SUMO

            mapView.renderVehicles(vehicles);

            vehicleCountLabel.setText(
                    "Cars: " + mapView.getCountCars() +
                            " | Buses: " + mapView.getCountBuses() +
                            " | Emergency: " + mapView.getCountEmergency()
            );

            List<trafficsimulation.TrafficLight> tls = simManager.getAllTrafficLights(); //fetch current TLs
            mapView.renderTrafficLights(tls);
            updateTrafficLightInfo(tls);

        }
        // If SUMO closes unexpectedly, stop spamming
        catch (Exception ex) {
            System.err.println("Tick failed: " + ex.getMessage());
            stopSimulation();
        }
    }


    /**
     * Converts a SUMO traffic light state string into a simplified color name.
     *
     * <p>SUMO state strings often contain multiple characters, one per signal group.
     * This method uses the first character as a quick summary:
     * <ul>
     *   <li>'g' -> GREEN</li>
     *   <li>'y' -> YELLOW</li>
     *   <li>'r' -> RED</li>
     * </ul>
     *
     * @param state raw state string returned by SUMO
     * @return "GREEN", "YELLOW", "RED", or "UNKNOWN" if the value cannot be interpreted
     */
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

    /**
     * Updates the traffic light info section based on the currently selected traffic light in the map view.
     *
     * <p>If no traffic light is selected, this method resets the labels to default values.
     *
     * @param tls list of traffic lights from the current simulation step
     */
    private void updateTrafficLightInfo(List<trafficsimulation.TrafficLight> tls) {
        String selectedId = mapView.getSelectedTrafficLightId();

        if (selectedId == null) {
            selectedTlIdLabel.setText("Selected TL: none");
            selectedTlStateLabel.setText("State: -");
            return;
        }

        selectedTlIdLabel.setText("Selected TL: " + selectedId);

        String state = "-";
        for (trafficsimulation.TrafficLight tl : tls) {
            if (selectedId.equals(tl.getId())) {
                state = tl.getPhaseState();
                break;
            }
        }
        selectedTlStateLabel.setText("State: " + summarizeTlState(state));
    }

    /**
     * Stops the periodic update loop and closes the simulation connection.
     */
    private void stopSimulation() {
        if (!running) return;
        running = false;

        if (timeline != null) { //if the update timer exists
            timeline.stop();
            timeline = null;
        }

        simManager.closeSimulation();
        actionStatus.setText("Simulation stopped.");
    }
}
