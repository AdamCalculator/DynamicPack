package com.adamcalculator.dynamicpack.client.mixin;

import com.adamcalculator.dynamicpack.client.PackMixinHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * For integrate to ResourcePacks screen
 */
@Mixin(TransferableSelectionList.PackEntry.class)
public abstract class ResourcePackEntryMixin {
    @Shadow @Final private TransferableSelectionList parent;

    @Inject(at = @At("RETURN"), method = "render")
    private void render(PoseStack ctx, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        PackMixinHelper.renderResourcePackEntry(this, ctx, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta, ci);
    }

    @Inject(at = @At("RETURN"), method = "mouseClicked")
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        PackMixinHelper.mouseClicked(this, parent, mouseX, mouseY, button, cir);
    }
}
