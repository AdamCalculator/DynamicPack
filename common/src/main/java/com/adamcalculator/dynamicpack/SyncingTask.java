package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.util.Out;

/**
 * Sync task.
 * Re-check all packs and update packs with update available
 */
public class SyncingTask implements Runnable {
    private final boolean manually; // skip checkUpdateAvailable().
    private boolean reloadRequired = false;

    public SyncingTask(boolean manually) {
        this.manually = manually;
    }

    @Override
    public void run() {
        DynamicPackModBase.INSTANCE.rescanPacks();
        for (Pack pack : DynamicPackModBase.packs) {
            try {
                pack.sync(createSyncProgressForPack(pack), manually);
            } catch (Exception e) {
                onError(pack, e);
                Out.error("error while process pack: " + pack.getLocation().getName(), e);
            }
        }

        syncDone(reloadRequired);
    }

    public void syncDone(boolean reloadRequired) {
        // to override
    }

    public void onError(Pack pack, Exception e) {
        // to override
    }

    public boolean isReloadRequired() {
        return reloadRequired;
    }

    public SyncProgress createSyncProgressForPack(Pack pack) {
        return new SyncProgress() {
            @Override
            public void textLog(String s) {
                Out.println(pack.getLocation().getName() + ": " + s);
            }

            @Override
            public void done(boolean reloadRequired) {
                if (reloadRequired && !SyncingTask.this.reloadRequired) {
                    try {
                        if (DynamicPackModBase.INSTANCE.isResourcePackActive(pack)) {
                            SyncingTask.this.reloadRequired = true;
                        }
                    } catch (Exception ignored) {
                        SyncingTask.this.reloadRequired = true;
                    }
                }
                Out.println(pack.getLocation().getName() + ": DONE");
            }

            @Override
            public void downloading(String name, float percentage) {
                if (Math.round(percentage) % 20 == 0) {
                    Out.println(pack.getLocation().getName() + ": " + name + ": " + percentage + "%");
                }
            }

            @Override
            public void start() {
                Out.println(pack.getLocation().getName() + ": STARTED");
            }
        };
    }
}
