package com.adamcalculator.dynamicpack.client.mixin;

import com.adamcalculator.dynamicpack.client.PackMixinHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Inject(at = @At("RETURN"), method = "<init>")
    private void dynamicpack$initCheck(GameConfig args, CallbackInfo ci) {
        PackMixinHelper.minecraftInitReturned();
    }
}
