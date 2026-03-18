package org.example.ignitron;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Log {
    private static final Logger LOG = Logger.getLogger("Ignitron");

    private Log() {} // prevent instantiation

    public static void info(String msg) {
        LOG.info(msg);
    }

    public static void warn(String msg) {
        LOG.warning(msg);
    }

    public static void fine(String msg) {
        LOG.fine(msg);
    }

    public static void error(String msg, Throwable t) {
        LOG.log(Level.SEVERE, msg, t);
    }

}
