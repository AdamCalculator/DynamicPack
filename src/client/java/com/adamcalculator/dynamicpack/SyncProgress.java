package com.adamcalculator.dynamicpack;

public interface SyncProgress {
    void textLog(String s);
    void done(boolean reloadRequired);
}
