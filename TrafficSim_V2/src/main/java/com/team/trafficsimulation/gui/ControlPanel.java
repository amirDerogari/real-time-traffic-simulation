package com.team.trafficsimulation.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class ControlPanel extends VBox {

    public ControlPanel(Object controller, MapView mapView) {
        setPadding(new Insets(10));
        setSpacing(10);
        setPrefWidth(280);

        Button connect = new Button("Connect");
        Button start = new Button("Start");
        Button stop = new Button("Stop");

        connect.setMaxWidth(Double.MAX_VALUE);
        start.setMaxWidth(Double.MAX_VALUE);
        stop.setMaxWidth(Double.MAX_VALUE);

        connect.setOnAction(e -> System.out.println("Connect clicked (backend later)"));
        start.setOnAction(e -> System.out.println("Start clicked (backend later)"));
        stop.setOnAction(e -> System.out.println("Stop clicked (backend later)"));

        getChildren().addAll(connect, start, stop);
    }
}
