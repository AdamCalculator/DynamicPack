package com.adamcalculator.dynamicpack.sync;

import com.adamcalculator.dynamicpack.status.StatusChecker;
import com.adamcalculator.dynamicpack.util.Out;

import java.util.function.Supplier;

public class SyncThread extends Thread {
    private static int counter = 0;
    private static final long SLEEP_DELAY = 1000 * 60 * 60 * 24; // 24 hours

    private final Supplier<SyncingTask> taskSupplier;

    public SyncThread(Supplier<SyncingTask> taskSupplier) {
        super("SyncThread" + (counter++));
        if (counter > 1) {
            Out.warn("Multiple SyncThread's is bad behavior...");
        }
        this.taskSupplier = taskSupplier;
    }

    @Override
    public void run() {
        while (true) {
            startSync();
            try {
                Thread.sleep(SLEEP_DELAY);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void startSync() {
        try {
            StatusChecker.check();
        } catch (Exception e) {
            Out.error("Error while check status!", e);
        }
        taskSupplier.get().run();
    }
}
