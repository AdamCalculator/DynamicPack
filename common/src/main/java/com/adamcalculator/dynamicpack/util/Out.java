package com.adamcalculator.dynamicpack.util;

import com.adamcalculator.dynamicpack.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Out {
    public static final Logger LOGGER = LoggerFactory.getLogger("dynamicpack");
    private static final String DEFAULT_PREFIX = "[DynamicPack] ";

    public static boolean ENABLE = true;
    public static boolean USE_SOUT = false;
    private static String PREFIX = "";

    public static void println(Object o) {
        if (!ENABLE) return;
        if (USE_SOUT) {
            System.out.println(PREFIX + o);
            return;
        }
        LOGGER.info(PREFIX + o);
    }

    public static void error(String s, Exception e) {
        if (!ENABLE) return;
        if (USE_SOUT) {
            System.err.println(PREFIX + s);
            e.printStackTrace();
            return;
        }
        LOGGER.error(PREFIX + s, e);
    }

    public static void warn(String s) {
        if (!ENABLE) return;
        if (USE_SOUT) {
            System.out.println(PREFIX + "WARN: " + s);
            return;
        }
        LOGGER.warn(PREFIX + s);
    }

    /**
     * Always enable! Ignore enable/disable
     */
    public static void securityWarning(String s) {
        if (USE_SOUT) {
            System.out.println("[DynamicPack] " + s);
            return;
        }

        try {
            LOGGER.warn("[DynamicPack] " + s);
        } catch (Exception ignored) {
            System.out.println("[DynamicPack] " + s);
        }
    }

    public static void debug(String s) {
        if (Mod.isDebugLogs()) {
            println("DEBUG: " + s);
        }
    }

    /**
     * Always enable! Ignore enable/disable
     */
    public static void securityStackTrace() {
        if (USE_SOUT) {
            System.out.println("[DynamicPack] Stacktrace");
            new Throwable("StackTrace printer").printStackTrace();
            return;
        }
        LOGGER.error("[DynamicPack] No error. This is stacktrace printer", new Throwable("StackTrace printer"));
    }

    public static void init(Loader loader) {
        if (loader == Loader.FABRIC && Mod.isRelease()) {
            PREFIX = DEFAULT_PREFIX;
        }
    }
}
