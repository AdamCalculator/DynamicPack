package com.adamcalculator.dynamicpack.mixin.client;

import com.adamcalculator.dynamicpack.DynamicPackModBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At("RETURN"), method = "<init>")
    private void dynamicpack$initCheck(RunArgs args, CallbackInfo ci) {
        DynamicPackModBase.INSTANCE.minecraftInitialized();
    }
}
