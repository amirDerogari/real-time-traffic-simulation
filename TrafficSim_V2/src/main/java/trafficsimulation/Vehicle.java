package trafficsimulation;

import org.eclipse.sumo.libtraci.StringVector;

public class Vehicle {

    //Attributes
    private final String id;
    private final String type;

    //Constructor
    public Vehicle(String id) {
        this.id = id;
        this.type = org.eclipse.sumo.libtraci.Vehicle.getTypeID(id);
    }

    //Getter
    public String getId() {
        return this.id;
    }

    public double getSpeed() {
        return org.eclipse.sumo.libtraci.Vehicle.getSpeed(this.id);
    }

    public PositionVector getPositionVector(){
        return new PositionVector(org.eclipse.sumo.libtraci.Vehicle.getPosition(this.id).getX(), org.eclipse.sumo.libtraci.Vehicle.getPosition(this.id).getY());
    }

    public String getEdgeId() {
        return org.eclipse.sumo.libtraci.Vehicle.getRoadID(this.id);
    }

    public String getRoadId() {return org.eclipse.sumo.libtraci.Vehicle.getRoadID(this.id);}

    //@return Type (DEFAULT_VEHTYPE, bus_standard, emergency)
    public String getTypeId() {return this.type;}


    //Setter methods
    public void setSpeed(double speed) {
        org.eclipse.sumo.libtraci.Vehicle.setSpeed(this.id, speed);
    }
}

