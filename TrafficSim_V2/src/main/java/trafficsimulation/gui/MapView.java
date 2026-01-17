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

    // store traffic light nodes by ID, so we change their color live (updated)
    private final Map<String, Circle> trafficLightNodes = new HashMap<>();

    // transform parameters
    private double scale = 1.0;
    private double minX, maxY;
    private double margin = 20;

    // Zoom and Pan state variables (worldLayer)
    private double zoom = 1.0; //current zoom factor applied to the map (1.0 = normal, >1 zoom in, <1 zoom out)

    private double panX = 0;
    private double panY = 0; //current translation (how far the map is shifted left/right and up/down)

    private double mouseStartX;
    private double mouseStartY; //mouse position when a drag starts (to measure how far the user moved)

    private double panStartX;
    private double panStartY; //the pan values at the moment the drag starts


    public MapView() {
        setStyle("-fx-background-color: #2b2b2b;");
        worldLayer.getChildren().addAll(roadLayer, trafficLightLayer, vehicleLayer); //a layer stack inside worldLayer. roads are behind, then lights, then vehicles on top
        getChildren().add(worldLayer); //display the whole Layer group
        roadLayer.setMouseTransparent(true); //roads ignore mouse events
        //trafficLightLayer.setMouseTransparent(true); //(traffic lights won’t be clickable now, after adding features, goes out of comment)

        applyTransforms(); //set WorldLayer's scale and translation

        // Mouse wheel zoom (centered on mouse position)
        setOnScroll(e -> {
            double oldZoom = zoom; //save previous zoom level

            double factor = (e.getDeltaY() > 0) ? 1.1 : 0.9; //zoom in or zoom out depending direction
            zoom = clamp(zoom * factor, 0.2, 5.0); //update zoom but with limit

            double mouseX = e.getX();
            double mouseY = e.getY(); //cursor position in view

            double f = zoom / oldZoom; // how much zoom changed
            panX = mouseX - f * (mouseX - panX);
            panY = mouseY - f * (mouseY - panY); //adjust zoom so that zoom works around mouse not top left corner

            applyTransforms(); //apply zoom + pan to the map
            e.consume(); //prevent scroll effecting parent UI, not scrolling whole window
        });

        // Mouse drag pan
        setOnMousePressed(e -> { //when mouse is pressed
            //right button for pan
            if (!e.isSecondaryButtonDown()) return; //only pan when right click on mouse is pressed

            mouseStartX = e.getX();
            mouseStartY = e.getY(); //save starting mouse position

            panStartX = panX;
            panStartY = panY; //Save current pan values at the start of the drag
        });

        setOnMouseDragged(e -> { //runs while the mouse is being dragged
            if (!e.isSecondaryButtonDown()) return;

            panX = panStartX + (e.getX() - mouseStartX);
            panY = panStartY + (e.getY() - mouseStartY); //Updates panX/panY by adding the mouse movement (current - start) to the original value

            applyTransforms(); //apply so map moves immediately
        });

        // Quick reset (double click)
        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                zoom = 1.0;
                panX = 0;
                panY = 0; //default view values
                applyTransforms(); //apply to jump back to default view
                return;
            }

            Object target = e.getTarget();

            boolean clickedVehicle = (target instanceof Shape) && vehicleNodes.containsValue((Shape) target); // true if the user clicked a Circle AND that Circle is one of the vehicle circles created
            boolean clickedTrafficLight = (target instanceof Circle) && trafficLightNodes.containsValue((Circle) target); // true if the user clicked a Circle AND that Circle is one of the traffic light circles created

            // Clear selection unless a vehicle circle was clicked
            if (!clickedVehicle && !clickedTrafficLight) { //if the circle is not a vehicle or TL
                clearSelection(); //deselect
                clearTrafficLightSelection();
            }
        });



        // redraw roads when window is resized
        widthProperty().addListener((obs, o, n) -> redrawStaticLayers());
        heightProperty().addListener((obs, o, n) -> redrawStaticLayers());
    }

    //apply current zoom and pan to the whole map (WorldLayer)
    private void applyTransforms() {
        worldLayer.setScaleX(zoom);
        worldLayer.setScaleY(zoom); //zooms in/out

        worldLayer.setTranslateX(panX);
        worldLayer.setTranslateY(panY); //pans the map

    }

    private double clamp(double value, double min, double max) {return Math.max(min, Math.min(max, value)); //force zoom to stay in range (not less than min/ more than max, otherwise it returns value)
    }

    public String getSelectedTrafficLightId() {
        return selectedTrafficLightId;
    }

    //inject the loaded road network into the MapView
    public void setNetwork(NetworkModel network) {
        this.network = network; //store NetworkModel

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
        redrawStaticLayers(); //immediately redraw the static map elements
    }

    //redrawing the static map
    private void redrawStaticLayers() {
        if (network == null) return; //draw nothing until a network is loaded

        roadLayer.getChildren().clear(); //remove road shape to build them fresh
        trafficLightLayer.getChildren().clear();//remove light shape to build them fresh
        trafficLightNodes.clear(); // clear the Circle cache to rebuild TL circles on resize/reload

        double w = getWidth();
        double h = getHeight(); //get the current size of the Mapview
        if (w <= 0 || h <= 0) return; //if size=0, skip drawing to avoid bad scaling

        // compute SUMO map in world coordinates
        double rangeX = network.maxX - network.minX; //width of network
        double rangeY = network.maxY - network.minY; //height of network

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
                line.getPoints().addAll(sx, sy); //convert each SUMO point (x,y) into screen coordinates (sx,sy)
            }
            line.setStroke(Color.LIGHTGRAY); //set road color
            line.setStrokeWidth(15); //set thickness
            roadLayer.getChildren().add(line); //add polyline to roadLayer so it appears on the map
        }

        // Create traffic light circles once, color updated in renderTrafficLights()
        network.trafficLightNodes.forEach((id, p) -> {
            Circle c = new Circle(toScreenX(p.x()), toScreenY(p.y()), 8); //create a circle
            c.setFill(Color.DARKGRAY);     // placeholder until SUMO updates
            c.setStroke(Color.WHITE);      // always visible
            c.setStrokeWidth(1.5);

            c.setOnMouseClicked(e -> {
                selectTrafficLight(id, c);
                e.consume();
            });
            trafficLightNodes.put(id, c); //store it here for late to update the same circle’s color when the SUMO phase changes
            trafficLightLayer.getChildren().add(c); //add circle to trafficLightLayer so it becomes visible
        });
    }

    public void renderVehicles(List<Vehicle> vehicles) {
        if (network == null) return; //won’t draw vehicles until the network is loaded

        for (Vehicle v : vehicles) {
            String id = v.getId(); //unique ID as key
            latestVehicles.put(id, v); //the latest Vehicle object data saves here so other features (tooltip/selection) can read its data
            PositionVector p = v.getPositionVector(); //the vehicle’s current SUMO (x,y)

            double x = toScreenX(p.getX());
            double y = toScreenY(p.getY()); //convert SUMO coords

            Shape node = vehicleNodes.get(id); //check if this vehicle already has a shape drawn

            // Create node if missing (based on vehicle type)
            if (node == null) {
                String type = v.getTypeId(); // you already have this in SimulationManager logic

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
            Tooltip.install(node, tooltip); //attach created tooltip to the node so hovering shows it
            //update the tooltip text every tick with the latest data
            tooltip.setText(
                    "Vehicle ID: " + id +
                            "\nSpeed: " + String.format("%.2f", v.getSpeed()) + " m/s" +
                            "\nEdge: " + v.getEdgeId()
            );

            //Update position - move the vehicle node to its new position every tick (works for all Shapes)
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
    }

    // update traffic lights colors from SUMO state each tick
    public void renderTrafficLights(List<TrafficLight> tls) {
        if (network == null) return; //won’t update lights until the network (and positions) is loaded

        for (TrafficLight tl : tls) { //look for TL in backend
            String id = tl.getId(); //get ID as key

            Circle c = trafficLightNodes.get(id); //Look up the already-created circle
            if (c == null) {
                // Not in our network list (ID mismatch) -> ignore for now
                continue;
            }

            c.setFill(colorFromPhaseState(tl.getPhaseState())); //set circle’s color based on the current phase
        }

    }

    //convert SUMO’s traffic-light phase string into color in display
    private Color colorFromPhaseState(String state) {
        if (state == null || state.isBlank()) return Color.GRAY;

        char s = Character.toLowerCase(state.charAt(0));
        return switch (s) {
            case 'g' -> Color.LIMEGREEN; // If any green exists -> show green
            case 'y' -> Color.GOLD; // If any yellow exists -> show yellow
            case 'r' -> Color.RED; // If any red exists -> show red
            default -> Color.GRAY; // Otherwise gray
        };
    }


    //handle the highlighting
    private void selectVehicle(String id) {
        // Deselect old highlight
        if (selectedVehicleId != null) {
            Shape old = vehicleNodes.get(selectedVehicleId);
            if (old != null) {
                old.setStroke(null);
                old.setStrokeWidth(0);
            }
        }

        // Select new
        selectedVehicleId = id;
        Shape node = vehicleNodes.get(id);
        if (node != null) {
            node.setStroke(Color.RED);
            node.setStrokeWidth(2);
        }
    }
    //when mouse leaves vehicle's circle
    private void onVehicleMouseExit(String vehicleId, Shape node) {
        if (!vehicleId.equals(selectedVehicleId)) { //this vehicle is not the selected one? --> remove the hover highlight
            node.setStroke(null);
            node.setStrokeWidth(0);
        }
        setCursor(javafx.scene.Cursor.DEFAULT); //reset the mouse cursor back to the default arrow
    }


    //deselect the currently selected vehicle
    private void clearSelection() {
        if (selectedVehicleId == null) return; //if nothing is selected, then it's ok

        Shape node = vehicleNodes.get(selectedVehicleId);
        if (node != null) { //if a vehicle's circle is selected, reset its appearance
            node.setStroke(null);
            node.setStrokeWidth(0);
        }
        selectedVehicleId = null; //set to null so that no vehicle is selected
    }

    // select a traffic light (called when a TL circle is clicked)
    private void selectTrafficLight(String id, Circle node) {
        // clear old highlight
        if (selectedTrafficLightNode != null) {
            selectedTrafficLightNode.setStroke(null);
            selectedTrafficLightNode.setStrokeWidth(0);
        }

        selectedTrafficLightId = id; // store the selected traffic light ID
        selectedTrafficLightNode = node; // store the selected circle node (so we can un-highlight later)

        // highlight
        node.setStroke(Color.WHITE);
        node.setStrokeWidth(2);
    }

    // clears traffic light selection
    private void clearTrafficLightSelection() {
        if (selectedTrafficLightNode != null) {
            selectedTrafficLightNode.setStroke(null);
            selectedTrafficLightNode.setStrokeWidth(0);
        }
        selectedTrafficLightNode = null; // no selected node anymore
        selectedTrafficLightId = null; // no selected ID anymore
    }

    public void resetDynamicLayers() {
        vehicleLayer.getChildren().clear();
        vehicleNodes.clear();
        vehicleTooltips.clear();
        latestVehicles.clear();
        selectedVehicleId = null;

        // also clear TL selection highlight
        clearTrafficLightSelection();
    }



    //next two methods convert SUMO world coordinates into JavaFX screen coordinates
    private double toScreenX(double x) { //shift X so the map starts at margin and scales it
        return margin + (x - minX) * scale; //move the network’s left boundary to 0
    }

    // JavaFX y goes downward; SUMO y goes upward -> invert using maxY
    private double toScreenY(double y) {
        return margin + (maxY - y) * scale; //does the same but flips Y (maxY - y)
    }
}