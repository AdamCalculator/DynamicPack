package com.adamcalculator.dynamicpack.sync;

import com.adamcalculator.dynamicpack.sync.state.SyncProgressState;

/**
 * Sync pack info interface
 */
public interface PackSyncProgress {
    void start();
    void downloading(String name, float percentage);
    void stateChanged(SyncProgressState state);
    void done(boolean reloadRequired);

    void textLog(String s);
}
