package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.sync.PackSyncProgress;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Abstract remote of pack
 */
public abstract class Remote {
    private static boolean initialized = false;
    public static final HashMap<String, Supplier<Remote>> REMOTES = new HashMap<>();

    public static void initRemoteTypes() {
        if (initialized) {
            return;
        }
        initialized = true;
        REMOTES.put("modrinth", ModrinthRemote::new);
        REMOTES.put("dynamic_repo", DynamicRepoRemote::new);
    }

    public abstract void init(Pack pack, JSONObject remote, JSONObject current);

    public abstract boolean checkUpdateAvailable() throws IOException;

    public abstract boolean sync(PackSyncProgress progress) throws IOException, NoSuchAlgorithmException;
}
