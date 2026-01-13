package trafficsimulation;

import org.eclipse.sumo.libtraci.Simulation;
import org.eclipse.sumo.libtraci.StringVector; //dynamic and can reserve memory
import trafficsimulation.app.SimulationController;


import java.util.ArrayList;
import java.util.List;

//Factory for Instances of Vehicles and TrafficLights
//Start and stop SUMO Simulation

public class SimulationManager {

    //Attributes
    private List<TrafficLight> trafficLights = new ArrayList<>();
    private final SimulationController simulationController = new SimulationController();
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

    //Get TrafficLights from SUMO (called once when starting simulation)
    public void initializeTrafficLights() {
        trafficLights = getAllTrafficLights();
    }

    //Simulation start
    public void startSimulation() {

        // preload native libs (Mojtaba added)
        try {
            Simulation.preloadLibraries();
        } catch (Exception e) {
            System.err.println("Failed to preload libtraci native libraries: " + e.getMessage());
            throw new RuntimeException(e);
        }

        String[] command = {"sumo-gui", "-c", configPath, "--start", "--delay", "200"};
        StringVector commandVector = new StringVector(command);

        System.out.println("Starting SUMO");
        Simulation.start(commandVector);

        // set connected after start (Mojtaba added)
        connected = true;
        System.out.println("TraCI connected");

        initializeTrafficLights();

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

    public void step() {

        int busCount = 0;
        Simulation.step();

        //  get vehicles and check for emergency
        List<Vehicle> vehicles = getVehicles();
        for (Vehicle v : vehicles) {
            if(v.getTypeId().equals("emergency")){
                if(v.getRoadId().equals("WI1")){
                    trafficLights.get(0).setPhaseIndex(0);
                }
                if(v.getRoadId().equals("ONETOTWO")){
                    trafficLights.get(1).setPhaseIndex(0);
                }
            }
            //bus counter
            if(v.getTypeId().equals("bus_standard")){
                busCount++;

                //GIVE BUSCOUNT TO SIM-CONTROLLER ?
                //exp. simulationController.updateBusStats(busCount);
            }
        }

        simulationController.collectStats(vehicles);
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

    //spawn emergency Vehicle
    public void spawnEmergencyVehicle(){
        StringVector routeEdges = new StringVector();
        //define route
        routeEdges.add("EM_IN");
        routeEdges.add("CIL2");
        routeEdges.add("WI1");
        routeEdges.add("ONETOTWO");
        routeEdges.add("EO2");
        org.eclipse.sumo.libtraci.Route.add("emergency_route", routeEdges);

        //fix ID: only one Emergency vehicle should be alive!
        if (!org.eclipse.sumo.libtraci.Vehicle.getIDList().contains("em_1")){
            org.eclipse.sumo.libtraci.Vehicle.add("em_1", "emergency_route", "emergency", "now");
        } else {
          System.out.println("emergency car already exists");
        }
    }

    //rushHour: let main traffic through CR1,CR2
    public void runRushHour(){
        System.out.println("runRushHour");
        trafficLights.get(0).forceGreenPhase(0,80);
        trafficLights.get(1).forceGreenPhase(0,80);
    }
}