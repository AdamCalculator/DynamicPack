package com.adamcalculator.dynamicpack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Out {
    public static final Logger LOGGER = LoggerFactory.getLogger("dynamicpack");

    public static void println(Object o) {
        LOGGER.warn(o + "");
    }

    public static void e(Exception e) {
        LOGGER.error("Out", e);
    }

    public static void error(String s, Exception e) {
        LOGGER.error(s, e);
    }

    public static void downloading(String url, File file) {
        LOGGER.warn("Downloading " + file.getName() + " from " + url);
    }
}
