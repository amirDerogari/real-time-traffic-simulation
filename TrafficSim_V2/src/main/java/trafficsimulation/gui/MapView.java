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

public class MapView extends Pane {

    private String selectedVehicleId = null;
    private final Group roadLayer = new Group();
    private final Group trafficLightLayer = new Group();
    private final Group vehicleLayer = new Group();
    private final Group worldLayer = new Group();
    private final Map<String, Vehicle> latestVehicles = new HashMap<>();
    private final Map<String, Tooltip> vehicleTooltips = new HashMap<>();

    private NetworkModel network;

    private final Map<String, Circle> vehicleNodes = new HashMap<>();

    // store traffic light nodes by ID so we change their color live
    private final Map<String, Circle> trafficLightNodes = new HashMap<>();

    // transform parameters
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


    public MapView() {
        setStyle("-fx-background-color: #2b2b2b;");
        worldLayer.getChildren().addAll(roadLayer, trafficLightLayer, vehicleLayer);
        getChildren().add(worldLayer);
        roadLayer.setMouseTransparent(true);
        // trafficLightLayer.setMouseTransparent(true); //(traffic lights won’t be clickable now, after adding features, goes out of comment)

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

            // Clear selection unless a vehicle circle was clicked
            if (!(e.getTarget() instanceof Circle)
                    || !vehicleNodes.containsValue((Circle) e.getTarget())) {
                clearSelection();
            }
        });

        // redraw roads when resized
        widthProperty().addListener((obs, o, n) -> redrawStaticLayers());
        heightProperty().addListener((obs, o, n) -> redrawStaticLayers());
    }

    private void applyTransforms() {
        worldLayer.setScaleX(zoom);
        worldLayer.setScaleY(zoom);

        worldLayer.setTranslateX(panX);
        worldLayer.setTranslateY(panY);

    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public void setNetwork(NetworkModel network) {
        this.network = network;
        redrawStaticLayers();
    }

    private void redrawStaticLayers() {
        if (network == null) return;

        roadLayer.getChildren().clear();
        trafficLightLayer.getChildren().clear();
        trafficLightNodes.clear(); // important: rebuild TL circles on resize/reload

        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        // compute scaling to fit convBoundary
        double rangeX = network.maxX - network.minX;
        double rangeY = network.maxY - network.minY;

        scale = Math.min((w - 2 * margin) / rangeX, (h - 2 * margin) / rangeY);

        // store for y-inversion
        minX = network.minX;
        maxY = network.maxY;

        // draw roads
        for (List<NetworkModel.Point> poly : network.polylines) {
            Polyline line = new Polyline();
            for (NetworkModel.Point p : poly) {
                double sx = toScreenX(p.x());
                double sy = toScreenY(p.y());
                line.getPoints().addAll(sx, sy);
            }
            line.setStroke(Color.LIGHTGRAY);
            line.setStrokeWidth(6);
            roadLayer.getChildren().add(line);
        }

        // Create traffic light circles once, color updated in renderTrafficLights()
        network.trafficLightNodes.forEach((id, p) -> {
            Circle c = new Circle(toScreenX(p.x()), toScreenY(p.y()), 6, Color.GRAY);
            trafficLightNodes.put(id, c);
            trafficLightLayer.getChildren().add(c);
        });
    }

    public void renderVehicles(List<Vehicle> vehicles) {
        if (network == null) return;

        for (Vehicle v : vehicles) {
            String id = v.getId();
            latestVehicles.put(id, v);
            PositionVector p = v.getPositionVector();

            double x = toScreenX(p.getX());
            double y = toScreenY(p.getY());

            Circle c = vehicleNodes.get(id);

            // 1) Create circle if missing
            if (c == null) {
                c = new Circle(5, Color.YELLOW);
                vehicleNodes.put(id, c);
                vehicleLayer.getChildren().add(c);
            }

            // 2) Ensure tooltip exists (even for circles created earlier)
            // Ensure tooltip exists for this vehicle
            Tooltip tooltip = vehicleTooltips.get(id);
            if (tooltip == null) {
                tooltip = new Tooltip();
                tooltip.setShowDelay(javafx.util.Duration.millis(200));
                vehicleTooltips.put(id, tooltip);
            }

            // Always (re)install and always update text every tick
            Tooltip.install(c, tooltip);
            tooltip.setText(
                    "Vehicle ID: " + id +
                            "\nSpeed: " + String.format("%.2f", v.getSpeed()) + " m/s" +
                            "\nEdge: " + v.getEdgeId()
            );

            // 3) Update position
            c.setCenterX(x);
            c.setCenterY(y);
            if (!Boolean.TRUE.equals(c.getProperties().get("handlersInstalled"))) {
                final String vehicleId = id;
                final Circle node = c;

                node.setOnMouseEntered(e -> {
                    node.setRadius(6);
                    node.setStroke(Color.BLACK);
                    node.setStrokeWidth(1.5);
                    setCursor(javafx.scene.Cursor.HAND);
                    e.consume();
                });

                node.setOnMouseExited(e -> {
                    onVehicleMouseExit(vehicleId, node);
                    e.consume();
                });

                node.setOnMouseClicked(e -> {
                    selectVehicle(vehicleId);
                    e.consume();
                });

                c.getProperties().put("handlersInstalled", true);
            }

        }
    }

    // update traffic lights colors from SUMO state each tick
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

    //Helper: pick ONE junction color from the full phase string (rGrG...)
    private Color colorFromPhaseState(String state) {
        if (state == null || state.isBlank()) return Color.GRAY;

        // If any green exists -> show green
        if (state.indexOf('G') >= 0 || state.indexOf('g') >= 0) return Color.LIMEGREEN;

        // If any yellow exists -> show yellow
        if (state.indexOf('y') >= 0 || state.indexOf('Y') >= 0) return Color.GOLD;

        // Otherwise red
        return Color.RED;
    }

    private void selectVehicle(String id) {
        // Deselect old
        if (selectedVehicleId != null) {
            Circle old = vehicleNodes.get(selectedVehicleId);
            if (old != null) {
                old.setRadius(4);
                old.setStroke(null);
            }
        }

        // Select new
        selectedVehicleId = id;
        Circle c = vehicleNodes.get(id);
        if (c != null) {
            c.setRadius(7);
            c.setStroke(Color.RED);
            c.setStrokeWidth(2);
        }
    }
    private void onVehicleMouseExit(String vehicleId, Circle c) {
        if (!vehicleId.equals(selectedVehicleId)) {
            c.setRadius(4);
            c.setStroke(null);
        }
        setCursor(javafx.scene.Cursor.DEFAULT);
    }


    private void clearSelection() {
        if (selectedVehicleId == null) return;

        Circle c = vehicleNodes.get(selectedVehicleId);
        if (c != null) {
            c.setRadius(4);
            c.setStroke(null);
        }
        selectedVehicleId = null;
    }


    private double toScreenX(double x) {
        return margin + (x - minX) * scale;
    }

    // JavaFX y goes downward; SUMO y goes upward -> invert using maxY
    private double toScreenY(double y) {
        return margin + (maxY - y) * scale;
    }
}
