package trafficsimulation.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import trafficsimulation.stats.SimulationStatsSnapshot;
import trafficsimulation.util.AppLogger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * PdfStatsExporter exports simulation statistics
 * into a formatted PDF report.
 *
 * The report contains a table with one row per simulation step.
 */

public class PdfStatsExporter implements StatsExporter {

    private static final Logger LOG = AppLogger.getLogger(PdfStatsExporter.class);

    /**
     * Exports simulation statistics to a PDF file.
     *
     * @param snapshots list of statistic snapshots
     * @param targetPath output file path
     * @throws IOException if PDF creation fails
     */

    @Override
    public void export(List<SimulationStatsSnapshot> snapshots, String targetPath) throws IOException {

        if (snapshots == null || snapshots.isEmpty()) {
            LOG.warning("PDF export: no snapshots available. PDF will still be created with header only.");
        }

        Document document = new Document(PageSize.A4);

        try (FileOutputStream out = new FileOutputStream(targetPath)) {

            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Traffic Simulation Statistics Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total snapshots: " + (snapshots == null ? 0 : snapshots.size())));
            document.add(new Paragraph(" "));

            // Table: 5 columns
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 2.0f, 1.6f, 1.6f, 1.6f});

            addHeaderCell(table, "Step");
            addHeaderCell(table, "Average Speed");
            addHeaderCell(table, "Vehicle Count");
            addHeaderCell(table, "Min Speed");
            addHeaderCell(table, "Max Speed");

            if (snapshots != null) {
                for (SimulationStatsSnapshot s : snapshots) {
                    table.addCell(new Phrase(String.valueOf(s.getSimulationStep())));
                    table.addCell(new Phrase(String.valueOf(s.getAverageSpeed())));
                    table.addCell(new Phrase(String.valueOf(s.getVehicleCount())));
                    table.addCell(new Phrase(String.valueOf(s.getMinSpeed())));
                    table.addCell(new Phrase(String.valueOf(s.getMaxSpeed())));
                }
            }

            document.add(table);

            document.close();
            LOG.info("PDF export successful: " + targetPath);

        } catch (DocumentException e) {
            // DocumentException ist kein IOException -> wir wrappen es als IOException
            throw new IOException("PDF export failed: " + e.getMessage(), e);
        }
    }

    private void addHeaderCell(PdfPTable table, String text) {
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}
