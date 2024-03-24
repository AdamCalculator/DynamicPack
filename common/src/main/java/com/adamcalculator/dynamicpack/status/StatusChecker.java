package com.adamcalculator.dynamicpack.status;

import com.adamcalculator.dynamicpack.DynamicPackMod;
import com.adamcalculator.dynamicpack.Mod;
import com.adamcalculator.dynamicpack.util.Loader;
import com.adamcalculator.dynamicpack.util.Out;
import com.adamcalculator.dynamicpack.util.Urls;
import org.json.JSONObject;

public class StatusChecker {
    private static final String URL = "https://adamcalculator.github.io/DynamicPack/dynamicpack.status.v1.json";


    private static boolean isUpdateAvailable = false;
    private static boolean isFormatActual = true;
    private static boolean isSafe = true;
    private static boolean isChecked = false;

    public static void check() throws Exception {
        Out.println("Checking status...");
        String s = Urls.parseContent(URL, 1024 * 1024 * 128);
        JSONObject j = new JSONObject(s);
        String platformKey;
        JSONObject lat = j.getJSONObject(platformKey = getLatestKeyForPlatform(DynamicPackMod.getLoader()));
        isUpdateAvailable = lat.getLong("build") > Mod.VERSION_BUILD;
        isSafe = lat.getLong("safe") <= Mod.VERSION_BUILD;
        isFormatActual = lat.getLong("format") <= Mod.VERSION_BUILD;

        isChecked = true;
        Out.println(String.format("Status checked! platformKey=%s, isSafe=%s, isFormatActual=%s, isUpdateAvailable=%s", platformKey, isSafe, isFormatActual, isUpdateAvailable));
    }

    private static String getLatestKeyForPlatform(Loader loader) {
        if (loader == null) {
            return "latest_version";
        }

        if (loader == Loader.FABRIC) {
            return "latest_version_fabric";
        }
        if (loader == Loader.FORGE) {
            return "latest_version_forge";
        }
        if (loader == Loader.NEO_FORGE) {
            return "latest_version_neoforge";
        }
        return "latest_version";
    }

    public static boolean isBlockUpdating(String remoteType) {
        if (remoteType.equals("modrinth")) {
            return false;
        }
        return !isSafe();
    }


    public static boolean isModUpdateAvailable() {
        return isUpdateAvailable;
    }

    public static boolean isSafe() {
        return isSafe;
    }

    public static boolean isFormatActual() {
        return isFormatActual;
    }

    public static boolean isChecked() {
        return isChecked;
    }
}
