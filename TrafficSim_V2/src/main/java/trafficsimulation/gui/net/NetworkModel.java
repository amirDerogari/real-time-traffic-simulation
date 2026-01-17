package trafficsimulation.gui.net;

import java.util.List;
import java.util.Map;

// this is a simple data container for everything the GUI needs to draw the map
public class NetworkModel {
    public final double minX, minY, maxX, maxY;
    public final List<List<Point>> polylines;          // roads
    public final List<List<Point>> junctionPolygons; // junction areas (polygons)
    public final Map<String, Point> trafficLightNodes; // junctionId -> position

    public NetworkModel(double minX, double minY, double maxX, double maxY,
                        List<List<Point>> polylines,
                        List<List<Point>> junctionPolygons,
                        Map<String, Point> trafficLightNodes) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        this.polylines = polylines;
        this.junctionPolygons = junctionPolygons;
        this.trafficLightNodes = trafficLightNodes;
    }

    public record Point(double x, double y) {} //tiny immutable type to represent a 2D coordinate
}
