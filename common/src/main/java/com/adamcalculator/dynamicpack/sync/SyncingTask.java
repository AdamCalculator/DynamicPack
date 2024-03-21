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
    public static boolean isSyncing = false;

    private final boolean manually; // skip checkUpdateAvailable().
    private boolean reloadRequired = false;
    private Pack currentPack;

    public SyncingTask(boolean manually) {
        this.manually = manually;
    }

    @Override
    public void run() {
        if (isSyncing) {
            Out.warn("SyncTask already syncing....");
            return;
        }
        isSyncing = true;
        Out.println("SyncTask started!");
        onSyncStart();
        DynamicPackModBase.INSTANCE.rescanPacks();
        DynamicPackModBase.INSTANCE.rescanPacksBlocked = true;
        for (Pack pack : DynamicPackModBase.INSTANCE.getPacks()) {
            currentPack = pack;
            try {
                pack.sync(createSyncProgressForPack(pack), manually);
                onPackDoneSuccess(pack);
            } catch (Exception e) {
                Out.error("Pack error: " + pack.getName(), e);
                onPackError(pack, e);
            }
        }
        DynamicPackModBase.INSTANCE.rescanPacksBlocked = false;
        onSyncDone(reloadRequired);
        Out.println("SyncTask ended!");
        isSyncing = false;
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

            private void _packLog(String s) {
                Out.println(pack.getName() + ": " + s);
            }

            @Override
            public void start() {
                _packLog("Sync started.");
            }

            @Override
            public void done(boolean reloadRequired) {
                _packLog("Sync done. pack reloadRequired=" + reloadRequired);

                if (reloadRequired && !SyncingTask.this.reloadRequired) {
                    try {
                        if (DynamicPackModBase.INSTANCE.isResourcePackActive(pack)) {
                            SyncingTask.this.reloadRequired = true;
                            _packLog("SyncTask.reloadRequired now true!");
                        }
                    } catch (Exception e) {
                        _packLog("SyncTask.reloadRequired now true, but check thrown exception: " + e);
                        SyncingTask.this.reloadRequired = true;
                    }
                }
            }

            @Override
            public void textLog(String s) {
                _packLog("[textLog] " + s);
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
