package com.adamcalculator.dynamicpack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IDValidator {
    private static final Pattern PATTERN_ID = Pattern.compile("^[a-z0-9]{1,100}$");

    public static boolean isValid(String input) {
        Matcher matcher = PATTERN_ID.matcher(input);
        return matcher.matches();
    }
}
