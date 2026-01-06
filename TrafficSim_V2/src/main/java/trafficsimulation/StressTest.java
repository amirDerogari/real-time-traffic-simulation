package trafficsimulation;

import org.eclipse.sumo.libtraci.Simulation;

import java.util.List;

public class StressTest {

    private static final String CONFIG = "src/main/resources/stauconfig.sumocfg";

    public static void main(String[] args) {

        //STRESS TEST: Small (Stau)
        Simulation.preloadLibraries();
        SimulationManager manager = new SimulationManager(CONFIG);

        //start Simulation
        manager.startSimulation();

        List<TrafficLight> tls = manager.getAllTrafficLights();
        for (TrafficLight tl : tls) {
            System.out.println(tl.getId());
        }

        StressTest test = new StressTest();

        System.out.println("Running traffic scenario");
        //test.goodTraffic(manager, tls.get(0), tls.get(1));
        //test.badTraffic(manager, tls.get(0), tls.get(1));
        test.phaseSwitching(manager, tls.get(0), tls.get(1));
        manager.closeSimulation();
    }

    public void phaseSwitching(SimulationManager sim, TrafficLight tlB, TrafficLight tlA) {

        System.out.println("Starting");

        for (int step = 0; step < 3600; step++) {
            sim.step();
            tlA.forceNextPhase();
            tlB.forceNextPhase();
        }
        System.out.println("finished.\n");
    }


    public void goodTraffic(SimulationManager sim, TrafficLight tlB, TrafficLight tlA) {

        System.out.println("GOOD traffic");

        for (int step = 0; step < 3600; step++) {
            sim.step();

            //0=G; 1=g; 2=r

            // long green phase for main road
            if (step % 50 < 40) { //cycle (40 from 50steps)
                tlB.setPhaseIndex(0);
                tlA.setPhaseIndex(0);
            } else {
                tlB.setPhaseIndex(2);
                tlA.setPhaseIndex(2);
            }
        }
        System.out.println("finished.\n");
    }

    public void badTraffic(SimulationManager sim, TrafficLight tlB, TrafficLight tlA) {

        System.out.println("BAD traffic");

        for (int step = 0; step < 3600; step++) {
            sim.step();

            //0=G; 1=g; 2=r

            //
            if (step % 60 < 10) { //cyle (10 from 60 steps for mainroad)
                tlB.setPhaseIndex(0);
                tlA.setPhaseIndex(0);
            } else {
                tlB.setPhaseIndex(2);
                tlA.setPhaseIndex(2);
            }
        }
        System.out.println("finished.\n");
    }


}
