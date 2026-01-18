package trafficsimulation.gui.net;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;
import static trafficsimulation.gui.net.NetworkModel.Point;

/**
 * Loads a SUMO network XML file (e.g. *.net.xml) from the classpath resources and converts it into a
 * {@link NetworkModel} that can be rendered in the GUI.
 *
 * <p>The loader reads:
 * <ul>
 *   <li>the converted boundary (convBoundary) to determine map scaling,</li>
 *   <li>lane shapes as polylines (used to draw roads),</li>
 *   <li>junction shapes as polygons (used to connect roads at intersections),</li>
 *   <li>traffic-light junction positions (used to display/identify traffic lights).</li>
 * </ul>
 *
 * <p>Internal edges (SUMO edges with function="internal") are skipped to reduce visual clutter.
 */
public class NetworkLoader {

    /**
     * Loads a SUMO network file from the application's resources.
     *
     * <p>The file is expected to be available on the classpath (e.g. inside {@code src/main/resources}).
     *
     * @param resourceName name/path of the resource (e.g. {@code "nets/city.net.xml"})
     * @return a fully populated {@link NetworkModel} containing boundary, road polylines, junction polygons,
     *         and traffic light positions
     * @throws IllegalStateException if the resource cannot be found or required XML elements are missing
     * @throws RuntimeException if parsing fails for any reason (e.g. malformed XML, number format issues)
     */
    public static NetworkModel loadFromResources(String resourceName) {
        try (InputStream in = NetworkLoader.class.getResourceAsStream("/" + resourceName)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found: " + resourceName);
            }

            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(in);
            doc.getDocumentElement().normalize();

            //Read convBoundary
            Element location = (Element) doc.getElementsByTagName("location").item(0);
            String[] b = location.getAttribute("convBoundary").split(",");
            double minX = Double.parseDouble(b[0]);
            double minY = Double.parseDouble(b[1]);
            double maxX = Double.parseDouble(b[2]);
            double maxY = Double.parseDouble(b[3]);

            //Read lane shapes (skip internal edges if you want)
            List<List<Point>> polylines = new ArrayList<>();
            NodeList edgeNodes = doc.getElementsByTagName("edge");

            for (int i = 0; i < edgeNodes.getLength(); i++) {
                Element edge = (Element) edgeNodes.item(i);

                // Skip internal edges (they clutter the drawing)
                if ("internal".equals(edge.getAttribute("function"))) continue;

                NodeList laneNodes = edge.getElementsByTagName("lane");
                for (int j = 0; j < laneNodes.getLength(); j++) {
                    Element lane = (Element) laneNodes.item(j);
                    String shape = lane.getAttribute("shape");
                    if (shape == null || shape.isBlank()) continue;

                    polylines.add(parseShape(shape));
                }
            }

            //Read junction polygons + traffic-light positions
            Map<String, Point> tlNodes = new HashMap<>();
            List<List<Point>> junctionPolygons = new ArrayList<>();
            NodeList junctionNodes = doc.getElementsByTagName("junction");

            for (int i = 0; i < junctionNodes.getLength(); i++) {
                Element junc = (Element) junctionNodes.item(i);

                if (!"traffic_light".equals(junc.getAttribute("type"))) continue;

                // Some nets have junction@tl (tlLogic id), some don't.
                // If tl exists -> use it, else use junction id.
                String tlId = junc.getAttribute("tl");
                if (tlId == null || tlId.isBlank()) {
                    tlId = junc.getAttribute("id");
                }
                if (!tlId.startsWith("TL_")) {
                    tlId = "TL_" + tlId;
                }
                double x = Double.parseDouble(junc.getAttribute("x"));
                double y = Double.parseDouble(junc.getAttribute("y"));
                tlNodes.put(tlId, new Point(x, y));
            }
            // to see in console to debug if there is a problem with TL
            System.out.println("Loaded TL nodes: " + tlNodes.keySet());

            for (int i = 0; i < junctionNodes.getLength(); i++) {
                Element junc = (Element) junctionNodes.item(i);

                String shape = junc.getAttribute("shape");
                if (shape == null || shape.isBlank()) continue;

                // Optional: skip internal junctions
                // if (junc.getAttribute("id").startsWith(":")) continue;

                junctionPolygons.add(parseShape(shape));
            }

            return new NetworkModel(minX, minY, maxX, maxY, polylines, junctionPolygons, tlNodes);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load network: " + resourceName, e);
        }
    }

    /**
     * Parses a SUMO {@code shape} attribute into a list of {@link Point}s.
     *
     * <p>SUMO encodes shapes as whitespace-separated coordinate pairs:
     * {@code "x,y x,y x,y ..."}.
     *
     * @param shape the raw shape string from the SUMO network XML
     * @return a list of points in the same order as in the input string
     * @throws NumberFormatException if any coordinate value cannot be parsed as a {@code double}
     */
    private static List<Point> parseShape(String shape) {
        String[] parts = shape.trim().split("\\s+");
        List<Point> pts = new ArrayList<>(parts.length);

        for (String p : parts) {
            String[] xy = p.split(",");
            pts.add(new Point(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
        }
        return pts;
    }
}
