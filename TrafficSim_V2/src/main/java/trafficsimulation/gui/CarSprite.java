package trafficsimulation.gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CarSprite extends ImageView {

    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    public CarSprite(String resourcePath, double width, double height) {
        super(load(resourcePath));

        setPreserveRatio(true);
        setFitWidth(width);
        setFitHeight(height);
        setSmooth(true);
    }

    private static Image load(String resourcePath) {
        return CACHE.computeIfAbsent(resourcePath, path -> {
            var stream = CarSprite.class.getResourceAsStream(path);
            if (stream == null) {
                throw new IllegalArgumentException("Image not found on classpath: " + path);
            }
            return new Image(stream);
        });
    }

    /** Places the car so that (x,y) is its CENTER point. */
    public void setCenterPosition(double x, double y) {
        setLayoutX(x - getFitWidth() / 2.0);
        setLayoutY(y - getFitHeight() / 2.0);
    }

    /** Rotates the image (degrees). 0 means facing right. */
    public void setHeadingDegrees(double deg) {
        setRotate(deg);
    }
}

