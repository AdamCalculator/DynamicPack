package com.adamcalculator.dynamicpack;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;

public class FabricDynamicMod extends DynamicPackModBase implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        var gameDir = FabricLoader.getInstance().getGameDir().toFile();
        init(gameDir);
    }

    @Override
    public void startSyncThread() {
        SyncingTask syncingTask = new SyncingTask(false) {
            @Override
            public void syncDone(boolean reloadRequired) {
                if (reloadRequired) {
                    tryToReloadResources();
                }
            }
        };
        Thread thread = new Thread(syncingTask);
        thread.setName("DynamicPack-SyncTask");
        thread.start();
    }

    private void tryToReloadResources() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
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
}
