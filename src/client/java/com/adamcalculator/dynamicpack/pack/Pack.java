package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.*;
import com.adamcalculator.dynamicpack.Files;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.zip.ZipFile;

public class Pack {
    private final File location;
    private JSONObject cachedJson;
    private long current_build;
    private String current_version;
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
            this.current_version = current.getString("version");

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

    public boolean isUpdateAvailable() throws IOException {
        return remote.checkUpdateAvailable();
    }

    public void sync(SyncProgress progress, boolean manually) throws IOException {
        if (isSyncing) {
            progress.textLog("already syncing...");
            progress.done();
            return;
        }
        if (!isUpdateAvailable() && !manually) {
            progress.textLog("update not available");
            progress.done();
            return;
        }

        isSyncing = true;
        progress.textLog("start syncing...");

        if (remote instanceof ModrinthRemote modrinthRemote) {
            modrinthSync(modrinthRemote, progress);

        } else if (remote instanceof DynamicRepoRemote dynamicRepoRemote) {
            dynamicRepoSync(dynamicRepoRemote, progress);
        } else {
            isSyncing = false;
        }
        progress.done();
    }

    private void dynamicRepoSync(DynamicRepoRemote dynamicRepoRemote, SyncProgress progress) {
        isSyncing = false; // todo
    }

    private void modrinthSync(ModrinthRemote modrinthRemote, SyncProgress progress) throws IOException {
        progress.textLog("getting latest version on modrinth...");
        ModrinthRemote.LatestModrinthVersion latest = modrinthRemote.getLatest();

        progress.textLog("downloading...");
        File file = Urls.downloadFileToTemp(latest.url, "dynamicpack_download", ".zip", new DownloadListener() {
            long total = -1;
            @Override
            public void onStart() {

            }

            @Override
            public void onContentLength(OptionalLong contentLength) {
                if (contentLength.isPresent()) total = contentLength.getAsLong();
            }

            @Override
            public void onProgress(long writtenBytes) {
                progress.downloading(Pack.this.location.getName() + ".zip", writtenBytes, total);
            }

            @Override
            public void onFinish(boolean success) {

            }
        });
        ZipFile zipFile = new ZipFile(file);
        boolean isDynamicPack = zipFile.getEntry(DynamicPackMod.CLIENT_FILE) != null;

        cachedJson.getJSONObject("current").put("version", latest.latestId);
        this.current_version = latest.latestId;

        if (!isDynamicPack) {
            PackUtil.addFileToZip(file, DynamicPackMod.CLIENT_FILE, cachedJson.toString(2));
        }
        if (this.isZip()) {
            Files.moveFile(file, this.location);

        } else {
            Files.deleteDirectory(this.location);
            Files.unzip(file, this.location);
        }


        progress.textLog("done!");
        isSyncing = false;
    }

    public String getCurrentUnique() {
        return current_version;
    }
}
