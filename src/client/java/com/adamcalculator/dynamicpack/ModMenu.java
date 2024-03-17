package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.util.Mod;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return Mod.isRelease() ? null : parent -> new DebugScreen();
    }
}