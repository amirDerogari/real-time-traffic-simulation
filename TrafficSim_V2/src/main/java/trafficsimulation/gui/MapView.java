package trafficsimulation.gui;

import trafficsimulation.PositionVector;
import trafficsimulation.Vehicle;
import trafficsimulation.TrafficLight;
import trafficsimulation.gui.net.NetworkModel;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.control.Tooltip;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;

/**
 * JavaFX view responsible for rendering the road network, vehicles, and traffic lights.
 *
 * <p>The view draws the static network (roads and junction polygons) once per resize or network reload,
 * and draws dynamic elements (vehicles and traffic light colors) every simulation tick.
 *
 * <p>Features:
 * <ul>
 *   <li>Zoom with mouse wheel (centered around cursor position)</li>
 *   <li>Pan with right mouse drag</li>
 *   <li>Double click to reset zoom/pan</li>
 *   <li>Selection and highlighting of vehicles and traffic lights</li>
 *   <li>Vehicle type filtering (cars, buses, emergency)</li>
 * </ul>
 */
public class MapView extends Pane {

    private String selectedVehicleId = null;
    private String selectedTrafficLightId = null;
    private Circle selectedTrafficLightNode = null;
    private final Group roadLayer = new Group();
    private final Group trafficLightLayer = new Group();
    private final Group vehicleLayer = new Group();
    private final Group worldLayer = new Group();
    private final Map<String, Vehicle> latestVehicles = new HashMap<>();
    private final Map<String, Tooltip> vehicleTooltips = new HashMap<>();
    private NetworkModel network;
    private final Map<String, Shape> vehicleNodes = new HashMap<>();

    /** Traffic light circles keyed by id, reused so their colors can be updated each tick. */
    private final Map<String, Circle> trafficLightNodes = new HashMap<>();


    private double scale = 1.0;
    private double minX, maxY;
    private double margin = 20;

    // Zoom and Pan state variables (worldLayer)
    private double zoom = 1.0;

    private double panX = 0;
    private double panY = 0;

    private double mouseStartX;
    private double mouseStartY;

    private double panStartX;
    private double panStartY;

    // --- Vehicle filtering flags
    private boolean showCars = true;
    private boolean showBuses = true;
    private boolean showEmergency = true;

    // --- last counts (for ControlPanel display)
    private int countCars = 0;
    private int countBuses = 0;
    private int countEmergency = 0;

    /**
     * Sets which vehicle types should be visible during rendering.
     *
     * <p>This only affects drawing. Vehicle counts are still computed from the full vehicle list.
     *
     * @param cars whether to show cars (default type)
     * @param buses whether to show buses (type id "bus_standard")
     * @param emergency whether to show emergency vehicles (type id "emergency")
     */
    public void setVehicleTypeVisibility(boolean cars, boolean buses, boolean emergency) {
        this.showCars = cars;
        this.showBuses = buses;
        this.showEmergency = emergency;
    }

    /**
     * @return number of cars counted during the last {@link #renderVehicles(List)} call
     */
    public int getCountCars() { return countCars; }

    /**
     * @return number of buses counted during the last {@link #renderVehicles(List)} call
     */
    public int getCountBuses() { return countBuses; }

    /**
     * @return number of emergency vehicles counted during the last {@link #renderVehicles(List)} call
     */
    public int getCountEmergency() { return countEmergency; }

    private boolean shouldRenderVehicle(Vehicle v) {
        String type = v.getTypeId();
        if ("emergency".equals(type)) return showEmergency;
        if ("bus_standard".equals(type)) return showBuses;
        return showCars; // everything else treated as "car"
    }

    /**
     * Applies the current zoom and pan values to the world layer.
     *
     * <p>This transforms the entire rendered map (roads, lights, vehicles) as a single group.
     */
    private void applyTransforms() {
        worldLayer.setScaleX(zoom);
        worldLayer.setScaleY(zoom);

        worldLayer.setTranslateX(panX);
        worldLayer.setTranslateY(panY);
    }

    /**
     * Creates an empty map view and initializes zoom/pan and mouse interaction handlers.
     *
     * <p>The map will not draw anything until a {@link NetworkModel} is provided via {@link #setNetwork(NetworkModel)}.
     */
    public MapView() {
        setStyle("-fx-background-color: #2b2b2b;");
        worldLayer.getChildren().addAll(roadLayer, trafficLightLayer, vehicleLayer);
        getChildren().add(worldLayer);
        roadLayer.setMouseTransparent(true); //roads ignore mouse events

        applyTransforms();

        // Mouse wheel zoom (centered on mouse position)
        setOnScroll(e -> {
            double oldZoom = zoom;

            double factor = (e.getDeltaY() > 0) ? 1.1 : 0.9;
            zoom = clamp(zoom * factor, 0.2, 5.0);

            double mouseX = e.getX();
            double mouseY = e.getY();

            double f = zoom / oldZoom;
            panX = mouseX - f * (mouseX - panX);
            panY = mouseY - f * (mouseY - panY);

            applyTransforms();
            e.consume();
        });

        // Mouse drag pan
        setOnMousePressed(e -> {
            //right button for pan
            if (!e.isSecondaryButtonDown()) return;

            mouseStartX = e.getX();
            mouseStartY = e.getY();

            panStartX = panX;
            panStartY = panY;
        });

        setOnMouseDragged(e -> {
            if (!e.isSecondaryButtonDown()) return;

            panX = panStartX + (e.getX() - mouseStartX);
            panY = panStartY + (e.getY() - mouseStartY);

            applyTransforms();
        });

        // Quick reset (double click)
        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                zoom = 1.0;
                panX = 0;
                panY = 0;
                applyTransforms();
                return;
            }

            Object target = e.getTarget();

            boolean clickedVehicle = (target instanceof Shape) && vehicleNodes.containsValue((Shape) target);
            boolean clickedTrafficLight = (target instanceof Circle) && trafficLightNodes.containsValue((Circle) target);

            // Clear selection unless a vehicle circle was clicked
            if (!clickedVehicle && !clickedTrafficLight) {
                clearSelection();
                clearTrafficLightSelection();
            }
        });



        // redraw roads when window is resized
        widthProperty().addListener((obs, o, n) -> redrawStaticLayers());
        heightProperty().addListener((obs, o, n) -> redrawStaticLayers());
    }

    /**
     * Clamps a value into the given inclusive range.
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * @return the id of the currently selected traffic light, or {@code null} if none is selected
     */
    public String getSelectedTrafficLightId() {
        return selectedTrafficLightId;
    }

    /**
     * Injects the loaded network model into the view and redraws static layers.
     *
     * <p>This clears all dynamic state (vehicles, tooltips, selection, traffic light circles) and rebuilds
     * the road/junction shapes for the current view size.
     *
     * @param network the network model to display
     */
    public void setNetwork(NetworkModel network) {
        this.network = network;

        // clean old state
        vehicleLayer.getChildren().clear();
        trafficLightLayer.getChildren().clear();

        vehicleNodes.clear();
        trafficLightNodes.clear();
        vehicleTooltips.clear();
        latestVehicles.clear();

        selectedVehicleId = null;
        selectedTrafficLightId = null;
        selectedTrafficLightNode = null;
        redrawStaticLayers();
    }

    /**
     * Rebuilds and redraws static layers (roads, junction polygons, and traffic light circles).
     *
     * <p>This is triggered after {@link #setNetwork(NetworkModel)} and also when the view is resized.
     */
    private void redrawStaticLayers() {
        if (network == null) return;

        roadLayer.getChildren().clear();
        trafficLightLayer.getChildren().clear();
        trafficLightNodes.clear(); // clear the Circle cache to rebuild TL circles on resize/reload

        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return; //if size=0, skip drawing to avoid bad scaling

        // compute SUMO map in world coordinates
        double rangeX = network.maxX - network.minX;
        double rangeY = network.maxY - network.minY;

        // scale in X and Y direction to fit and picks the smaller one so both width and height fit (no clipping)
        scale = Math.min((w - 2 * margin) / rangeX, (h - 2 * margin) / rangeY);

        // store for y-inversion
        minX = network.minX;
        maxY = network.maxY;

        // draw junction polygons
        for (List<NetworkModel.Point> poly : network.junctionPolygons) {
            Polygon junction = new Polygon();
            for (NetworkModel.Point p : poly) {
                junction.getPoints().addAll(toScreenX(p.x()), toScreenY(p.y()));
            }

            junction.setFill(Color.LIGHTGRAY);
            junction.setStroke(Color.LIGHTGRAY);
            junction.setStrokeWidth(9.5);
            junction.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

            roadLayer.getChildren().add(junction);
        }

        // draw roads
        for (List<NetworkModel.Point> poly : network.polylines) {
            Polyline line = new Polyline();
            for (NetworkModel.Point p : poly) {
                double sx = toScreenX(p.x());
                double sy = toScreenY(p.y());
                line.getPoints().addAll(sx, sy);
            }
            line.setStroke(Color.LIGHTGRAY);
            line.setStrokeWidth(15);
            roadLayer.getChildren().add(line);
        }

        // Create traffic light circles once, color updated in renderTrafficLights()
        network.trafficLightNodes.forEach((id, p) -> {
            Circle c = new Circle(toScreenX(p.x()), toScreenY(p.y()), 8);
            c.setFill(Color.DARKGRAY);     // placeholder until SUMO updates
            c.setStroke(Color.WHITE);      // always visible
            c.setStrokeWidth(1.5);

            c.setOnMouseClicked(e -> {
                selectTrafficLight(id, c);
                e.consume();
            });
            trafficLightNodes.put(id, c); // Cache for color updates
            trafficLightLayer.getChildren().add(c);
        });
    }

    /**
     * Renders vehicles for the current simulation step.
     *
     * <p>Vehicle shapes are created once per vehicle id and then moved each tick using translations.
     * Vehicles can be filtered by type (cars/buses/emergency).
     *
     * @param vehicles list of vehicles from the backend for the current simulation step
     */
    public void renderVehicles(List<Vehicle> vehicles) {
        if (network == null) return;

        // reset counts every tick
        countCars = 0;
        countBuses = 0;
        countEmergency = 0;

        // track IDs we want visible this tick
        java.util.Set<String> visibleIds = new java.util.HashSet<>();

        for (Vehicle v : vehicles) {
            String id = v.getId();
            String type = v.getTypeId();

            // count (grouping)
            if ("emergency".equals(type)) countEmergency++;
            else if ("bus_standard".equals(type)) countBuses++;
            else countCars++;

            // filtering
            if (!shouldRenderVehicle(v)) {
                continue;
            }

            visibleIds.add(id);

            latestVehicles.put(id, v); //the latest Vehicle object data saves here so other features (tooltip/selection) can read its data
            PositionVector p = v.getPositionVector();

            double x = toScreenX(p.getX());
            double y = toScreenY(p.getY());

            Shape node = vehicleNodes.get(id);

            // Create shape if missing (based on vehicle type)
            if (node == null) {
                if ("emergency".equals(type)) {
                    // triangle
                    Polygon tri = new Polygon(
                            0.0, -7.0,
                            6.0,  6.0,
                            -6.0,  6.0
                    );
                    tri.setFill(Color.ORANGE);
                    node = tri;

                } else if ("bus_standard".equals(type)) {
                    // rectangle
                    Rectangle r = new Rectangle(-7, -4, 14, 8);
                    r.setFill(Color.DODGERBLUE);
                    node = r;

                } else {
                    // default car
                    Circle c = new Circle(5, Color.YELLOW);
                    node = c;
                }

                vehicleNodes.put(id, node);
                vehicleLayer.getChildren().add(node);
            }

            //Ensure tooltip exists for each ID (even for circle created up here)
            Tooltip tooltip = vehicleTooltips.get(id);
            if (tooltip == null) {
                tooltip = new Tooltip();
                tooltip.setShowDelay(javafx.util.Duration.millis(200));
                vehicleTooltips.put(id, tooltip);
            }

            // Always (re)install and always update text every tick
            Tooltip.install(node, tooltip);
            tooltip.setText(
                    "Vehicle ID: " + id +
                            "\nType: " + type +
                            "\nSpeed: " + String.format("%.2f", v.getSpeed()) + " m/s" +
                            "\nEdge: " + v.getEdgeId()
            );

            node.setTranslateX(x);
            node.setTranslateY(y);

            //prevent re-adding event handlers every update frame
            if (!Boolean.TRUE.equals(node.getProperties().get("handlersInstalled"))) {
                final String vehicleId = id;
                final Shape vehicleNode = node;

                //highlight vehicle
                vehicleNode.setOnMouseEntered(e -> {
                    vehicleNode.setStroke(Color.BLACK);
                    vehicleNode.setStrokeWidth(1.5);
                    setCursor(javafx.scene.Cursor.HAND);
                    e.consume();
                });

                //remove highlight
                vehicleNode.setOnMouseExited(e -> {
                    onVehicleMouseExit(vehicleId, vehicleNode);
                    e.consume();
                });

                //mark vehicle as selected
                vehicleNode.setOnMouseClicked(e -> {
                    selectVehicle(vehicleId);
                    e.consume(); //prevent bubbling up to the map (so a click on a vehicle doesn’t also trigger “clear selection” on the background)
                });

                vehicleNode.getProperties().put("handlersInstalled", true); //store a flag on the node so next ticks skip re-installing handlers
            }

        }
        // Remove nodes that were visible before but not visible now (filtered out or disappeared)
        java.util.List<String> toRemove = new java.util.ArrayList<>();
        for (String existingId : vehicleNodes.keySet()) {
            if (!visibleIds.contains(existingId)) {
                Shape n = vehicleNodes.get(existingId);
                if (n != null) vehicleLayer.getChildren().remove(n);
                toRemove.add(existingId);
                vehicleTooltips.remove(existingId);
                latestVehicles.remove(existingId);

                // if selected vehicle got removed, clear selection
                if (existingId.equals(selectedVehicleId)) {
                    // Clear highlight if selected vehicle disappears
                    if (n != null) {
                        n.setStroke(null);
                        n.setStrokeWidth(0);
                    }
                    selectedVehicleId = null;
                }
            }
        }
        for (String rid : toRemove) vehicleNodes.remove(rid);
    }

    /**
     * Updates traffic light colors based on their current phase states.
     *
     * <p>Traffic light circles are created when the network is drawn; this method only changes their fill colors.
     *
     * @param tls list of traffic lights from the backend for the current simulation step
     */
    public void renderTrafficLights(List<TrafficLight> tls) {
        if (network == null) return;

        for (TrafficLight tl : tls) {
            String id = tl.getId();

            Circle c = trafficLightNodes.get(id);
            if (c == null) {
                // Not in our network list (ID mismatch) -> ignore for now
                continue;
            }

            c.setFill(colorFromPhaseState(tl.getPhaseState()));
        }

    }

    /**
     * Converts a SUMO traffic light phase state string into a display color.
     *
     * <p>This method uses the first character of the state string as a simplified summary:
     * g->green, y->yellow, r->red, otherwise gray.
     *
     * @param state raw phase state string returned by SUMO
     * @return corresponding JavaFX color used for rendering
     */
    private Color colorFromPhaseState(String state) {
        if (state == null || state.isBlank()) return Color.GRAY;

        char s = Character.toLowerCase(state.charAt(0));
        return switch (s) {
            case 'g' -> Color.LIMEGREEN;
            case 'y' -> Color.GOLD;
            case 'r' -> Color.RED;
            default -> Color.GRAY;
        };
    }

    private void selectVehicle(String id) {
        if (selectedVehicleId != null) {
            Shape old = vehicleNodes.get(selectedVehicleId);
            if (old != null) {
                old.setStroke(null);
                old.setStrokeWidth(0);
            }
        }

        selectedVehicleId = id;
        Shape node = vehicleNodes.get(id);
        if (node != null) {
            node.setStroke(Color.RED);
            node.setStrokeWidth(2);
        }
    }

    private void onVehicleMouseExit(String vehicleId, Shape node) {
        if (!vehicleId.equals(selectedVehicleId)) {
            node.setStroke(null);
            node.setStrokeWidth(0);
        }
        setCursor(javafx.scene.Cursor.DEFAULT);
    }

    private void clearSelection() {
        if (selectedVehicleId == null) return;

        Shape node = vehicleNodes.get(selectedVehicleId);
        if (node != null) {
            node.setStroke(null);
            node.setStrokeWidth(0);
        }
        selectedVehicleId = null;
    }

    // select a traffic light (called when a TL circle is clicked)
    private void selectTrafficLight(String id, Circle node) {
        if (selectedTrafficLightNode != null) {
            selectedTrafficLightNode.setStroke(null);
            selectedTrafficLightNode.setStrokeWidth(0);
        }

        selectedTrafficLightId = id;
        selectedTrafficLightNode = node;

        node.setStroke(Color.WHITE);
        node.setStrokeWidth(2);
    }

    // clears traffic light selection
    private void clearTrafficLightSelection() {
        if (selectedTrafficLightNode != null) {
            selectedTrafficLightNode.setStroke(null);
            selectedTrafficLightNode.setStrokeWidth(0);
        }
        selectedTrafficLightNode = null;
        selectedTrafficLightId = null;
    }

    /**
     * Clears all dynamic elements (vehicles, tooltips, selection) without removing the static network.
     *
     * <p>This is useful when restarting the simulation or when the backend connection resets.
     */
    public void resetDynamicLayers() {
        vehicleLayer.getChildren().clear();
        vehicleNodes.clear();
        vehicleTooltips.clear();
        latestVehicles.clear();
        selectedVehicleId = null;

        // also clear TL selection highlight
        clearTrafficLightSelection();
    }



    /**
     * Converts a SUMO world x-coordinate into a JavaFX screen x-coordinate.
     *
     * @param x world x-coordinate
     * @return screen x-coordinate after scaling and margin offset
     */
    private double toScreenX(double x) {
        return margin + (x - minX) * scale;
    }

    /**
     * Converts a SUMO world y-coordinate into a JavaFX screen y-coordinate.
     *
     * <p>JavaFX y increases downward, while SUMO y increases upward, so this method inverts y using {@code maxY}.
     *
     * @param y world y-coordinate
     * @return screen y-coordinate after scaling, inversion, and margin offset
     */
    private double toScreenY(double y) {
        return margin + (maxY - y) * scale;
    }
}