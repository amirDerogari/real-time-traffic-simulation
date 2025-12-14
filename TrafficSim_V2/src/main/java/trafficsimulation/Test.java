package trafficsimulation;

import org.eclipse.sumo.libtraci.Simulation;

import java.util.List;

public class Test {

    private static final String CONFIG = "src/main/resources/beispiel2config.sumocfg";

    public static void main(String[] args) {

        Simulation.preloadLibraries();
        SimulationManager manager = new SimulationManager(CONFIG);
        manager.startSimulation();

        manager.step();

        //System.out.println("Test finished.");

        for(int i=0; i<20; i++) {
            List<Vehicle> vehicles = manager.getVehicles();
            if(i%8 == 0 && !vehicles.isEmpty()) {
                for(Vehicle vehicle : vehicles){
                    System.out.println("Car " + vehicle.getId() + "an Position: "+ vehicle.getPositionVector().getX() + ", " + vehicle.getPositionVector().getY());
                }
            }
            manager.step();
        }


        //HERE EXAMPLE OF TRAFIC LIGHT CONTROL
        //AND CORE VEHICLE METHODS

        manager.closeSimulation();

    }

}
