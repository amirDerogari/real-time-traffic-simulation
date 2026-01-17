package trafficsimulation.gui.net;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

import static trafficsimulation.gui.net.NetworkModel.Point;

public class NetworkLoader {

    public static NetworkModel loadFromResources(String resourceName) {
        try (InputStream in = NetworkLoader.class.getResourceAsStream("/" + resourceName)) {
            if (in == null) throw new IllegalStateException("Resource not found: " + resourceName);

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
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

            //Read traffic-light junction positions
            Map<String, Point> tlNodes = new HashMap<>();
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

            // Read junction shapes (to visually connect roads at intersections)
            List<List<Point>> junctionPolygons = new ArrayList<>();

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

    private static List<Point> parseShape(String shape) {
        // "x,y x,y x,y ..."
        String[] parts = shape.trim().split("\\s+");
        List<Point> pts = new ArrayList<>(parts.length);
        for (String p : parts) {
            String[] xy = p.split(",");
            pts.add(new Point(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
        }
        return pts;
    }
}
