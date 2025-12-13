package com.team.trafficsimulation.export;

import java.io.IOException;
import java.util.List;

import com.team.trafficsimulation.stats.SimulationStatsSnapshot;

public interface StatsExporter {

    void export(List<SimulationStatsSnapshot> snapshots,
                String targetPath) throws IOException;

}
