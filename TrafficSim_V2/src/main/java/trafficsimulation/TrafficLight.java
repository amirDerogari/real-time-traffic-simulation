package trafficsimulation;

public class TrafficLight {

    //Attributes and Constructor
    private final String id;

    public TrafficLight(String id) {
        this.id = id;
    }

    //getter
    public String getId() {
        return this.id;
    }

    public String getPhaseState(){
        return org.eclipse.sumo.libtraci.TrafficLight.getRedYellowGreenState(this.id);
    }

    public String getProgramId() {
        return org.eclipse.sumo.libtraci.TrafficLight.getProgram(this.id);
    } //standard:0

    //setter
    public void setPhaseState(String state) {
        org.eclipse.sumo.libtraci.TrafficLight.setRedYellowGreenState(this.id, state);
    }


    public void setPhaseIndex(int phaseIndex) {
        org.eclipse.sumo.libtraci.TrafficLight.setPhase(this.id, phaseIndex);
    }


    //here we will set the new created programs
    public void setProgram(String programID) {
        org.eclipse.sumo.libtraci.TrafficLight.setProgram(this.id, programID);
    }

}
