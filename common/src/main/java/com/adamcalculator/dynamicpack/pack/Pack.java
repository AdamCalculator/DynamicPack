package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.DynamicPackModBase;
import com.adamcalculator.dynamicpack.PackUtil;
import com.adamcalculator.dynamicpack.sync.PackSyncProgress;
import com.adamcalculator.dynamicpack.util.AFiles;
import com.adamcalculator.dynamicpack.util.Out;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Pack {
    public static final String UNKNOWN_PACK_MCMETA = """
                {
                  "pack": {
                    "pack_format": 17,
                    "description": "Unknown DynamicPack resource-pack..."
                  }
                }
                """;

    private final File location;
    private final JSONObject cachedJson;
    private final Remote remote;

    private boolean cachedUpdateAvailable;
    private boolean isSyncing = false;


    public Pack(File location, JSONObject json) {
        this.location = location;
        this.cachedJson = json;

        try {
            JSONObject remote = json.getJSONObject("remote");
            String remoteType = remote.getString("type");
            this.remote = Remote.REMOTES.get(remoteType).get();
            this.remote.init(this, remote, cachedJson.getJSONObject("current"));

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse remote", e);
        }
    }

    public boolean isZip() {
        if (location.isDirectory()) {
            return false;
        }
        return location.getName().toLowerCase().endsWith(".zip");
    }

    public File getLocation() {
        return location;
    }

    public String getName() {
        return location.getName();
    }

    public JSONObject getPackJson() {
        return cachedJson;
    }


    public void updateJsonLatestUpdate() {
        cachedJson.getJSONObject("current").put("latest_updated", System.currentTimeMillis() / 1000);
    }

    public long getLatestUpdated() {
        try {
            return cachedJson.getJSONObject("current").getLong("latest_updated");

        } catch (Exception e) {
            return -1;
        }
    }

    public boolean checkIsUpdateAvailable() throws IOException {
        return cachedUpdateAvailable = remote.checkUpdateAvailable();
    }

    public boolean getCachedUpdateAvailableStatus() {
        return cachedUpdateAvailable;
    }

    public void sync(PackSyncProgress progress, boolean manually) throws Exception {
        try {
            sync0(progress, manually);
            checkSafePackMinecraftMeta();
        } catch (Exception e) {
            isSyncing = false;
            checkSafePackMinecraftMeta();
            throw e;
        }
    }

    private void sync0(PackSyncProgress progress, boolean manually) throws Exception {
        if (isSyncing) {
            progress.textLog("already syncing...");
            progress.done(false);
            return;
        }
        if (!checkIsUpdateAvailable() && !manually) {
            progress.textLog("update not available");
            progress.done(false);
            return;
        }

        isSyncing = true;
        progress.start();
        progress.textLog("start syncing...");

        boolean reloadRequired = remote.sync(progress, manually);

        isSyncing = false;
        progress.done(reloadRequired);
    }

    private void checkSafePackMinecraftMeta() throws IOException {
        PackUtil.openPackFileSystem(location, path -> {
            Path mcmeta = path.resolve(DynamicPackModBase.MINECRAFT_META);
            boolean safe = PackUtil.isPathFileExists(mcmeta);
            if (safe) {
                try {
                    safe = checkMinecraftMetaIsValid(PackUtil.readString(mcmeta));
                } catch (IOException ignored) {
                    safe = false;
                }
            }
            if (!safe) {
                AFiles.nioWriteText(mcmeta, UNKNOWN_PACK_MCMETA);
            }
        });
    }

    private boolean checkMinecraftMetaIsValid(String s) {
        try {
            return DynamicPackModBase.INSTANCE.checkResourcePackMetaValid(s);

        } catch (Exception e) {
            Out.error("Error while check meta valid.", e);
            return false;
        }
    }
}
