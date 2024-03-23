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
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.GsonHelper;

public abstract class DynamicPackModBase extends DynamicPackMod {
    private SystemToast toast = null;
    private long toastUpdated = 0;

    public void setToastContent(Component title, Component text) {
        if (!isMinecraftInitialized()) {
            return;
        }

        if (toast == null || (System.currentTimeMillis() - toastUpdated > 1000*5)) {
            ToastComponent toastManager = Minecraft.getInstance().getToasts();
            toastManager.addToast(toast = new SystemToast(/*1.20.4 port*/new SystemToast.SystemToastId(5000), title, text));
        } else {
            toast.reset(title, text);
        }
        toastUpdated = System.currentTimeMillis();
    }

    public void onWorldJoinForUpdateChecks(LocalPlayer player) {
        if (Mod.isDebugMessageOnWorldJoin()) {
            player.sendSystemMessage(Component.literal("Debug message on world join").withStyle(ChatFormatting.GREEN));
        }
        Component download = Component.translatable("dynamicpack.status_checker.download")
                .withStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("dynamicpack.status_checker.download.hover", Component.literal(com.adamcalculator.dynamicpack.Mod.MODRINTH_URL).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.AQUA))))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, com.adamcalculator.dynamicpack.Mod.MODRINTH_URL))
                )
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE);


        if (player == null) {
            Out.warn("player == null on world join");

        } else if (!StatusChecker.isSafe()) {
            player.sendSystemMessage(Component.translatable("dynamicpack.status_checker.not_safe", download));
            setToastContent(Component.translatable("dynamicpack.status_checker.not_safe.toast.title"),
                    Component.translatable("dynamicpack.status_checker.not_safe.toast.description"));

        } else if (!StatusChecker.isFormatActual()) {
            player.sendSystemMessage(Component.translatable("dynamicpack.status_checker.format_not_actual", download));

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
                if (manually) setToastContent(Component.literal("DynamicPack"), Component.translatable("dynamicpack.toast.syncStarted"));
            }

            @Override
            public void onSyncDone(boolean reloadRequired) {
                if (manually || reloadRequired) {
                    setToastContent(Component.literal("DynamicPack"), Component.translatable("dynamicpack.toast.done"));
                }
                if (reloadRequired) {
                    tryToReloadResources();
                }
            }

            @Override
            public void onStateChanged(Pack pack, SyncProgressState state) {
                if (!manually) return;

                if (state instanceof StateDownloading downloading) {
                    setToastContent(Component.translatable("dynamicpack.toast.pack.state.downloading.title", pack.getName()), Component.translatable("dynamicpack.toast.pack.state.downloading.description", downloading.getPercentage(), downloading.getName()));

                } else if (state instanceof StateFileDeleted deleted) {
                    setToastContent(Component.translatable("dynamicpack.toast.pack.state.deleting.title", pack.getName()), Component.translatable("dynamicpack.toast.pack.state.deleting.description", deleted.getPath().getFileName().toString()));

                } else {
                    setToastContent(Component.translatable("dynamicpack.toast.pack.state.unknown.title"), Component.translatable("dynamicpack.toast.pack.state.unknown.description"));
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
        // 1.20.4 port
        JsonObject pack = GsonHelper.parse(s).getAsJsonObject("pack");
        if (pack.get("pack_format").getAsNumber() instanceof LazilyParsedNumber lazilyParsedNumber) {
            lazilyParsedNumber.intValue();
        }
        JsonElement description = pack.get("description");
        if (description.isJsonNull()) {
            throw new NullPointerException("description is null in pack.mcmeta");
        }
        //MetadataSectionType.fromCodec("not used in this case string", PackMetadataSection.CODEC).fromJson(GsonHelper.parse(s).getAsJsonObject("pack"));
        return true;
    }

    private void tryToReloadResources() {
        Minecraft client = Minecraft.getInstance();
        if (client != null) {
            if (client.level == null) {
                client.execute(client::reloadResourcePacks);

            } else {
                setToastContent(Component.translatable("dynamicpack.toast.needReload"),
                        Component.translatable("dynamicpack.toast.needReload.description"));
            }
        }
    }
}
