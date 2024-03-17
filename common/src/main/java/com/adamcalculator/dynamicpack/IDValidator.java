package com.adamcalculator.dynamicpack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IDValidator {
    private static final Pattern pattern = Pattern.compile("^[a-z0-9A-Z]{0,100}$");

    public static boolean isValid(String input) {
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }
}
