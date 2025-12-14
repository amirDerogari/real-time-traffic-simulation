package trafficsimulation.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import trafficsimulation.util.AppLogger;

public class StatsManager {
    private static final Logger LOG = AppLogger.getLogger(StatsManager.class);

    private final List<SimulationStatsSnapshot> history = new ArrayList<>();

    public void addSnapshot(SimulationStatsSnapshot snapshot) {
        if (snapshot == null) {
            LOG.warning("Null-Snapshot wurde an StatsManager übergeben – ignoriert.");
            return;
        }
        history.add(snapshot);
        LOG.fine("Stats-Snapshot hinzugefügt. Total snapshots: " + history.size());
    }

    public List<SimulationStatsSnapshot> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public SimulationStatsSnapshot getLatest() {
        if (history.isEmpty()) {
            return null;
        }
        return history.get(history.size() - 1);
    }

}
