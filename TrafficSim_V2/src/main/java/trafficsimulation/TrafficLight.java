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


    /**
     * Forces a switch to next lightphase
     * Extracts trafficlight phases and switches between them depending on amount
     * created for manual switches
     */
    public void forceNextPhase(){
        TraCILogicVector logicVector = org.eclipse.sumo.libtraci.TrafficLight.getCompleteRedYellowGreenDefinition(this.id); //TraciLogicVector
        TraCILogic logic = logicVector.get(0);
        int numberOfPhases = logic.getPhases().size();
        System.out.println("phase count: " + numberOfPhases);
        int current = this.getPhase(); //0,1,2
        System.out.println("current phase: " + current);
        int nextPhase = (current+1)%numberOfPhases;
        System.out.println("next phase: " + nextPhase);
        this.setPhaseIndex(nextPhase);
    }

    /**
     * Forces a phase state for a given duration
     * @param phaseIndex Index of the phase to perform
     * @param duration Length of steps to hold the state
     */
    public void forceGreenPhase(int phaseIndex, int duration){
        setPhaseIndex(phaseIndex);
        setPhaseDuration(duration);
        //phases indexes that make main lane green:
        //TL_CR1 = 0
        //TL_CR2 = 0
    }


}
