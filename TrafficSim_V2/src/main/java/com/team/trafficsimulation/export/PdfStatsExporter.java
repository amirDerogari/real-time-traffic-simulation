package com.team.trafficsimulation.export;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.team.trafficsimulation.stats.SimulationStatsSnapshot;
import com.team.trafficsimulation.util.AppLogger;

public class PdfStatsExporter implements StatsExporter { // PDF exporter placeholder

    private static final Logger LOG =
            AppLogger.getLogger(PdfStatsExporter.class); // Class logger

    @Override
    public void export(List<SimulationStatsSnapshot> snapshots,
                       String targetPath) throws IOException {

        LOG.warning(
                "PDF-Export is not implemented yet. Target path: " + targetPath
        ); // Inform about missing implementation
    }
}
