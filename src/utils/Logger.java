package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple application logger.
 * Replace printStackTrace calls with structured logging.
 */
public class Logger {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void info(String msg) {
        System.out.println(prefix("INFO") + msg);
    }

    public static void warn(String msg) {
        System.out.println(prefix("WARN") + msg);
    }

    public static void error(String msg, Throwable t) {
        System.err.println(prefix("ERROR") + msg);
        if (t != null) {
            // minimal stack trace output; could be enhanced to file logging later
            t.printStackTrace(System.err);
        }
    }

    private static String prefix(String level) {
        return "[" + level + "] " + LocalDateTime.now().format(FMT) + " - ";
    }
}

