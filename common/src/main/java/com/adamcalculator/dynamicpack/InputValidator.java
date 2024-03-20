package com.adamcalculator.dynamicpack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator {
    private static final Pattern CONTENT_ID_PATTERN = Pattern.compile("^[a-z0-9_:]{2,128}$");

    public static boolean isContentIdValid(String input) {
        if (input == null) {
            return false;
        }
        Matcher matcher = CONTENT_ID_PATTERN.matcher(input);
        return matcher.matches();
    }

    public static boolean isPackNameValid(String input) {
        if (input == null) {
            return false;
        }

        return input.trim().length() < 64 && !input.trim().isEmpty() && !input.contains("\n") && !input.contains("\r") && !input.contains("\b");
    }
}
