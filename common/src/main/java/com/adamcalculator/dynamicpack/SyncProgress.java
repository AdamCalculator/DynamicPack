package com.adamcalculator.dynamicpack;

/**
 * Sync pack info interface
 */
public interface SyncProgress {
    void textLog(String s);
    void done(boolean reloadRequired);
    void downloading(String name, float percentage);
    void start();
}
