package trafficsimulation;

import org.eclipse.sumo.libtraci.Simulation;
import org.eclipse.sumo.libtraci.StringVector; //dynamic and can reserve memory
import trafficsimulation.app.SimulationController;
import trafficsimulation.EmergencyDistanceException;
import trafficsimulation.stats.SimulationStatsSnapshot;


import java.util.ArrayList;
import java.util.List;

//Factory for Instances of Vehicles and TrafficLights
//Start and stop SUMO Simulation

public class SimulationManager {

    private final List<SimulationStatsSnapshot> statsHistory = new ArrayList<>();

    //Sumo and Net-Config
    //Attributes
    private int emergencyCount = 0;
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

    /**
     * Get TrafficLights from SUMO and store in attribute
     * Used for calling methods of traffic light instances
     * Called once when Simulation starts
     */
    public void initializeTrafficLights() {
        trafficLights = getAllTrafficLights();
    }

    /**
     * Starts the SUMO Simulation
     * Loads Libtraci libaries and connects to SUMO with Configuration from configPath
     * Also Initializes a list of traffic lights
     */
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


        initializeTrafficLights();

    }

    /**
     * Ends the Simulation properly
     * Disconnects from SUMO
     */
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


    /**
     * Tells SUMO to make one step
     * Also checks if emergency Cars are on critical Edges to ensure its priority
     */
    public void step() {

        int busCount = 0;
        Simulation.step();

        //  get vehicles and check for emergency
        List<Vehicle> vehicles = getVehicles();
        for (Vehicle v : vehicles) {
            if(v.getTypeId().equals("emergency")){

                if(v.getRoadId().equals("EM_IN")){
                    for(Vehicle v2 : vehicles){
                        if(v2.getRoadId().equals("CIL1")){
                            org.eclipse.sumo.libtraci.Vehicle.slowDown(v2.getId(), 1.0, 5);
                        }
                    }
                }

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

        SimulationStatsSnapshot snapshot =
                simulationController.collectStats(vehicles);

        statsHistory.add(snapshot);

    }

    //Factory of Instances
    //calls IDs, then creates list of Instances of our Vehicle class
    /**
     * Factory of Instances
     * calls IDs, then creates list of Instances of our Vehicle class
     * @return List with Vehicle IDs of living cars
     * Important to know: with this called in step() every time, objects are created new in every step
     */
    public List<Vehicle> getVehicles(){
        StringVector ids = org.eclipse.sumo.libtraci.Vehicle.getIDList();
        List<Vehicle> vehicles = new ArrayList<>();

        for(String id : ids){
            vehicles.add(new Vehicle(id));
        }
        return vehicles;
    }

    /**
     * Factory of Instances
     * calls IDs, then creates list of Instances of our TrafficLight class
     * @return List with TrafficLight IDs
     * Is called to initialize Map objects
     */
    public List<TrafficLight> getAllTrafficLights() {

        StringVector ids = org.eclipse.sumo.libtraci.TrafficLight.getIDList();
        List<TrafficLight> trafficlights = new ArrayList<>();

        for (String id : ids) {
            trafficlights.add(new TrafficLight(id));
        }
        return trafficlights;
    }

    //Changing TL Phase (temporarily added by Mojtaba to check if it works)
    public void nextPhase(String tlId) {
        int current = org.eclipse.sumo.libtraci.TrafficLight.getPhase(tlId);
        int next = (current + 1) % 3; // or use real phase count
        org.eclipse.sumo.libtraci.TrafficLight.setPhase(tlId, next);
    }


    //spawn emergency Vehicle
    /**
     * Spawn an emergency Vehicle
     * First its route is set
     * Then priority is manipulated over other cars
     * Created for manual vehicle injection with GUI button
     */
    public void spawnEmergencyVehicle(){

        //Error handling (Distance)
        try {
            List<Vehicle> vehicles = getVehicles();
            for(Vehicle v : vehicles){
                if (v.getEdgeId().equals("EM_IN") && v.getTypeId().equals("emergency")){
                    throw new EmergencyDistanceException("Error: Startpoint EM_IN is blocked");
                }
            }

        //only create route for the first spawn
        if(emergencyCount==0){
            StringVector routeEdges = new StringVector();
            //define route
            routeEdges.add("EM_IN");
            routeEdges.add("CIL2");
            routeEdges.add("WI1");
            routeEdges.add("ONETOTWO");
            routeEdges.add("EO2");
            org.eclipse.sumo.libtraci.Route.add("emergency_route", routeEdges);
        }

        String vehicleId = "em_" + emergencyCount;
        org.eclipse.sumo.libtraci.Vehicle.add("em_"+ vehicleId, "emergency_route", "emergency", "now");

        //emergency car aggressive drive up (ensures not to wait at a driveway)
        org.eclipse.sumo.libtraci.Vehicle.setSpeedMode("em_"+ vehicleId, 31); //does not slow down
        org.eclipse.sumo.libtraci.Vehicle.setLaneChangeMode("em_"+ vehicleId, 1621); //uses any space
        org.eclipse.sumo.libtraci.Vehicle.setImpatience("em_"+ vehicleId, 1.0);

        emergencyCount++;

        //error catch part
        }
        catch (EmergencyDistanceException e) {
            // treat exception
            System.err.println("LOGIK-ERROR: " + e.getMessage());
        }
    }

    /**
     * Activates the RushHour
     * It forces a green phase for the main traffic lane
     * on both TrafficLights for 80 steps
     */
    public void runRushHour(){
        System.out.println("runRushHour");
        trafficLights.get(0).forceGreenPhase(0,80);
        trafficLights.get(1).forceGreenPhase(0,80);
    }
}