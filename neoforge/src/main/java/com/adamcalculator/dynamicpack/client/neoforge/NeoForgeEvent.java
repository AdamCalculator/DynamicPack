package com.adamcalculator.dynamicpack.client.neoforge;


import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@Mod.EventBusSubscriber
public class NeoForgeEvent {
    @SubscribeEvent
    public static void onEvent(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LocalPlayer localPlayer) {
            NeoForgeDynamicPackMod.NEOFORGE_DYNAMICPACK.forgeOnWorldJoin(localPlayer);
        }
    }
}
