package com.adamcalculator.dynamicpack.client;

import com.adamcalculator.dynamicpack.DynamicPackMod;
import com.adamcalculator.dynamicpack.pack.DynamicRepoRemote;
import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.sync.SyncingTask;
import com.adamcalculator.dynamicpack.util.TranslatableException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.function.Consumer;

public class DynamicPackScreen extends Screen {
    private final Screen parent;
    private Pack pack;
    private final MutableComponent screenDescText;
    private Button syncButton;
    private final Consumer<Pack> destroyListener = this::setPack;
    private Button contentsButton;

    public DynamicPackScreen(Screen parent, Pack pack) {
        super(Component.literal(pack.getName()).withStyle(ChatFormatting.BOLD));
        this.pack = pack;
        this.minecraft = Minecraft.getInstance();
        this.parent = parent;
        this.screenDescText = Component.translatable("dynamicpack.screen.pack.description");
        setPack(pack);
    }

    private void setPack(Pack pack) {
        if (this.pack != null) {
            this.pack.removeDestroyListener(destroyListener);
        }
        this.pack = pack;
        pack.addDestroyListener(destroyListener);
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        syncButton.active = !SyncingTask.isSyncing;
        contentsButton.active = !SyncingTask.isSyncing;
        int h = 20;
        Compat.drawString(context, this.font, this.title, 20, 8, 16777215);
        Compat.drawString(context, this.font, screenDescText, 20, 20 + h, 16777215);
        Compat.drawString(context, this.font, Component.translatable("dynamicpack.screen.pack.remote_type", pack.getRemoteType()), 20, 36 + h, 16777215);
        Compat.drawString(context, this.font, Component.translatable("dynamicpack.screen.pack.latestUpdated", pack.getLatestUpdated() < 0 ? "-" : new Date(pack.getLatestUpdated() * 1000)), 20, 52 + h, 16777215);

        if (pack.getLatestException() != null) {
            Compat.drawWrappedString(context, Component.translatable("dynamicpack.screen.pack.latestException", TranslatableException.getComponentFromException(pack.getLatestException())).getString(512), 20, 78 + h, 500, 99, 0xff2222);
            h+=10;
        }

        if (SyncingTask.isSyncing) {
            Compat.drawWrappedString(context, SyncingTask.syncingLog1, 20, 78+30 + h, 500, 99, 0xCCCCCC);
            Compat.drawWrappedString(context, SyncingTask.syncingLog2, 20, 78+30+20 + h, 500, 99, 0xCCCCCC);
            Compat.drawWrappedString(context, SyncingTask.syncingLog3, 20, 78+30+40 + h, 500, 99, 0xCCCCCC);
        }
    }

    @Override
    protected void init() {
        addRenderableWidget(syncButton = Compat.createButton(
                Component.translatable("dynamicpack.screen.pack.manually_sync"),
                        () -> DynamicPackMod.INSTANCE.startManuallySync(),
                100, 20, width - 120, 10
        ));

        addRenderableWidget(Compat.createButton(CommonComponents.GUI_DONE, this::onClose, 150, 20, this.width / 2 + 4, this.height - 48));
        addRenderableWidget(contentsButton = Compat.createButton(Component.translatable("dynamicpack.screen.pack.dynamic.contents"), () -> {
            Minecraft.getInstance().setScreen(new ContentsScreen(this, pack));
        }, 150, 20, this.width / 2 + 4-160, this.height - 48));
        contentsButton.visible = pack.getRemote() instanceof DynamicRepoRemote;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
        pack.removeDestroyListener(destroyListener);
    }
}
