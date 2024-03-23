package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.util.Loader;
import com.adamcalculator.dynamicpack.util.Out;

public class Support {
    public static boolean _getDefaultVerifySkipResult() {
        Out.securityWarning("=========== Security warning ============");
        Out.securityWarning("# OpenPGP verifying is skipped because on this platform: " + DynamicPackMod.getLoader() + "; and " + Mod.VERSION_NAME + " mod version it currently not support OpenGPG (bouncycastle library)!");
        Out.securityWarning("# Packs update-uploader not checked correctly!");
        Out.securityWarning("# If you can help with this problem: see github: https://github.com/AdamCalculator/DynamicPack");
        Out.securityWarning("=========================================");
        return true;
    }

    public static boolean _isSkipGPGVerify() {
        return DynamicPackMod.getLoader() == Loader.FORGE || DynamicPackMod.getLoader() == Loader.NEO_FORGE;
    }
}
