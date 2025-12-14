package trafficsimulation.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AppLogger {

    private static boolean initialized = false; // Initialization flag

    private AppLogger() {
        // Prevent instantiation (utility class)
    }

    public static Logger getLogger(Class<?> clazz) { // Returns class-specific logger
        if (!initialized) { // Initialize logging only once
            initRootLogger();
            initialized = true;
        }
        return Logger.getLogger(clazz.getName()); // Logger named after class
    }

    private static void initRootLogger() { // Configure global logger
        Logger rootLogger = Logger.getLogger(""); // Root logger

        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler); // Remove default handlers
        }

        ConsoleHandler consoleHandler = new ConsoleHandler(); // Console handler
        consoleHandler.setFormatter(new SimpleFormatter()); // Simple format
        consoleHandler.setLevel(Level.ALL); // Allow all log levels

        rootLogger.addHandler(consoleHandler); // Add handler
        rootLogger.setLevel(Level.ALL); // Enable all logs
    }
}
