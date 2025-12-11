package com.team.trafficsimulation;

import org.eclipse.sumo.libtraci.TraCIConnection; //steuert die Verbindung
import org.eclipse.sumo.libtraci.StringVector;
import org.eclipse.sumo.libtraci.Simulation; //für preloadlibaries()
import org.eclipse.sumo.libtraci.GUI;
import org.eclipse.sumo.libtraci.Vehicle;

public class TrafficSimulatorApp {

    public static void main(String[] args) {

        // Pfad zur SUMO Konfigurationsdatei
        String sumoConfigPath = "src/main/resources/beispiel2config.sumocfg";

        //Bibliotheken Test
        try {
            Simulation.preloadLibraries();
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der libtraci Bibliotheken: " + e.getMessage());
        }

        //Simulation Start
        String[] command = {"sumo-gui", "-c", sumoConfigPath, "--delay", "200"};
        StringVector commandVector = new StringVector(command);



        //
        System.out.println("Starte SUMO: " + sumoConfigPath);
        Simulation.start(commandVector);

        for(int i=0; i<50; i++) {
            Simulation.step();
        }

        Simulation.close();



    }
}
