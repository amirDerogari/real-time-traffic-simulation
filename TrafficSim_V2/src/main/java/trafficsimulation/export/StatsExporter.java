package trafficsimulation.export;

import java.io.IOException;
import java.util.List;
import trafficsimulation.stats.SimulationStatsSnapshot;

/**
 * StatsExporter defines a common interface
 * for exporting simulation statistics.
 *
 * Implementations can export data to different formats
 * such as CSV or PDF.
 */

public interface StatsExporter {

    /**
     * Exports simulation statistics to a file.
     *
     * @param snapshots list of statistic snapshots
     * @param targetPath output file path
     * @throws IOException if export fails
     */

    void export(List<SimulationStatsSnapshot> snapshots,
                String targetPath) throws IOException;
}
