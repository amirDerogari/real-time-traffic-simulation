package trafficsimulation.gui;

import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MapView extends Pane {

    private static final double CAR_W = 44;
    private static final double CAR_H = 24;

    // Road geometry (must match what we draw)
    private static final double ROAD_H_Y = 200;
    private static final double ROAD_H_X1 = 80;
    private static final double ROAD_H_X2 = 820;

    private static final double ROAD_V_X = 450;
    private static final double ROAD_V_Y1 = 80;
    private static final double ROAD_V_Y2 = 720;

    private static class MovingCar {
        CarSprite sprite;
        boolean horizontal;  // true = horizontal road, false = vertical road
        double speed;        // pixels per frame
        int dir;             // +1 or -1
    }

    private final List<MovingCar> cars = new ArrayList<>();

    public MapView() {
        setStyle("-fx-background-color: #2b2b2b;");
        setPrefSize(900, 800);

        drawDemoRoads();
        placeCarsInSpecificOrderDemo();

        startAnimation();
    }

    private void drawDemoRoads() {
        Line roadH = new Line(ROAD_H_X1, ROAD_H_Y, ROAD_H_X2, ROAD_H_Y);
        roadH.setStroke(Color.LIGHTGRAY);
        roadH.setStrokeWidth(14);

        Line roadV = new Line(ROAD_V_X, ROAD_V_Y1, ROAD_V_X, ROAD_V_Y2);
        roadV.setStroke(Color.LIGHTGRAY);
        roadV.setStrokeWidth(14);

        getChildren().addAll(roadH, roadV);
    }

    private void placeCarsInSpecificOrderDemo() {
        // Put the ordered cars ON the horizontal road, spaced nicely
        double startX = 140;
        double gap = 85;

        for (int i = 0; i < CarAssets.ORDERED.size(); i++) {
            double x = startX + i * gap;

            // Clamp so they don’t start outside road
            if (x < ROAD_H_X1 + 20) x = ROAD_H_X1 + 20;
            if (x > ROAD_H_X2 - 20) x = ROAD_H_X2 - 20;

            int dir = (i % 2 == 0) ? +1 : -1;

            addMovingCarOnHorizontalRoad(CarAssets.ORDERED.get(i), x, ROAD_H_Y, dir);
        }
    }

    private void startAnimation() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateCars();
            }
        };
        timer.start();
    }

    private void updateCars() {
        for (MovingCar c : cars) {
            if (c.horizontal) {
                double x = (c.sprite.getLayoutX() + CAR_W / 2.0) + (c.speed * c.dir);
                double y = ROAD_H_Y;

                // bounce at road ends
                if (x < ROAD_H_X1) { x = ROAD_H_X1; c.dir = +1; c.sprite.setHeadingDegrees(0); }
                if (x > ROAD_H_X2) { x = ROAD_H_X2; c.dir = -1; c.sprite.setHeadingDegrees(180); }

                c.sprite.setCenterPosition(x, y);

            } else {
                double x = ROAD_V_X;
                double y = (c.sprite.getLayoutY() + CAR_H / 2.0) + (c.speed * c.dir);

                // bounce at road ends
                if (y < ROAD_V_Y1) { y = ROAD_V_Y1; c.dir = +1; c.sprite.setHeadingDegrees(90); }
                if (y > ROAD_V_Y2) { y = ROAD_V_Y2; c.dir = -1; c.sprite.setHeadingDegrees(270); }

                c.sprite.setCenterPosition(x, y);
            }
        }
    }

    private void addMovingCarOnHorizontalRoad(String path, double x, double y, int dir) {
        CarSprite sprite = new CarSprite(path, CAR_W, CAR_H);
        sprite.setCenterPosition(x, y);
        sprite.setHeadingDegrees(dir > 0 ? 0 : 180);

        MovingCar mc = new MovingCar();
        mc.sprite = sprite;
        mc.horizontal = true;
        mc.dir = dir;
        mc.speed = ThreadLocalRandom.current().nextDouble(0.2, 0.8);

        cars.add(mc);
        getChildren().add(sprite);
    }

    private void addMovingCarOnVerticalRoad(String path, double x, double y, int dir) {
        CarSprite sprite = new CarSprite(path, CAR_W, CAR_H);
        sprite.setCenterPosition(x, y);
        sprite.setHeadingDegrees(dir > 0 ? 90 : 270);

        MovingCar mc = new MovingCar();
        mc.sprite = sprite;
        mc.horizontal = false;
        mc.dir = dir;
        mc.speed = ThreadLocalRandom.current().nextDouble(0.2, 0.8);

        cars.add(mc);
        getChildren().add(sprite);
    }

    /** Called from ControlPanel: inject a random car ON a road and make it move */
    public void injectRandomCarOnRoad() {
        int idx = ThreadLocalRandom.current().nextInt(CarAssets.ORDERED.size());
        String path = CarAssets.ORDERED.get(idx);

        boolean horizontal = ThreadLocalRandom.current().nextBoolean();

        if (horizontal) {
            double x = ThreadLocalRandom.current().nextDouble(ROAD_H_X1 + 20, ROAD_H_X2 - 20);
            int dir = ThreadLocalRandom.current().nextBoolean() ? +1 : -1;
            addMovingCarOnHorizontalRoad(path, x, ROAD_H_Y, dir);
        } else {
            double y = ThreadLocalRandom.current().nextDouble(ROAD_V_Y1 + 20, ROAD_V_Y2 - 20);
            int dir = ThreadLocalRandom.current().nextBoolean() ? +1 : -1;
            addMovingCarOnVerticalRoad(path, ROAD_V_X, y, dir);
        }
    }
}
