package tools;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ConsoleLogger {
    private static final Logger LOGGER = Logger.getLogger(ConsoleLogger.class.getName());
    private static final String LOG_FORMAT = "%1$s %n";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    static {
        LOGGER.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(LOG_FORMAT, lr.getMessage()
                );
            }
        });
        LOGGER.addHandler(handler);
    }

    /**
     * Convert a duration from milliseconds to the "d days, h hours, m minutes" format.
     *
     * @param durMs the duration in milliseconds.
     * @return the duration in the "d days, h hours, m minutes" format.
     */
    public static String durationToHumanReadableString(Long durMs) {
        Duration dur = Duration.ofMillis(durMs);
        long days = dur.toDays();
        int hours = dur.toHoursPart();
        int minutes = dur.toMinutesPart();
        String readableDuration = "";

        readableDuration += days + " days, ";
        readableDuration += hours + " hrs, ";
        readableDuration += minutes + " min";

        return readableDuration;
    }

    public static void logDate() {
        logTrace("Started: " + DATE_FORMAT.format(System.currentTimeMillis()) + "\n");
    }

    public static void logTrace(String msg) {
        LOGGER.info(msg);
    }

    public static void logError(String msg) {
        LOGGER.warning(msg);
    }

    /**
     * Custom StackTrace Logger
     *
     * @param ex Exception to print the stackTrace of.
     */
    public static void logStackTrace(Exception ex) {
        logError("Exception found: " + ex.toString());
        StackTraceElement[] stackTrace = ex.getStackTrace();
        for (StackTraceElement elem : stackTrace) {
            logError("\t at " + elem.toString());
        }
    }
}
