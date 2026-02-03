package application.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ✅ NUOVO: Logger Utility per logging professionale
 * Sostituisce System.out/err.println con un sistema di logging strutturato
 *
 * Uso:
 * - LoggerUtil.info("Messaggio informativo");
 * - LoggerUtil.error("Errore", exception);
 * - LoggerUtil.warning("Messaggio di warning");
 *
 * @author SwapUnina Development Team
 * @version 1.0
 */
public class LoggerUtil {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";

    /**
     * Log informativo
     */
    public static void info(String message) {
        log("INFO", BLUE, message, null);
    }

    /**
     * Log di successo
     */
    public static void success(String message) {
        log("SUCCESS", GREEN, message, null);
    }

    /**
     * Log di warning
     */
    public static void warning(String message) {
        log("WARNING", YELLOW, message, null);
    }

    /**
     * Log di errore con eccezione
     */
    public static void error(String message, Throwable throwable) {
        log("ERROR", RED, message, throwable);
    }

    /**
     * Log di errore senza eccezione
     */
    public static void error(String message) {
        error(message, null);
    }

    /**
     * Log di debug
     */
    public static void debug(String message) {
        // Solo in modalità debug
        if (isDebugMode()) {
            log("DEBUG", CYAN, message, null);
        }
    }

    /**
     * Log personalizzato
     */
    public static void log(String level, String color, String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String threadName = Thread.currentThread().getName();
        StackTraceElement caller = getCaller();
        String callerInfo = caller != null ? caller.getClassName() + "." + caller.getMethodName() + ":" + caller.getLineNumber() : "Unknown";

        String logMessage = String.format("%s [%s] %s%-7s%s %s - %s",
                timestamp,
                threadName,
                color,
                level,
                RESET,
                callerInfo,
                message
        );

        synchronized (System.out) {
            if ("ERROR".equals(level)) {
                System.err.println(logMessage);
            } else {
                System.out.println(logMessage);
            }

            if (throwable != null) {
                System.err.println(color + "Stack Trace:" + RESET);
                throwable.printStackTrace(System.err);
            }
        }
    }

    /**
     * Ottiene il chiamante del metodo logger
     */
    private static StackTraceElement getCaller() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // stackTrace[0] = getStackTrace
        // stackTrace[1] = log (metodo corrente)
        // stackTrace[2] = info/error/warning (metodo pubblico)
        // stackTrace[3] = chiamante reale
        if (stackTrace.length > 4) {
            return stackTrace[4];
        }
        return null;
    }

    /**
     * Verifica se siamo in modalità debug
     */
    private static boolean isDebugMode() {
        String debug = System.getProperty("app.debug", "false");
        return "true".equalsIgnoreCase(debug) || "1".equals(debug);
    }

    /**
     * Metodo di utilità per logging di operazioni database
     */
    public static void logDbOperation(String operation, String table, boolean success) {
        if (success) {
            success(String.format("DB: %s su %s completato", operation, table));
        } else {
            error(String.format("DB: %s su %s fallito", operation, table));
        }
    }

    /**
     * Metodo di utilità per logging di operazioni UI
     */
    public static void logUiAction(String component, String action) {
        info(String.format("UI: %s - %s", component, action));
    }

    /**
     * Metodo di utilità per logging di performance
     */
    public static void logPerformance(String operation, long durationMs) {
        String color = durationMs > 1000 ? RED : durationMs > 500 ? YELLOW : GREEN;
        log("PERF", color, String.format("%s completato in %d ms", operation, durationMs), null);
    }
}
