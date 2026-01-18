package trafficsimulation.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * AppLogger is a utility class for configuring and providing
 * application-wide loggers.
 *
 * It ensures:
 * - consistent logging format
 * - single initialization
 * - prevention of duplicate console handlers
 */

public class AppLogger {

    private static boolean initialized = false; //Prevents multiple logger initializations

    private AppLogger() {
        // Prevent instantiation (utility class)
    }

    /**
     * Returns a logger instance for the given class.
     *
     * @param clazz the class requesting the logger
     * @return configured logger instance
     */


    public static synchronized Logger getLogger(Class<?> clazz) {
        if (!initialized) { // Initialize logging only once
            initRootLogger();
            initialized = true;
        }
        return Logger.getLogger(clazz.getName()); // Logger named after class
    }

    /**
     * Initializes the root logger configuration.
     *
     * This method sets up a single console handler,
     * defines the log format, and prevents duplicate log output.
     */

    private static void initRootLogger() { // Configure global logger
        Logger rootLogger = Logger.getLogger(""); // Root logger

        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler); // Remove default handlers
        }

        ConsoleHandler consoleHandler = new ConsoleHandler(); // Console handler
        consoleHandler.setFormatter(new SimpleFormatter());
        consoleHandler.setLevel(Level.INFO); // Allow all log levels

        rootLogger.addHandler(consoleHandler); // Add handler
        rootLogger.setLevel(Level.INFO); // Enable all logs
    }
}
