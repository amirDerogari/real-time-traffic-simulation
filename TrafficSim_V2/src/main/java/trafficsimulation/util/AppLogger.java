package trafficsimulation.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AppLogger {
    private static boolean initialized = false;

    private AppLogger() {
        // Utility-Klasse -> kein Konstruktor
    }

    public static Logger getLogger(Class<?> clazz) {
        if (!initialized) {
            initRootLogger();
            initialized = true;
        }
        return Logger.getLogger(clazz.getName());
    }

    private static void initRootLogger() {
        Logger rootLogger = Logger.getLogger("");
        // Standard-Handler entfernen
        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        consoleHandler.setLevel(Level.ALL);

        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.ALL);
    }
}
