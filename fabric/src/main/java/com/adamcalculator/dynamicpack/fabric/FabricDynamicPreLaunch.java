package com.adamcalculator.dynamicpack.fabric;

import com.adamcalculator.dynamicpack.client.DynamicPackModBase;
import com.adamcalculator.dynamicpack.util.Loader;
import com.adamcalculator.dynamicpack.util.Out;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.client.Minecraft;

public class FabricDynamicPreLaunch extends DynamicPackModBase implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        Out.println("DynamicPack loaded. Hello fabric world!");

        var gameDir = FabricLoader.getInstance().getGameDir().toFile();
        init(gameDir, Loader.FABRIC);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onWorldJoinForUpdateChecks(Minecraft.getInstance().player));
    }
}
