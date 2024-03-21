package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.sync.SyncingTask;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PackMixinHelper {
    private static final Identifier BUTTON_TEXTURE = Identifier.of("dynamicpack", "select_button.png");
    private static final Identifier BUTTON_WARNING_TEXTURE = Identifier.of("dynamicpack", "select_button_warning.png");

    public static void renderResourcePackEntry(Object resourcePackEntryMixin, MatrixStack context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        PackListWidget.ResourcePackEntry entry = (PackListWidget.ResourcePackEntry) resourcePackEntryMixin;
        Pack pack = DynamicPackModBase.INSTANCE.getDynamicPackByMinecraftName(entry.getName());
        if (pack != null && !SyncingTask.isSyncing) {
            int i = mouseX - x;
            int j = mouseY - y;
         
            // TODO(adam) 2024.03.20: INDICATOR NOT WORKING IN 1.19.4 BACKPORT...
            //DrawableHelper.drawTexture(context, Identifier.of("dynamicpack", "select_button.png"), x + 174, y+16, 0.0F, ((i >= 174 && j >= 16 && hovered) ? 16f : 0f), 16, 16, 16, 32);
        
           context.drawTexture(pack.getLatestException() != null ? BUTTON_WARNING_TEXTURE : BUTTON_TEXTURE, x + 174, y+16, 0.0F, ((i >= 174 && j >= 16 && hovered) ? 16f : 0f), 16, 16, 16, 32);

        }
    }

    public static void mouseClicked(Object resourcePackEntryMixin, PackListWidget widget, double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        PackListWidget.ResourcePackEntry entry = (PackListWidget.ResourcePackEntry) resourcePackEntryMixin;
        Pack pack = DynamicPackModBase.INSTANCE.getDynamicPackByMinecraftName(entry.getName());
        if (pack != null && !SyncingTask.isSyncing) {
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
