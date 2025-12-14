package trafficsimulation;

import org.eclipse.sumo.libtraci.Simulation;
import org.eclipse.sumo.libtraci.StringVector; //dynamic and can reserve memory

import java.util.ArrayList;
import java.util.List;

//Factory for Instances of Vehicles and TrafficLights
//Start and stop SUMO Simulation

public class SimulationManager {

    //Sumo and Net-Config
    private final String configPath;

    // connection flag (Mojtaba added)
    private volatile boolean connected = false;

    //Constructor
    public SimulationManager(String configPath) {
        this.configPath = configPath;
    }

    //  newt two lines: getter used by ControlPanel (Mojtaba added)
    public boolean isConnected() {
        return connected;
    }

    //Simulation Start (Delay input still Here!)
    public void startSimulation() {

            // preload native libs (Mojtaba added)
            try {
                Simulation.preloadLibraries();
            } catch (Exception e) {
                System.err.println("Failed to preload libtraci native libraries: " + e.getMessage());
                throw new RuntimeException(e);


        }
        String[] command = {"sumo", "-c", configPath, "--start", "--delay", "200"};
        StringVector commandVector = new StringVector(command);

        System.out.println("Starting SUMO");
        Simulation.start(commandVector);

        // set connected after start (Mojtaba added)
        connected = true;
        System.out.println("TraCI connected");

    }

    //End SUMO process and close TraCI connection
    public void closeSimulation() {
        try {

            // stop tick calls before close (Mojtaba added)
            connected = false;

            Simulation.close();
            System.out.println("SUMO process closed");
        } catch (Exception e) {
            System.err.println("Error while closing: " + e.getMessage());
        }
    }

    //Step
    public void step(){
        Simulation.step();
    }

    //Factory of Instances
    //calls IDs, then creates list of Instances of our Vehicle class
    public List<Vehicle> getVehicles(){
        StringVector ids = org.eclipse.sumo.libtraci.Vehicle.getIDList();
        List<Vehicle> vehicles = new ArrayList<>();

        for(String id : ids){
            vehicles.add(new Vehicle(id));
        }
        return vehicles;
    }

    //the Same for TrafficLights
    public List<TrafficLight> getAllTrafficLights() {

        StringVector ids = org.eclipse.sumo.libtraci.TrafficLight.getIDList();
        List<TrafficLight> trafficlights = new ArrayList<>();


        for (String id : ids) {
            trafficlights.add(new TrafficLight(id));
        }
        return trafficlights;
    }

}