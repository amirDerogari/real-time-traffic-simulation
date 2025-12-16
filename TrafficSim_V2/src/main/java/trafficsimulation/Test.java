package trafficsimulation;

import org.eclipse.sumo.libtraci.Simulation;

import java.util.List;

public class Test {


    private static final String CONFIG2 = "src/main/resources/beispiel2config.sumocfg";

    public static void main(String[] args) {


        //SIMPLE DEMONSTRATION OF VEHICLE FUNCTIONS

        Simulation.preloadLibraries();
        SimulationManager manager = new SimulationManager(CONFIG2);
        manager.startSimulation();

        manager.step();

        for(int i=0; i<30; i++) {
            List<Vehicle> vehicles = manager.getVehicles();
            if(i%8 == 0 && !vehicles.isEmpty()) {
                for(Vehicle vehicle : vehicles){
                    System.out.println("Car " + vehicle.getId() + "an Position: "+ vehicle.getPositionVector().getX() + ", " + vehicle.getPositionVector().getY());
                }
            }
            manager.step();
        }
        manager.closeSimulation();

    }

}
