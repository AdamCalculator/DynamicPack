package com.adamcalculator.dynamicpack;

public interface SyncProgress {
    void textLog(String s);
    void done(boolean reloadRequired);
    void downloading(String name, float percentage);
    void start();
}
