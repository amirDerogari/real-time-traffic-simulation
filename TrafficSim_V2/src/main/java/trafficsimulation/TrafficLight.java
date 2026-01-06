package trafficsimulation;

import org.eclipse.sumo.libtraci.TraCILogic;
import org.eclipse.sumo.libtraci.TraCILogicVector;

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

    //@return Program ID with Default value 0
    public String getProgramId() {
        return org.eclipse.sumo.libtraci.TrafficLight.getProgram(this.id);
    }

    public int getPhase(){ return org.eclipse.sumo.libtraci.TrafficLight.getPhase(this.id); }

    //setter
    //@param eg. "GgrrGG"
    public void setPhaseState(String state) {
        org.eclipse.sumo.libtraci.TrafficLight.setRedYellowGreenState(this.id, state);
    }

    //@param Related phase index to each phaseState
    public void setPhaseIndex(int phaseIndex) {
        org.eclipse.sumo.libtraci.TrafficLight.setPhase(this.id, phaseIndex);
    }

    //@param Default = 0, example = night/day
    public void setProgram(String programID) {
        org.eclipse.sumo.libtraci.TrafficLight.setProgram(this.id, programID);
    }

    //nextPhase (for GUI Button)
    public void forceNextPhase(){
        //String programID = this.getProgramId(); //default = 0
        TraCILogicVector logicVector = org.eclipse.sumo.libtraci.TrafficLight.getCompleteRedYellowGreenDefinition(this.id); //TraciLogicVector
        int programsCount = logicVector.size(); //amount of phases
        TraCILogic logic = logicVector.get(0);
        int numberOfPhases = logic.getPhases().size();
        System.out.println("phase count: " + numberOfPhases);
        int current = this.getPhase(); //0,1,2
        System.out.println("current phase: " + current);
        int nextPhase = (current+1)%numberOfPhases;
        System.out.println("next phase: " + nextPhase);
        this.setPhaseIndex(nextPhase);
    }

}
