package com.adamcalculator.dynamicpack.mixin.client;

import com.adamcalculator.dynamicpack.PackMixinHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PackListWidget.ResourcePackEntry.class)
public abstract class ResourcePackEntryMixin {
    @Shadow @Final private PackListWidget widget;

    @Inject(at = @At("RETURN"), method = "render")
    private void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        PackMixinHelper.renderResourcePackEntry(this, context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta, ci);
    }

    @Inject(at = @At("RETURN"), method = "mouseClicked")
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        PackMixinHelper.mouseClicked(this, widget, mouseX, mouseY, button, cir);
    }
}
