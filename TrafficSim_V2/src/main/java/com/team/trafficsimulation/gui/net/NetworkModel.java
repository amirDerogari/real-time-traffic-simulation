package com.team.trafficsimulation.gui.net;

import java.util.List;
import java.util.Map;

public class NetworkModel {
    public final double minX, minY, maxX, maxY;
    public final List<List<Point>> polylines;          // roads
    public final Map<String, Point> trafficLightNodes; // junctionId -> position

    public NetworkModel(double minX, double minY, double maxX, double maxY,
                        List<List<Point>> polylines,
                        Map<String, Point> trafficLightNodes) {
        this.minX = minX; this.minY = minY; this.maxX = maxX; this.maxY = maxY;
        this.polylines = polylines;
        this.trafficLightNodes = trafficLightNodes;
    }

    public record Point(double x, double y) {}
}
