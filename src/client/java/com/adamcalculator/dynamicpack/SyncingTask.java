package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;

public class SyncingTask implements Runnable {
    private boolean manually;
    private boolean reloadRequired = false;

    public SyncingTask(boolean manually) {
        this.manually = manually;
    }

    @Override
    public void run() {
        DynamicPackMod.rescanPacks();
        for (Pack pack : DynamicPackMod.packs) {
            try {
                SyncProgress syncProgress = new SyncProgress() {
                    @Override
                    public void textLog(String s) {
                        Out.println("Pack_"+pack.getLocation().getName() + ": " + s);
                    }

                    @Override
                    public void done(boolean b) {
                        if (b) {
                            reloadRequired = true;
                        }
                        Out.println("Pack_"+pack.getLocation().getName() + " is done!");
                    }
                };
                pack.sync(syncProgress, manually);
            } catch (Exception e) {
                Out.error("error while process pack: " + pack.getLocation().getName(), e);
            }
        }
    }

    public boolean isReloadRequired() {
        return reloadRequired;
    }
}
