package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import com.google.common.io.Files;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class SyncingTask implements Runnable {
    public static void startSyncThread() {
        SyncingTask syncingTask = new SyncingTask(false) {
            @Override
            public void syncDone(boolean reloadRequired) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && reloadRequired) {
                    if (client.world == null) {
                        client.reloadResources();
                    } else {
                        ToastManager toastManager = client.getToastManager();
                        if (toastManager != null) {
                            toastManager.add(SystemToast.create(client, SystemToast.Type.PACK_LOAD_FAILURE,
                                    Text.translatable("dynamicpack.toast.needReload"), Text.translatable("dynamicpack.toast.needReload.desc")));
                        }
                    }
                }
            }
        };
        Thread thread = new Thread(syncingTask);
        thread.setName("DynamicPack-SyncTask");
        thread.start();
    }

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
                pack.sync(createSyncProgressForPack(pack), manually);
            } catch (Exception e) {
                Out.error("error while process pack: " + pack.getLocation().getName(), e);
            }
        }

        syncDone(reloadRequired);
    }

    public void syncDone(boolean reloadRequired) {

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
                        for (String readLine : Files.readLines(new File(DynamicPackMod.gameDir, "options.txt"), StandardCharsets.UTF_8)) {
                            if (readLine.startsWith("resourcePacks:")) {
                                String name = "file/" + pack.getLocation().getName();
                                if (readLine.contains(name)) {
                                    SyncingTask.this.reloadRequired = true;
                                }
                            }
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
