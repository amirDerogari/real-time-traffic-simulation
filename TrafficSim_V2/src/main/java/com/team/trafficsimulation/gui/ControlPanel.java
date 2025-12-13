package com.team.trafficsimulation.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Right-side GUI controls (Person B).
 * For now it's demo-only: inject random moving cars on the roads.
 * Later, replace Object controller with SimulationController when Person A finishes it.
 */
public class ControlPanel extends VBox {

    public ControlPanel(Object controller, MapView mapView) {

        // Panel layout
        setPadding(new Insets(12));
        setSpacing(10);
        setPrefWidth(280);
        setStyle("-fx-background-color: #3c3f41;");

        // Title
        Label title = new Label("Controls");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Buttons
        Button injectRandomBtn = new Button("Inject Random Car (On Road)");
        injectRandomBtn.setMaxWidth(Double.MAX_VALUE);

        Button injectTenBtn = new Button("Inject 10 Cars");
        injectTenBtn.setMaxWidth(Double.MAX_VALUE);

        Button clearBtn = new Button("Clear Cars (Demo)");
        clearBtn.setMaxWidth(Double.MAX_VALUE);

        // Actions
        injectRandomBtn.setOnAction(e -> mapView.injectRandomCarOnRoad());

        injectTenBtn.setOnAction(e -> {
            for (int i = 0; i < 10; i++) {
                mapView.injectRandomCarOnRoad();
            }
        });

        // If you did NOT implement clear yet, just keep this as a placeholder.
        // Later I can add a proper clearCars() method to MapView.
        clearBtn.setOnAction(e -> {
            System.out.println("Clear pressed (not implemented yet).");
        });

        // Spacer so buttons stay at the top
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Add to panel
        getChildren().addAll(
                title,
                new Separator(),
                injectRandomBtn,
                injectTenBtn,
                clearBtn,
                spacer
        );
    }
}
