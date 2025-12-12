package com.team.trafficsimulation;

import org.eclipse.sumo.libtraci.Simulation; //für preloadlibaries()

import java.util.List;


public class TrafficSimulatorApp {

    //config Path
    private static final String CONFIG = "src/main/resources/beispiel2config.sumocfg";

    public static void main(String[] args) {

        //Preload Libraries
        try {
            Simulation.preloadLibraries();
        } catch (Exception e) {
            System.err.println("Failed Library loading: " + e.getMessage());
        }

        //Simulation
        SimulationManager manager = new SimulationManager(CONFIG);
        manager.startSimulation();

        //do i steps
        for (int i=0; i<30; i++) {
            manager.step();

            //Example: Print Positions
            List<Vehicle> vehicles = manager.getVehicles();
            for (Vehicle v : vehicles) {
                PositionVector pos = v.getPositionVector();

                if((i%10) == 0){
                    System.out.println(pos.getX() + " " + pos.getY());
                }
            }
        }

    manager.closeSimulation();

    }
}
