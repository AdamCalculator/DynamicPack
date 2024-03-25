package com.adamcalculator.dynamicpack;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator {
    private static final Pattern CONTENT_ID_PATTERN = Pattern.compile("^[a-z0-9_:-]{2,128}$");
    private static final Pattern PATH_PATTERN = Pattern.compile("^[A-Za-z0-9_./() +-]{0,255}$");

    public static boolean isContentIdValid(String input) {
        if (input == null) {
            return false;
        }
        Matcher matcher = CONTENT_ID_PATTERN.matcher(input);
        return matcher.matches();
    }

    public static boolean isContentNameValid(String input) {
        if (input == null) {
            return false;
        }

        return input.trim().length() < 64 && !input.trim().isEmpty() && !input.contains("\n") && !input.contains("\r") && !input.contains("\b");
    }

    public static boolean isPackNameValid(String input) {
        if (input == null) {
            return false;
        }

        return input.trim().length() < 64 && !input.trim().isEmpty() && !input.contains("\n") && !input.contains("\r") && !input.contains("\b");
    }

    public static boolean isHashValid(String hash) {
        return hash != null && hash.length() == 40 && !hash.contains(" ");
    }

    public static void validOrThrownPath(String par) {
        if (par == null) {
            throw new SecurityException("null", new NullPointerException("path to valid is null"));
        }
        if (!PATH_PATTERN.matcher(par).matches()) {
            throw new SecurityException("Not valid path: " + new String(par.getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII));
        }
    }
}
