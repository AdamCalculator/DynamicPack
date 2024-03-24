package com.adamcalculator.dynamicpack.client.neoforge;
//
//import com.adamcalculator.dynamicpack.client.DynamicPackModBase;
//import com.adamcalculator.dynamicpack.util.Loader;
//import com.adamcalculator.dynamicpack.util.Out;
//import net.minecraft.client.player.LocalPlayer;
//import net.neoforged.fml.common.Mod;
//import net.neoforged.fml.loading.FMLPaths;
//
//@Mod(com.adamcalculator.dynamicpack.Mod.MOD_ID)
//public class NeoForgeDynamicPackMod extends DynamicPackModBase {
//    public static NeoForgeDynamicPackMod NEOFORGE_DYNAMICPACK;
//
//    public NeoForgeDynamicPackMod() {
//        super();
//        NEOFORGE_DYNAMICPACK = this;
//
//        var gameDir = FMLPaths.GAMEDIR.get().toFile();
//        init(gameDir, Loader.NEO_FORGE);
//
//        Out.println("DynamicPack loaded. Hello neoforge world!");
//    }
//
//    public void forgeOnWorldJoin(LocalPlayer localPlayer) {
//        onWorldJoinForUpdateChecks(localPlayer);
//    }
//}
