package trafficsimulation;

import org.eclipse.sumo.libtraci.Simulation;

public class Test {

    private static final String CONFIG2 = "src/main/resources/final_map.sumocfg";

    public static void main(String[] args) {

        //SIMPLE DEMONSTRATIONS

        Simulation.preloadLibraries();
        SimulationManager manager = new SimulationManager(CONFIG2);
        manager.startSimulation();

        manager.step();


        for(int step=0; step<1000; step++ ) {
            manager.step();

            if (step == 40 || step == 44) {
                manager.spawnEmergencyVehicle();
                //manager.runRushHour();
                //green: CR1=0:GGgrrrGGgrrr and CR2=0:GGgrrrGGgrrr
                //towards CR1: WI1, towards CR2: ONETOTWO

            }
        }

        /*
        //EXAMPLE force TL-States (Success!) on Beispiel2
        List<TrafficLight> tls = manager.getAllTrafficLights();
        for (TrafficLight tl : tls) {
            System.out.println(tl.getId());
        }

        for (int steps = 0; steps < 100; steps++) {
            manager.step();
            if(steps%5==0){
                System.out.println("Forcing");
                tls.get(0).forceNextPhase(); //top/bottom
            }
        }
        /*


        /* EXAMPLE PRINT POSITIONS
        for(int i=0; i<1000; i++) {
            List<Vehicle> vehicles = manager.getVehicles();
            if(i%8 == 0 && !vehicles.isEmpty()) {
                for(Vehicle vehicle : vehicles){
                    System.out.println("Car " + vehicle.getId() + "an Position: "+ vehicle.getPositionVector().getX() + ", " + vehicle.getPositionVector().getY());
                }
            }
            manager.step();
        }
        */

        manager.closeSimulation();

    }

}
