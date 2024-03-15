package com.adamcalculator.dynamicpack;

public interface SyncProgress {
    void textLog(String s);

    void downloading(String file, long writtenBytes, long total);

    void done();
}
