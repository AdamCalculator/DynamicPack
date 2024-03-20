package com.adamcalculator.dynamicpack.mixin.client;

import com.adamcalculator.dynamicpack.DynamicPackModBase;
import net.minecraft.client.gui.screen.pack.PackScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackScreen.class)
public class ResourcePackScreenMixin {
    @Inject(at = @At("RETURN"), method = "updatePackLists")
    private void updatePackLists(CallbackInfo ci) {
        DynamicPackModBase.INSTANCE.rescanPacks();
    }
}
