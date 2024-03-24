package com.adamcalculator.dynamicpack.client;

import com.adamcalculator.dynamicpack.DynamicPackMod;
import com.adamcalculator.dynamicpack.Mod;
import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.status.StatusChecker;
import com.adamcalculator.dynamicpack.sync.SyncThread;
import com.adamcalculator.dynamicpack.sync.SyncingTask;
import com.adamcalculator.dynamicpack.sync.state.StateDownloading;
import com.adamcalculator.dynamicpack.sync.state.StateFileDeleted;
import com.adamcalculator.dynamicpack.sync.state.SyncProgressState;
import com.adamcalculator.dynamicpack.util.Out;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LazilyParsedNumber;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.*;
import net.minecraft.util.GsonHelper;

import java.util.UUID;

public abstract class DynamicPackModBase extends DynamicPackMod {
    private SystemToast toast = null;
    private long toastUpdated = 0;

    public void setToastContent(Component title, Component text) {
        if (!isMinecraftInitialized()) {
            return;
        }

        if (toast == null || (System.currentTimeMillis() - toastUpdated > 1000*5)) {
            ToastComponent toastManager = Minecraft.getInstance().getToasts();
            toastManager.addToast(toast = new SystemToast(SystemToast.SystemToastIds.NARRATOR_TOGGLE, title, text));
        } else {
            toast.reset(title, text);
        }
        toastUpdated = System.currentTimeMillis();
    }

    public void onWorldJoinForUpdateChecks(LocalPlayer player) {
        if (Mod.isDebugMessageOnWorldJoin()) {
            player.sendMessage(new TextComponent("Debug message on world join").withStyle(ChatFormatting.GREEN), UUID.randomUUID());
        }
        Component download = new TranslatableComponent("dynamicpack.status_checker.download")
                .withStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("dynamicpack.status_checker.download.hover", new TextComponent(com.adamcalculator.dynamicpack.Mod.MODRINTH_URL).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.AQUA))))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, com.adamcalculator.dynamicpack.Mod.MODRINTH_URL))
                )
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE);


        if (player == null) {
            Out.warn("player == null on world join");

        } else if (!StatusChecker.isSafe()) {
            player.sendMessage(new TranslatableComponent("dynamicpack.status_checker.not_safe", download), UUID.randomUUID());
            setToastContent(new TranslatableComponent("dynamicpack.status_checker.not_safe.toast.title"),
                    new TranslatableComponent("dynamicpack.status_checker.not_safe.toast.description"));

        } else if (!StatusChecker.isFormatActual()) {
            player.sendMessage(new TranslatableComponent("dynamicpack.status_checker.format_not_actual", download), UUID.randomUUID());

        } else if (StatusChecker.isModUpdateAvailable()) {
            Out.println("DynamicPack mod update available: " + com.adamcalculator.dynamicpack.Mod.MODRINTH_URL);

        } else if (!StatusChecker.isChecked()) {
            Out.warn("StatusChecker isChecked = false :(");
        } else {
            Out.println("Mod in actual state in current date!");
        }
    }

    @Override
    public void startSyncThread() {
        new SyncThread(() -> createSyncTask(false)).start();
    }

    @Override
    public void startManuallySync() {
        Thread thread = new Thread(() -> createSyncTask(true).run());
        thread.setName("DynamicPack-ManuallySyncThread" + (DynamicPackMod.manuallySyncThreadCounter++));
        thread.start();
    }

    private SyncingTask createSyncTask(boolean manually) {
        return new SyncingTask(manually) {
            @Override
            public void onSyncStart() {
                if (manually) setToastContent(new TextComponent("DynamicPack"), new TranslatableComponent("dynamicpack.toast.syncStarted"));
            }

            @Override
            public void onSyncDone(boolean reloadRequired) {
                if (manually || reloadRequired) {
                    setToastContent(new TextComponent("DynamicPack"), new TranslatableComponent("dynamicpack.toast.done"));
                }
                if (reloadRequired) {
                    tryToReloadResources();
                }
            }

            @Override
            public void onStateChanged(Pack pack, SyncProgressState state) {
                if (!manually) return;

                if (state instanceof StateDownloading) {
                    StateDownloading downloading = (StateDownloading) state;
                    setToastContent(new TranslatableComponent("dynamicpack.toast.pack.state.downloading.title", pack.getName()), new TranslatableComponent("dynamicpack.toast.pack.state.downloading.description", downloading.getPercentage(), downloading.getName()));

                } else if (state instanceof StateFileDeleted) {
                    StateFileDeleted deleted = (StateFileDeleted) state;
                    setToastContent(new TranslatableComponent("dynamicpack.toast.pack.state.deleting.title", pack.getName()), new TranslatableComponent("dynamicpack.toast.pack.state.deleting.description", deleted.getPath().getFileName().toString()));

                } else {
                    setToastContent(new TranslatableComponent("dynamicpack.toast.pack.state.unknown.title"), new TranslatableComponent("dynamicpack.toast.pack.state.unknown.description"));
                }
            }
        };
    }

    @Override
    public String getCurrentGameVersion() {
        SharedConstants.tryDetectVersion();
        return SharedConstants.getCurrentVersion().getId();
    }

    @Override
    public boolean checkResourcePackMetaValid(String s) {
        // Coped from 1.20.4 port (this is a 1.19.4 port)
        JsonObject pack = GsonHelper.parse(s).getAsJsonObject("pack");
        Number num = pack.get("pack_format").getAsNumber();
        if (num instanceof LazilyParsedNumber) {
            LazilyParsedNumber lazilyParsedNumber = (LazilyParsedNumber) num;
            lazilyParsedNumber.intValue();
        }
        JsonElement description = pack.get("description");
        if (description.isJsonNull()) {
            throw new NullPointerException("description is null in pack.mcmeta");
        }
        return true;
    }

    private void tryToReloadResources() {
        Minecraft client = Minecraft.getInstance();
        if (client != null) {
            if (client.level == null) {
                client.execute(client::reloadResourcePacks);

            } else {
                setToastContent(new TranslatableComponent("dynamicpack.toast.needReload"),
                        new TranslatableComponent("dynamicpack.toast.needReload.description"));
            }
        }
    }
}
