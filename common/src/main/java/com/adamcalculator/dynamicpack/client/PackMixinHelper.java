package com.adamcalculator.dynamicpack.client;

import com.adamcalculator.dynamicpack.DynamicPackMod;
import com.adamcalculator.dynamicpack.pack.Pack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PackMixinHelper {
    private static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.tryBuild("dynamicpack", "select_button.png");
    private static final ResourceLocation BUTTON_WARNING_TEXTURE = ResourceLocation.tryBuild("dynamicpack", "select_button_warning.png");
    private static final ResourceLocation BUTTON_SYNCING_TEXTURE = ResourceLocation.tryBuild("dynamicpack", "select_button_syncing.png");

    private static void drawTexture(PoseStack context, Pack pack, int x, int y, int i, int j, boolean hovered) {
        Exception latestException = pack.getLatestException();
        if (pack.isSyncing()) {
            Compat.drawTexture(context, BUTTON_TEXTURE, x + 174, y+16, 0.0F, ((i >= 174 && j >= 16 && hovered) ? 16f : 0f), 16, 16, 16, 32);


            double alpha = System.currentTimeMillis() / 200d;
            int xshift = (int) (Math.sin(alpha) * 6.9d);
            int yshift = (int) (Math.cos(alpha) * 6.9d);

            Compat.drawTexture(context, BUTTON_SYNCING_TEXTURE, x + 174 + xshift+6, y+16 + yshift+6, 0.0F, ((i >= 174 && j >= 16 && hovered) ? 16f : 0f), 4, 4, 16, 32);

        } else if (latestException != null) {
            Compat.drawTexture(context, BUTTON_WARNING_TEXTURE, x + 174, y+16, 0.0F, ((i >= 174 && j >= 16 && hovered) ? 16f : 0f), 16, 16, 16, 32);

        } else {
            Compat.drawTexture(context, BUTTON_TEXTURE, x + 174, y+16, 0.0F, ((i >= 174 && j >= 16 && hovered) ? 16f : 0f), 16, 16, 16, 32);
        }
    }

    public static void renderResourcePackEntry(Object resourcePackEntryMixin, PoseStack context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        TransferableSelectionList.PackEntry entry = (TransferableSelectionList.PackEntry) resourcePackEntryMixin;
        Pack pack = DynamicPackMod.INSTANCE.getDynamicPackByMinecraftName(entry.getPackId());
        if (pack != null) {
            int i = mouseX - x;
            int j = mouseY - y;
            drawTexture(context, pack, x, y, i, j, hovered);
        }
    }

    public static void mouseClicked(Object resourcePackEntryMixin, TransferableSelectionList widget, double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        TransferableSelectionList.PackEntry entry = (TransferableSelectionList.PackEntry) resourcePackEntryMixin;
        Pack pack = DynamicPackMod.INSTANCE.getDynamicPackByMinecraftName(entry.getPackId());
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
        Minecraft.getInstance().setScreen(new DynamicPackScreen(Minecraft.getInstance().screen, pack));
    }

    public static void minecraftInitReturned() {
        DynamicPackMod.INSTANCE.minecraftInitialized();
    }

    public static void updatePacksMinecraftRequest() {
        DynamicPackMod.INSTANCE.rescanPacks();
    }
}
