package trafficsimulation.export;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import trafficsimulation.stats.SimulationStatsSnapshot;
import trafficsimulation.util.AppLogger;

public class PdfStatsExporter implements StatsExporter {
    private static final Logger LOG = AppLogger.getLogger(PdfStatsExporter.class);

    @Override
    public void export(List<SimulationStatsSnapshot> snapshots, String targetPath) throws IOException {
        LOG.warning("PDF-Export ist noch nicht implementiert. Zielpfad: " + targetPath);
        // TODO: später mit PDF-Bibliothek implementieren (z.B. iText, OpenPDF, o.ä.)
    }

}
