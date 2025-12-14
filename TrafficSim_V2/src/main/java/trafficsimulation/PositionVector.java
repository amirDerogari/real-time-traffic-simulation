package trafficsimulation;

//Simple 2D Vector class to store x,y positions of objects after calling getX and getY functions

public class PositionVector {
    private final double x;
    private final double y;

    public PositionVector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

}