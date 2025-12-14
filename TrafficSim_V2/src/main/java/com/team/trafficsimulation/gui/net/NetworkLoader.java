package com.team.trafficsimulation.gui.net;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

import static com.team.trafficsimulation.gui.net.NetworkModel.Point;

public class NetworkLoader {

    public static NetworkModel loadFromResources(String resourceName) {
        try (InputStream in = NetworkLoader.class.getResourceAsStream("/" + resourceName)) {
            if (in == null) throw new IllegalStateException("Resource not found: " + resourceName);

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            doc.getDocumentElement().normalize();

            // 1) Read convBoundary
            Element location = (Element) doc.getElementsByTagName("location").item(0);
            String[] b = location.getAttribute("convBoundary").split(",");
            double minX = Double.parseDouble(b[0]);
            double minY = Double.parseDouble(b[1]);
            double maxX = Double.parseDouble(b[2]);
            double maxY = Double.parseDouble(b[3]);

            // 2) Read lane shapes (skip internal edges if you want)
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

            // 3) Read traffic-light junction positions
            Map<String, Point> tlNodes = new HashMap<>();
            NodeList junctionNodes = doc.getElementsByTagName("junction");
            for (int i = 0; i < junctionNodes.getLength(); i++) {
                Element junc = (Element) junctionNodes.item(i);
                if (!"traffic_light".equals(junc.getAttribute("type"))) continue;

                String id = junc.getAttribute("id");
                double x = Double.parseDouble(junc.getAttribute("x"));
                double y = Double.parseDouble(junc.getAttribute("y"));
                tlNodes.put(id, new Point(x, y));
            }

            return new NetworkModel(minX, minY, maxX, maxY, polylines, tlNodes);

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
