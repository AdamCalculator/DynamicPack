package com.adamcalculator.dynamicpack.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Out {
    public static final Logger LOGGER = LoggerFactory.getLogger("dynamicpack");
    public static boolean USE_SOUT = false;

    public static void println(Object o) {
        if (USE_SOUT) {
            System.out.println(o);
            return;
        }
        LOGGER.warn(o + "");
    }

    public static void e(Exception e) {
        if (USE_SOUT) {
            System.out.println(e);
            e.printStackTrace();
            return;
        }
        LOGGER.error("Out", e);
    }

    public static void error(String s, Exception e) {
        if (USE_SOUT) {
            System.err.println(s);
            e.printStackTrace();
            return;
        }
        LOGGER.error(s, e);
    }

    public static void downloading(String url, File file) {
        if (USE_SOUT) {
            System.out.println(file.getName() + " downloading from " + url);
            return;
        }
        LOGGER.warn("Downloading " + file.getName() + " from " + url);
    }

    public static void warn(String s) {
        if (USE_SOUT) {
            System.out.println("WARN: " + s);
            return;
        }
        LOGGER.warn(s);
    }
}
