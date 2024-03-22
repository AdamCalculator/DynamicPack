package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.DynamicPackModBase;
import com.adamcalculator.dynamicpack.PackUtil;
import com.adamcalculator.dynamicpack.status.StatusChecker;
import com.adamcalculator.dynamicpack.sync.PackSyncProgress;
import com.adamcalculator.dynamicpack.util.AFiles;
import com.adamcalculator.dynamicpack.util.Out;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    private final String remoteTypeStr;
    private Exception latestException;
    private final List<Consumer<Pack>> destroyListeners = new ArrayList<>();
    private boolean destroyed = false;


    public Pack(File location, JSONObject json) {
        this.location = location;
        this.cachedJson = json;

        try {
            JSONObject remote = json.getJSONObject("remote");
            String remoteType = remote.getString("type");
            this.remoteTypeStr = remoteType;
            this.remote = Remote.REMOTES.get(remoteType).get();
            this.remote.init(this, remote, cachedJson.getJSONObject("current"));

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse remote", e);
        }
    }

    public boolean isSyncing() {
        return isSyncing;
    }

    // See StatusChecker for this.
    // Developer can block network for specify version in dynamicpack.status.v1.json by security questions
    public boolean isNetworkBlocked() {
        return StatusChecker.isBlockUpdating(remoteTypeStr);
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
        checkNetwork();
        return cachedUpdateAvailable = remote.checkUpdateAvailable();
    }

    public boolean getCachedUpdateAvailableStatus() {
        return cachedUpdateAvailable;
    }

    public void sync(PackSyncProgress progress, boolean manually) throws Exception {
        try {
            sync0(progress, manually);
            checkSafePackMinecraftMeta();
            setLatestException(null);
        } catch (Exception e) {
            isSyncing = false;
            setLatestException(e);
            try {
                checkSafePackMinecraftMeta();
            } catch (Exception e2) {
                Out.error("Error while check safe pack meta", e);
            }
            throw e;
        }
    }

    private void sync0(PackSyncProgress progress, boolean manually) throws Exception {
        if (isSyncing) {
            progress.textLog("already syncing...");
            progress.done(false);
            return;
        }

        checkNetwork();

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

    private void checkNetwork() {
        if (isNetworkBlocked()) {
            throw new SecurityException("Network is blocked for remote_type=" + remoteTypeStr + " current version of mod not safe. Update mod!");
        }
    }

    private void checkSafePackMinecraftMeta() throws Exception {
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

    public String getRemoteType() {
        return remoteTypeStr;
    }

    public void setLatestException(Exception e) {
        Out.debug(this + ": latestExcep="+e);
        this.latestException = e;
    }

    public Exception getLatestException() {
        return latestException;
    }

    public void saveReScanData(Pack oldestPack) {
        if (oldestPack == null) return;
        oldestPack.markAsDestroyed(this);

        if (this.latestException == null) {
            this.latestException = oldestPack.latestException;
        }
    }

    public void addDestroyListener(Consumer<Pack> runnable) {
        destroyListeners.add(runnable);
    }

    public void removeDestroyListener(Consumer<Pack> runnable) {
        destroyListeners.remove(runnable);
    }

    private void markAsDestroyed(Pack heirPack) {
        for (Consumer<Pack> runnable : destroyListeners.toArray(new Consumer[0])) {
            runnable.accept(heirPack);
        }
        destroyListeners.clear();
        this.destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
