package trafficsimulation;

import org.eclipse.sumo.libtraci.TraCILogic;
import org.eclipse.sumo.libtraci.TraCILogicVector;

public class TrafficLight {

    //Attributes
    private final String id;

    //Constructor
    public TrafficLight(String id) {
        this.id = id;
    }

    //Getter
    public String getId() {
        return this.id;
    }

    public String getPhaseState(){
        return org.eclipse.sumo.libtraci.TrafficLight.getRedYellowGreenState(this.id);
    }

    public int getPhase(){ return org.eclipse.sumo.libtraci.TrafficLight.getPhase(this.id); }

    //Setter
    public void setPhaseIndex(int phaseIndex) {
        org.eclipse.sumo.libtraci.TrafficLight.setPhase(this.id, phaseIndex);
    }

    public void setPhaseDuration(int phaseDuration) { org.eclipse.sumo.libtraci.TrafficLight.setPhaseDuration(this.id, phaseDuration); }


    //NextPhase (for GUI Button)
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


    //rush-hour function: prioritize traffic on main stream
    public void forceGreenPhase(int phaseIndex, int duration){
        setPhaseIndex(phaseIndex);
        setPhaseDuration(duration);
        //phases indexes that make main lane green:
        //TL_CR1 = 0
        //TL_CR2 = 0
    }


}
