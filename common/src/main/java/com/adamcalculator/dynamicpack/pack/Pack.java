package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.DynamicPackModBase;
import com.adamcalculator.dynamicpack.PackUtil;
import com.adamcalculator.dynamicpack.sync.PackSyncProgress;
import com.adamcalculator.dynamicpack.util.AFiles;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Pack {
    private final File location;
    JSONObject cachedJson;
    private boolean cachedUpdateAvailable;
    long current_build;
    private Remote remote;
    private boolean isSyncing = false;

    public Pack(File location, JSONObject json) {
        this.location = location;
        this.cachedJson = json;

        JSONObject current = json.getJSONObject("current");

        JSONObject remote = json.getJSONObject("remote");
        String remoteType = remote.getString("type");
        if (Objects.equals(remoteType, "dynamic_repo")) {
            this.remote = new DynamicRepoRemote(this, remote);
            this.current_build = current.getLong("build");

        } else if (Objects.equals(remoteType, "modrinth")) {
            this.remote = new ModrinthRemote(this, remote);

        } else {
            throw new RuntimeException("Unknown remote format: " + remoteType);
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

    public long getCurrentBuild() {
        return current_build;
    }

    public String getCurrentUnique() {
        return cachedJson.getJSONObject("current").optString("version", "");
    }


    public String getCurrentVersionNumber() {
        return cachedJson.getJSONObject("current").optString("version_number", "");
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

        boolean reloadRequired = remote.sync(progress);

        isSyncing = false;
        progress.done(reloadRequired);
    }

    private void checkSafePackMinecraftMeta() throws IOException {
        final String meta = """
                {
                  "pack": {
                    "pack_format": 17,
                    "description": "Unknown DynamicPack resource-pack..."
                  }
                }
                """;
        if (isZip()) {
            ZipFile zipFile = new ZipFile(location);
            ZipEntry zipEntry = zipFile.getEntry(DynamicPackModBase.MINECRAFT_META);
            boolean safe = zipEntry != null;
            if (safe) {
                safe = checkMinecraftMetaIsValid(PackUtil.readString(zipFile.getInputStream(zipEntry)));
            }
            if (!safe) {
                PackUtil.addFileToZip(location, DynamicPackModBase.MINECRAFT_META, meta);
            }
        } else {
            File file = new File(location, DynamicPackModBase.MINECRAFT_META);
            boolean safe = PackUtil.isPathFileExists(file.toPath());
            if (safe) {
                safe = checkMinecraftMetaIsValid(AFiles.read(file));
            }
            if (!safe) {
                AFiles.write(file, meta);
            }
        }
    }

    private boolean checkMinecraftMetaIsValid(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONObject pack = jsonObject.getJSONObject("pack");
            pack.getLong("pack_format");
            if (pack.getString("description").length() > 60) {
                throw new Exception("Description length");
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean isContentActive(String id) {
        return true; // todo
    }
}
