package trafficsimulation.export;

import trafficsimulation.Vehicle;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class VehiclePositionCsvExporter {

    public void export(long step, List<Vehicle> vehicles, String path) throws IOException {
        try (FileWriter writer = new FileWriter(path, true)) { // append=true
            if (step == 0) {
                writer.write("step,vehicleId,edgeId,speed\n");
            }

            for (Vehicle v : vehicles) {
                String edgeId = v.getEdgeId();
                writer.write(step + "," + v.getId() + "," + edgeId + "," + v.getSpeed() + "\n");
            }
        }
    }
}
