package trafficsimulation;

import org.eclipse.sumo.libtraci.StringVector;

public class Vehicle {

    //Attributes and Constructor
    private final String id;
    public Vehicle(String id) {
        this.id = id;
    }
    public String getId() {
        return this.id;
    }

    //Getter methods
    public double getSpeed() {
        return org.eclipse.sumo.libtraci.Vehicle.getSpeed(this.id);
    }

    public PositionVector getPositionVector(){
        return new PositionVector(org.eclipse.sumo.libtraci.Vehicle.getPosition(this.id).getX(), org.eclipse.sumo.libtraci.Vehicle.getPosition(this.id).getY());
    }

    public String getEdgeId() {
        return org.eclipse.sumo.libtraci.Vehicle.getRoadID(this.id);
    }


    //Setter methods
    public void setSpeed(double speed) {
        org.eclipse.sumo.libtraci.Vehicle.setSpeed(this.id, speed);
    }

    //necessary?
    public void changeLane(int laneIndex, double duration) {
        org.eclipse.sumo.libtraci.Vehicle.changeLane(this.id, laneIndex, duration);
    }


    //inject vehicle!
    //amount == length of list getVehicles

    //to change route
    public void setRoute(StringVector edgeIds) {
        org.eclipse.sumo.libtraci.Vehicle.setRoute(this.id, edgeIds);
    }

    //setroute overload?
}

