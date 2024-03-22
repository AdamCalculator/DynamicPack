package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.include.modmenu.util.DrawingUtil;
import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.sync.SyncingTask;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Date;
import java.util.function.Consumer;

public class DynamicPackScreen extends Screen {
    private final Screen parent;
    private Pack pack;
    private final MutableText screenDescText;
    private ButtonWidget syncButton;
    private final Consumer<Pack> destroyListener = this::setPack;

    public DynamicPackScreen(Screen parent, Pack pack) {
        super(Text.literal(pack.getName()).formatted(Formatting.BOLD));
        this.pack = pack;
        this.client = MinecraftClient.getInstance();
        this.parent = parent;
        this.screenDescText = Text.translatable("dynamicpack.screen.pack.description");
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
    public void render(MatrixStack context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        syncButton.active = !SyncingTask.isSyncing;
        int h = 20;
        DrawableHelper.drawTextWithShadow(context, this.textRenderer, this.title, 20, 8, 16777215);
        DrawableHelper.drawTextWithShadow(context, this.textRenderer, screenDescText, 20, 20 + h, 16777215);
        DrawableHelper.drawTextWithShadow(context, this.textRenderer, Text.translatable("dynamicpack.screen.pack.remote_type", pack.getRemoteType()), 20, 36 + h, 16777215);
        DrawableHelper.drawTextWithShadow(context, this.textRenderer, Text.translatable("dynamicpack.screen.pack.latestUpdated", pack.getLatestUpdated() < 0 ? "-" : new Date(pack.getLatestUpdated() * 1000)), 20, 52 + h, 16777215);

        if (pack.getLatestException() != null) {
            DrawingUtil.drawWrappedString(context, Text.translatable("dynamicpack.screen.pack.latestException", pack.getLatestException().getMessage()).asTruncatedString(9999), 20, 78 + h, 500, 99, 0xff2222);
            h+=10;
        }

        if (SyncingTask.isSyncing) {
            DrawingUtil.drawWrappedString(context, SyncingTask.syncingLog1, 20, 78+30 + h, 500, 99, 0xCCCCCC);
            DrawingUtil.drawWrappedString(context, SyncingTask.syncingLog2, 20, 78+30+20 + h, 500, 99, 0xCCCCCC);
            DrawingUtil.drawWrappedString(context, SyncingTask.syncingLog3, 20, 78+30+40 + h, 500, 99, 0xCCCCCC);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        addDrawableChild(syncButton = Compat.createButton(
                Text.translatable("dynamicpack.screen.pack.manually_sync"),
                        () -> DynamicPackModBase.INSTANCE.startManuallySync(),
                100, 20, width - 120, 10
        ));

        addDrawableChild(Compat.createButton(ScreenTexts.DONE, this::close, 150, 20, this.width / 2 + 4, this.height - 48));
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
        pack.removeDestroyListener(destroyListener);
    }
}
