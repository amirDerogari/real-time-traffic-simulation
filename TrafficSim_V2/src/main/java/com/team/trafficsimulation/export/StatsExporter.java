package com.team.trafficsimulation.export;

import java.io.IOException;
import java.util.List;

import com.team.trafficsimulation.stats.SimulationStatsSnapshot;

public interface StatsExporter { // Exporter interface

    void export(List<SimulationStatsSnapshot> snapshots,
                String targetPath) throws IOException; // Export contract
}
