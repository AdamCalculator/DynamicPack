package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.sync.SyncThread;
import com.adamcalculator.dynamicpack.sync.SyncingTask;
import com.adamcalculator.dynamicpack.sync.state.StateDownloading;
import com.adamcalculator.dynamicpack.sync.state.StateFileDeleted;
import com.adamcalculator.dynamicpack.sync.state.SyncProgressState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.resource.metadata.PackResourceMetadataReader;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;

public class FabricDynamicMod extends DynamicPackModBase implements ClientModInitializer {
    private static final boolean SHOW_STATE = false;
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
        new SyncThread(() -> createSyncTask(false)).start();
    }

    @Override
    public void startManuallySync() {
        Thread thread = new Thread(() -> createSyncTask(true).run());
        thread.setName("DynamicPack-ManuallySyncThread" + (DynamicPackModBase.manuallySyncThreadCounter++));
        thread.start();
    }

    private SyncingTask createSyncTask(boolean manually) {
        return new SyncingTask(manually) {
            @Override
            public void onSyncStart() {
                if (manually) setToastContent(Text.literal("DynamicPack"), Text.translatable("dynamicpack.toast.syncStarted"));
            }

            @Override
            public void onSyncDone(boolean reloadRequired) {
                if (manually || reloadRequired) {
                    setToastContent(Text.literal("DynamicPack"), Text.translatable("dynamicpack.toast.done"));
                }
                if (reloadRequired) {
                    tryToReloadResources();
                }
            }

            @Override
            public void onStateChanged(Pack pack, SyncProgressState state) {
                if (!manually) return;

                if (state instanceof StateDownloading downloading) {
                    setToastContent(Text.translatable("dynamicpack.toast.pack.state.downloading.title", pack.getName()), Text.translatable("dynamicpack.toast.pack.state.downloading.description", downloading.getPercentage(), downloading.getName()));

                } else if (state instanceof StateFileDeleted deleted) {
                    setToastContent(Text.translatable("dynamicpack.toast.pack.state.deleting.title", pack.getName()), Text.translatable("dynamicpack.toast.pack.state.deleting.description", deleted.getPath().getFileName().toString()));

                } else {
                    setToastContent(Text.translatable("dynamicpack.toast.pack.state.unknown.title"), Text.translatable("dynamicpack.toast.pack.state.unknown.description"));
                }
            }
        };
    }

    @Override
    public String getCurrentGameVersion() {
        return SharedConstants.getGameVersion().getId();
    }

    @Override
    public boolean checkResourcePackMetaValid(String s) {
        new PackResourceMetadataReader().fromJson(JsonHelper.deserialize(s).getAsJsonObject("pack"));
        return true;
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
