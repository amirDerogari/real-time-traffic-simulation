package trafficsimulation.gui.net;

import java.util.List;
import java.util.Map;

/**
 * Immutable data container that holds all geometry and metadata the GUI needs to draw a SUMO road network.
 *
 * <p>The model contains:
 * <ul>
 *   <li>the network boundary (min/max coordinates) used for scaling and coordinate transforms,</li>
 *   <li>road geometries as polylines (each lane/road is a list of points),</li>
 *   <li>junction areas as polygons (each junction is a list of points),</li>
 *   <li>traffic light node positions mapped by a traffic light id.</li>
 * </ul>
 */
public class NetworkModel {

    /** Minimum and Maximum x/y-coordinate of the network boundary in SUMO world coordinates. */
    public final double minX, minY, maxX, maxY;

    /** Road geometries represented as polylines (each polyline is a list of {@link Point}s). */
    public final List<List<Point>> polylines;          // roads

    /** Junction geometries represented as polygons (each polygon is a list of {@link Point}s). */
    public final List<List<Point>> junctionPolygons; // junction areas (polygons)

    /** Traffic light node positions, mapped by traffic light id (e.g. "TL_..."). */
    public final Map<String, Point> trafficLightNodes; // junctionId -> position

    /**
     * Creates a new {@code NetworkModel}.
     *
     * @param minX minimum x-coordinate of the network boundary
     * @param minY minimum y-coordinate of the network boundary
     * @param maxX maximum x-coordinate of the network boundary
     * @param maxY maximum y-coordinate of the network boundary
     * @param polylines road polylines to draw
     * @param junctionPolygons junction polygons to draw
     * @param trafficLightNodes mapping from traffic light id to its world position
     */
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
