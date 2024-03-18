package com.adamcalculator.dynamicpack.mixin.client;

import com.adamcalculator.dynamicpack.DynamicPackModBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.resource.ResourceReload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At("RETURN"), method = "onInitFinished")
    private void dynamicpack$initCheck(RealmsClient realms, ResourceReload reload, RunArgs.QuickPlay quickPlay, CallbackInfo ci) {
        DynamicPackModBase.INSTANCE.minecraftInitialized();
    }
}
