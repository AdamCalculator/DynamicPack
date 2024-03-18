package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.sync.SyncingTask;
import com.adamcalculator.dynamicpack.sync.state.StateDownloading;
import com.adamcalculator.dynamicpack.sync.state.StateFileDeleted;
import com.adamcalculator.dynamicpack.sync.state.SyncProgressState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;

public class FabricDynamicMod extends DynamicPackModBase implements ClientModInitializer {
    private SystemToast toast = null;
    private long toastUpdated = 0;

    public void setToastContent(Text title, Text text) {
        if (!isMinecraftInitialized()) {
            return;
        }

        if (toast == null || (System.currentTimeMillis() - toastUpdated > 1000*5)) {
            ToastManager toastManager = MinecraftClient.getInstance().getToastManager();
            toastManager.add(toast = new SystemToast(SystemToast.Type.NARRATOR_TOGGLE, title, text));
        } else {
            toast.setContent(title, text);
        }
        toastUpdated = System.currentTimeMillis();
    }


    @Override
    public void onInitializeClient() {
        var gameDir = FabricLoader.getInstance().getGameDir().toFile();
        init(gameDir);
    }

    @Override
    public void startSyncThread() {
        SyncingTask syncingTask = new SyncingTask(false) {
            @Override
            public void onSyncStart() {
                setToastContent(Text.literal("DynamicPack"), Text.translatable("dynamicpack.toast.syncStarted"));
            }

            @Override
            public void onSyncDone(boolean reloadRequired) {
                if (reloadRequired) {
                    setToastContent(Text.literal("DynamicPack"), Text.translatable("dynamicpack.toast.done"));
                    tryToReloadResources();
                }
            }

            @Override
            public void onStateChanged(Pack pack, SyncProgressState state) {
                if (state instanceof StateDownloading downloading) {
                    setToastContent(Text.translatable("dynamicpack.toast.pack.state.downloading.title", pack.getName()), Text.translatable("dynamicpack.toast.pack.state.downloading.description", downloading.getPercentage(), downloading.getName()));

                } else if (state instanceof StateFileDeleted deleted) {
                    setToastContent(Text.translatable("dynamicpack.toast.pack.state.deleting.title", pack.getName()), Text.translatable("dynamicpack.toast.pack.state.deleting.description", deleted.getPath().getFileName().toString()));

                } else {
                    setToastContent(Text.translatable("dynamicpack.toast.pack.state.unknown.title"), Text.translatable("dynamicpack.toast.pack.state.unknown.description"));
                }
            }
        };
        Thread thread = new Thread(syncingTask);
        thread.setName("DynamicPack-SyncTask" + (SyncingTask.threadCounter++));
        thread.start();
    }

    private void tryToReloadResources() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            if (client.world == null) {
                client.send(client::reloadResources);

            } else {
                setToastContent(Text.translatable("dynamicpack.toast.needReload"),
                        Text.translatable("dynamicpack.toast.needReload.description"));
            }
        }
    }
}
