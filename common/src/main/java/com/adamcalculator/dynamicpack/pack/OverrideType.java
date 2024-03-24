package com.adamcalculator.dynamicpack.pack;

public enum OverrideType {
    TRUE,
    FALSE,
    NOT_SET;

    public static OverrideType ofBoolean(boolean b) {
        return b ? TRUE : FALSE;
    }

    public OverrideType next() {
        if (this == TRUE) {
            return FALSE;
        }
        if (this == FALSE) {
            return NOT_SET;
        }
        if (this == NOT_SET) {
            return TRUE;
        }
        throw new UnsupportedOperationException("Hmm");
    }

    public boolean asBoolean() {
        return this == TRUE;
    }
}
