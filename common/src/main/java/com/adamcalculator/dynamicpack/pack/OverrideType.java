package com.adamcalculator.dynamicpack.pack;

public enum OverrideType {
    TRUE,
    FALSE,
    NOT_SET;

    public static OverrideType ofBoolean(boolean b) {
        return b ? TRUE : FALSE;
    }

    public OverrideType next() {
        return switch (this) {
            case TRUE -> FALSE;
            case FALSE -> NOT_SET;
            case NOT_SET -> TRUE;
        };
    }

    public boolean asBoolean() {
        return this == TRUE;
    }
}
