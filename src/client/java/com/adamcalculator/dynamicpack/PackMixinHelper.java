package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PackMixinHelper {
    public static void renderResourcePackEntry(Object resourcePackEntryMixin, DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        PackListWidget.ResourcePackEntry entry = (PackListWidget.ResourcePackEntry) resourcePackEntryMixin;
        if (DynamicPackModBase.INSTANCE.isNameIsDynamic(entry.getName())) {
            int i = mouseX - x;
            int j = mouseY - y;
            context.drawTexture(Identifier.of("dynamicpack", "select_button.png"), x + 174, y+16, 0.0F, ((i >= 174 && j >= 16 && hovered) ? 16f : 0f), 16, 16, 16, 32);
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