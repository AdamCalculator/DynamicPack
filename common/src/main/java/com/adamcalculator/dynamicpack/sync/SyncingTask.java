package com.adamcalculator.dynamicpack.sync;

import com.adamcalculator.dynamicpack.DynamicPackModBase;
import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.sync.state.StateDownloadDone;
import com.adamcalculator.dynamicpack.sync.state.StateDownloading;
import com.adamcalculator.dynamicpack.sync.state.SyncProgressState;
import com.adamcalculator.dynamicpack.util.Out;

/**
 * Sync task.
 * Re-check all packs and update packs with update available
 */
public class SyncingTask implements Runnable {
    public static long threadCounter = 0;

    private final boolean manually; // skip checkUpdateAvailable().
    private boolean reloadRequired = false;
    private Pack currentPack;

    public SyncingTask(boolean manually) {
        this.manually = manually;
    }

    @Override
    public void run() {
        onSyncStart();
        DynamicPackModBase.INSTANCE.rescanPacks();
        for (Pack pack : DynamicPackModBase.packs) {
            currentPack = pack;
            try {
                pack.sync(createSyncProgressForPack(pack), manually);
                onPackDoneSuccess(pack);
            } catch (Exception e) {
                onPackError(pack, e);
            }
        }
        onSyncDone(reloadRequired);
    }

    public void onPackDoneSuccess(Pack pack) {}

    public void onSyncStart() {}

    public void onSyncDone(boolean reloadRequired) {}

    public void onPackError(Pack pack, Exception e) {}

    public void onStateChanged(Pack pack, SyncProgressState state) {}


    private void setState(SyncProgressState state) {
        try {
            onStateChanged(currentPack, state);
        } catch (Exception e) {
            Out.error("onStateChanged exception!!!", e);
        }
    }

    public PackSyncProgress createSyncProgressForPack(Pack pack) {
        return new PackSyncProgress() {
            private StateDownloading cachedDownloading = null;


            @Override
            public void start() {
                Out.println(pack.getLocation().getName() + ": Sync started.");
            }

            @Override
            public void done(boolean reloadRequired) {
                Out.println(pack.getLocation().getName() + ": Sync done. pack reloadRequired=" + reloadRequired);

                if (reloadRequired && !SyncingTask.this.reloadRequired) {
                    try {
                        if (DynamicPackModBase.INSTANCE.isResourcePackActive(pack)) {
                            SyncingTask.this.reloadRequired = true;
                            Out.println(pack.getLocation().getName() + ": SyncTask.reloadRequired now true!");
                        }
                    } catch (Exception e) {
                        Out.error(pack.getLocation().getName() + ": SyncTask.reloadRequired now true!", e);
                        SyncingTask.this.reloadRequired = true;
                    }
                }
            }

            @Override
            public void textLog(String s) {
                Out.println(pack.getLocation().getName() + ": [textLog] " + s);
            }

            @Override
            public void downloading(String name, float percentage) {
                if (cachedDownloading == null) {
                    cachedDownloading = new StateDownloading(name);
                } else {
                    cachedDownloading.setName(name);
                }
                if (percentage == 100f) {
                    setState(new StateDownloadDone());
                    return;
                }

                cachedDownloading.setPercentage(Math.round(percentage * 10f) / 10f);
                setState(cachedDownloading);
            }

            @Override
            public void stateChanged(SyncProgressState state) {
                setState(state);
            }
        };
    }
}
