package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.DynamicPackModBase;
import com.adamcalculator.dynamicpack.PackUtil;
import com.adamcalculator.dynamicpack.SyncProgress;
import com.adamcalculator.dynamicpack.util.*;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.zip.ZipFile;

public class Pack {
    private final File location;
    private JSONObject cachedJson;
    private boolean cachedUpdateAvailable;
    private long current_build;
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

    public void sync(SyncProgress progress, boolean manually) throws Exception {
        try {
            sync0(progress, manually);

        } catch (Exception e) {
            isSyncing = false;
            throw e;
        }
    }

    private void sync0(SyncProgress progress, boolean manually) throws Exception {
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

        boolean reloadRequired = false;
        if (remote instanceof ModrinthRemote modrinthRemote) {
            reloadRequired = modrinthSync(modrinthRemote, progress);

        } else if (remote instanceof DynamicRepoRemote dynamicRepoRemote) {
            reloadRequired = dynamicRepoSync(dynamicRepoRemote, progress);
        } else {
            isSyncing = false;
        }
        isSyncing = false;
        progress.done(reloadRequired);
    }

    private boolean dynamicRepoSync(DynamicRepoRemote dynamicRepoRemote, SyncProgress progress) throws Exception {
        String packUrlContent;
        if (dynamicRepoRemote.skipSign) {
            packUrlContent = Urls.parseContent(dynamicRepoRemote.packUrl, Mod.MOD_FILES_LIMIT);
            Out.warn("Dynamic pack " + location.getName() + " is skipping signing.");
            progress.textLog("File parsed, verify skipped.");

        } else {
            packUrlContent = Urls.parseContentAndVerify(dynamicRepoRemote.packSigUrl, dynamicRepoRemote.packUrl, dynamicRepoRemote.publicKey, Mod.MOD_FILES_LIMIT);
            progress.textLog("Success parse and verify file.");
        }

        JSONObject j = new JSONObject(packUrlContent);
        if (j.getLong("formatVersion") != 1) {
            throw new RuntimeException("Incompatible formatVersion!");
        }


        DynamicRepoSyncProcessV1 dynamicRepoSyncProcessV1 = new DynamicRepoSyncProcessV1(this, dynamicRepoRemote, progress, j);
        try {
            dynamicRepoSyncProcessV1.run();
            dynamicRepoSyncProcessV1.close();

        } catch (Exception e) {
            dynamicRepoSyncProcessV1.close();
            throw e;
        }

        this.current_build = j.getLong("build");
        cachedJson.getJSONObject("current").put("build", this.current_build);

        if (isZip()) {
            PackUtil.addFileToZip(location, DynamicPackModBase.CLIENT_FILE, cachedJson.toString(2));
        } else {
            AFiles.write(new File(location, DynamicPackModBase.CLIENT_FILE), cachedJson.toString(2));
        }
        return true;
    }

    public boolean isContentActive(String id) {
        return true; // todo
    }

    private boolean modrinthSync(ModrinthRemote modrinthRemote, SyncProgress progress) throws IOException, NoSuchAlgorithmException {
        progress.textLog("getting latest version on modrinth...");
        ModrinthRemote.LatestModrinthVersion latest = modrinthRemote.getLatest();

        progress.textLog("downloading...");
        File file = null;
        int attempts = 3;
        while (attempts > 0) {
            file = Urls.downloadFileToTemp(latest.url, "dynamicpack_download", ".zip", Mod.MODRINTH_HTTPS_FILE_SIZE_LIMIT, new FileDownloadConsumer(){
                @Override
                public void onUpdate(FileDownloadConsumer it) {
                    float percentage = it.getPercentage();
                    progress.downloading("Modrinth pack (zip)", percentage);
                }
            });

            if (Hashes.calcHashForFile(file).equals(latest.fileHash)) {
                progress.textLog("Download done! Hashes is equals.");
                break;
            }
            attempts--;
        }
        if (attempts == 0) {
            throw new RuntimeException("Failed to download correct file from modrinth.");
        }

        ZipFile zipFile = new ZipFile(file);
        boolean isDynamicPack = zipFile.getEntry(DynamicPackModBase.CLIENT_FILE) != null;

        cachedJson.getJSONObject("current").put("version", latest.latestId);
        cachedJson.getJSONObject("current").remove("version_number");


        if (!isDynamicPack) {
            PackUtil.addFileToZip(file, DynamicPackModBase.CLIENT_FILE, cachedJson.toString(2));
        }
        if (this.isZip()) {
            AFiles.moveFile(file, this.location);

        } else {
            AFiles.deleteDirectory(this.location);
            AFiles.unzip(file, this.location);
        }
        progress.textLog("dynamicmcpack.json is updated.");

        progress.textLog("done!");
        return true;
    }
}
