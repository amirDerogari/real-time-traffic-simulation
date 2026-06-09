# Real-Time Traffic Simulation

A Java desktop application for simulating and visualizing urban traffic in real time. The project connects a **JavaFX** user interface with **SUMO (Simulation of Urban Mobility)** through **libtraci**, allowing users to observe vehicles, control traffic lights, spawn emergency vehicles, simulate rush-hour behavior, and export simulation statistics.

This project was developed as a university team project with a focus on object-oriented programming, GUI development, simulation control, and data export.

---

## Key Features

- **Real-time SUMO integration** using libtraci
- **Interactive JavaFX map** for visualizing roads, vehicles, and traffic lights
- **Start/stop simulation control** from the GUI
- **Vehicle filtering** for cars, buses, and emergency vehicles
- **Live vehicle counters** by vehicle type
- **Traffic light selection and manual phase switching**
- **Emergency vehicle spawning** with special priority behavior
- **Rush-hour mode** to simulate heavier traffic flow
- **Automatic statistics export** to CSV and PDF
- **Vehicle position logging** for later analysis

---

## Tech Stack

| Area | Technology |
|---|---|
| Programming Language | Java 17 |
| GUI | JavaFX |
| Build Tool | Maven |
| Traffic Simulation | SUMO |
| SUMO Communication | libtraci 1.25.0 |
| Report Export | OpenPDF |
| Data Export | CSV |

---

## Project Structure

```text
real-time-traffic-simulation/
├── TrafficSim_V2/
│   ├── libs/
│   │   └── libtraci-1.25.0.jar
│   ├── src/main/java/trafficsimulation/
│   │   ├── MainApp.java
│   │   ├── SimulationManager.java
│   │   ├── Vehicle.java
│   │   ├── TrafficLight.java
│   │   ├── app/
│   │   │   └── SimulationController.java
│   │   ├── export/
│   │   │   ├── CsvStatsExporter.java
│   │   │   ├── PdfStatsExporter.java
│   │   │   └── VehiclePositionCsvExporter.java
│   │   ├── gui/
│   │   │   ├── TrafficSimulationAppFX.java
│   │   │   ├── MapView.java
│   │   │   ├── ControlPanel.java
│   │   │   └── net/
│   │   │       ├── NetworkLoader.java
│   │   │       └── NetworkModel.java
│   │   ├── stats/
│   │   │   ├── StatsManager.java
│   │   │   ├── SimulationStatsSnapshot.java
│   │   │   └── VehiclePositionSnapshot.java
│   │   └── util/
│   │       └── AppLogger.java
│   ├── src/main/resources/
│   │   ├── final_map.net.xml
│   │   ├── final_map.rou.xml
│   │   └── final_map.sumocfg
│   └── pom.xml
├── User Guide.pdf
├── Project Milestone Report.pdf
└── Summary of Enhancements and Design Decisions.pdf
```

---

## Requirements

Before running the project, install:

- **Java JDK 17** or newer
- **Maven**
- **SUMO**
- **Git**

SUMO must be available from the command line because the application starts SUMO with the `sumo` command.

Check your SUMO installation with:

```bash
sumo --version
```

If the command is not recognized, add SUMO to your system `PATH`.

---

## Installation

Clone the repository:

```bash
git clone https://github.com/YOUR-USERNAME/real-time-traffic-simulation.git
```

Go to the Maven project folder:

```bash
cd real-time-traffic-simulation/TrafficSim_V2
```

Build the project:

```bash
mvn clean install
```

---

## Run the Application

From inside the `TrafficSim_V2` folder, run:

```bash
mvn javafx:run
```

Main class:

```text
trafficsimulation.MainApp
```

Default SUMO configuration:

```text
src/main/resources/final_map.sumocfg
```

---

## How It Works

The application starts a SUMO simulation and communicates with it through libtraci. During each simulation step, vehicle and traffic light data are read from SUMO and displayed in the JavaFX interface.

The GUI consists of:

- a map view for roads, vehicles, and traffic lights
- a control panel for simulation actions
- live counters for vehicle types
- tools for traffic light control, rush-hour mode, and emergency vehicle spawning

When the application is closed, collected statistics are exported automatically.

---

## Exported Files

| File | Description |
|---|---|
| `simulation_stats.csv` | Step-based statistics such as vehicle count, average speed, minimum speed, and maximum speed |
| `simulation_stats.pdf` | PDF report containing exported simulation statistics |
| `vehicle_positions.csv` | Vehicle position and speed data recorded during the simulation |

---

## Main Components

- **`SimulationManager`**: starts/stops SUMO, advances simulation steps, reads live vehicle data, handles traffic lights, emergency vehicles, and rush-hour logic
- **`TrafficSimulationAppFX`**: JavaFX application entry point and main GUI setup
- **`MapView`**: renders the SUMO road network, vehicles, traffic lights, zooming, panning, and selection
- **`ControlPanel`**: provides buttons, filters, vehicle counters, and traffic light controls
- **`SimulationController`**: connects simulation data with statistics collection and export
- **`StatsManager`**: calculates average speed, min/max speed, vehicle count, and density per edge
- **Export classes**: generate CSV and PDF output files

---

## What I Learned

Through this project, I practiced:

- building a JavaFX desktop application
- working with Maven and external libraries
- connecting Java with SUMO through libtraci
- designing object-oriented simulation components
- handling real-time GUI updates
- exporting data to CSV and PDF
- organizing a larger Java project into packages and responsibilities

---

## Future Improvements

- Add screenshots or a demo GIF to the README
- Add real-time charts for simulation statistics
- Allow users to load different SUMO maps from the GUI
- Add custom vehicle creation from the interface
- Improve traffic light configuration options
- Add automated tests for core simulation and statistics logic

---

## Authors

This project was developed as a team project by:

- **Amirreza Derogari**
- **Caspar**
- **Mojtaba**

---

## Documentation

Additional project documentation is included in the repository:

- `User Guide.pdf`
- `Project Milestone Report.pdf`
- `Summary of Enhancements and Design Decisions.pdf`

---

## License

No license file is currently included. If this project should be open source, a license such as MIT, Apache 2.0, or GPL should be added.
