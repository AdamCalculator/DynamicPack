package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.include.modmenu.util.DrawingUtil;
import com.adamcalculator.dynamicpack.pack.Pack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PackMixinHelper {
    private static final Identifier BUTTON_TEXTURE = Identifier.of("dynamicpack", "select_button.png");
    private static final Identifier BUTTON_WARNING_TEXTURE = Identifier.of("dynamicpack", "select_button_warning.png");
    private static final Identifier BUTTON_SYNCING_TEXTURE = Identifier.of("dynamicpack", "select_button_syncing.png");

    private static void drawTexture(DrawContext context, Pack pack, int x, int y, int i, int j, boolean hovered) {
        Exception latestException = pack.getLatestException();
        if (pack.isSyncing()) {
            DrawingUtil.drawTexture(context, BUTTON_TEXTURE, x + 174, y+16, 0.0F, ((i >= 174 && j >= 16 && hovered) ? 16f : 0f), 16, 16, 16, 32);


            double alpha = System.currentTimeMillis() / 200d;
            int xshift = (int) (Math.sin(alpha) * 6.9d);
            int yshift = (int) (Math.cos(alpha) * 6.9d);

            DrawingUtil.drawTexture(context, BUTTON_SYNCING_TEXTURE, x + 174 + xshift+6, y+16 + yshift+6, 0.0F, ((i >= 174 && j >= 16 && hovered) ? 16f : 0f), 4, 4, 16, 32);

        } else if (latestException != null) {
            DrawingUtil.drawTexture(context, BUTTON_WARNING_TEXTURE, x + 174, y+16, 0.0F, ((i >= 174 && j >= 16 && hovered) ? 16f : 0f), 16, 16, 16, 32);

        } else {
            DrawingUtil.drawTexture(context, BUTTON_TEXTURE, x + 174, y+16, 0.0F, ((i >= 174 && j >= 16 && hovered) ? 16f : 0f), 16, 16, 16, 32);
        }
    }

    public static void renderResourcePackEntry(Object resourcePackEntryMixin, DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        PackListWidget.ResourcePackEntry entry = (PackListWidget.ResourcePackEntry) resourcePackEntryMixin;
        Pack pack = DynamicPackModBase.INSTANCE.getDynamicPackByMinecraftName(entry.getName());
        if (pack != null) {
            int i = mouseX - x;
            int j = mouseY - y;
            drawTexture(context, pack, x, y, i, j, hovered);
        }
    }

    public static void mouseClicked(Object resourcePackEntryMixin, PackListWidget widget, double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        PackListWidget.ResourcePackEntry entry = (PackListWidget.ResourcePackEntry) resourcePackEntryMixin;
        Pack pack = DynamicPackModBase.INSTANCE.getDynamicPackByMinecraftName(entry.getName());
        if (pack != null) {
            double d = mouseX - (double)widget.getRowLeft();
            double e = mouseY - (double)widget.getRowTop(widget.children().indexOf(entry));

            if (d >= 174) {
                if (e >= 16) {
                   openPackScreen(pack);
                }
            }
        }
    }

    private static void openPackScreen(Pack pack) {
        MinecraftClient.getInstance().setScreen(new DynamicPackScreen(MinecraftClient.getInstance().currentScreen, pack));
    }
}
