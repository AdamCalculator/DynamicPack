package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.DynamicPackModBase;
import com.adamcalculator.dynamicpack.Mod;
import com.adamcalculator.dynamicpack.PackUtil;
import com.adamcalculator.dynamicpack.sync.PackSyncProgress;
import com.adamcalculator.dynamicpack.util.AFiles;
import com.adamcalculator.dynamicpack.util.Hashes;
import com.adamcalculator.dynamicpack.util.Out;
import com.adamcalculator.dynamicpack.util.Urls;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class ModrinthRemote extends Remote {
    private Pack parent;

    private String projectId;
    private String gameVersion;


    public ModrinthRemote() {
    }

    public void init(Pack parent, JSONObject json) {
        this.parent = parent;
        this.projectId = json.getString("modrinth_project_id");
        var ver = json.getString("game_version");
        this.gameVersion = ver.equalsIgnoreCase("current") ? getCurrentGameVersion() : ver;
    }

    private String getCurrentGameVersion() {
        return DynamicPackModBase.INSTANCE.getCurrentGameVersion();
    }

    public String getVersionsUrl() {
        return "https://api.modrinth.com/v2/project/" + projectId + "/version";
    }

    public JSONObject parseLatestVersionJson() throws IOException {
        String content = Urls.parseContent(getVersionsUrl(), Mod.MOD_MODTINTH_API_LIMIT);
        JSONArray j = new JSONArray(content);
        for (Object o : j) {
            JSONObject jsonObject = (JSONObject) o;
            JSONArray gameVersions = jsonObject.getJSONArray("game_versions");
            boolean supportGameVersion = false;
            for (Object version : gameVersions) {
                if (gameVersion.equals(version)) {
                    supportGameVersion = true;
                    break;
                }
            }
            if (supportGameVersion) {
                return jsonObject;
            }
        }
        return null;
    }

    @Override
    public boolean checkUpdateAvailable() throws IOException {
        JSONObject latest = parseLatestVersionJson();
        if (latest == null) {
            Out.warn("Latest version of " + parent.getLocation().getName() + " not available for this game_version");
            return false;
        }
        if (latest.optString("version_number", "").equals(parent.getCurrentVersionNumber())) {
            return false;
        }
        return !parent.getCurrentUnique().equals(latest.getString("id"));
    }

    @Override
    public boolean sync(PackSyncProgress progress) throws IOException {
        progress.textLog("getting latest version on modrinth...");
        ModrinthRemote.LatestModrinthVersion latest = getLatest();

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

        parent.cachedJson.getJSONObject("current").put("version", latest.latestId);
        parent.cachedJson.getJSONObject("current").remove("version_number");


        if (!isDynamicPack) {
            PackUtil.addFileToZip(file, DynamicPackModBase.CLIENT_FILE, parent.cachedJson.toString(2));
        }

        if (parent.isZip()) {
            AFiles.moveFile(file, parent.getLocation());

        } else {
            AFiles.deleteDirectory(parent.getLocation());
            AFiles.unzip(file, parent.getLocation());
        }
        progress.textLog("dynamicmcpack.json is updated.");

        progress.textLog("done!");
        return true;
    }

    public LatestModrinthVersion getLatest() throws IOException {
        JSONObject latest = parseLatestVersionJson();
        String latestId = latest.getString("id");
        JSONArray files = latest.getJSONArray("files");
        int i = 0;
        while (i < files.length()) {
            JSONObject j = files.getJSONObject(i);
            if (j.getBoolean("primary")) {
                String url = j.getString("url");
                JSONObject hashes = j.getJSONObject("hashes");
                return new LatestModrinthVersion(latestId, url, hashes.getString("sha1"));
            }
            i++;
        }
        return null;
    }

    public static class LatestModrinthVersion {

        public final String latestId;
        public final String url;
        public final String fileHash;

        public LatestModrinthVersion(String latestId, String url, String fileHash) {
            this.latestId = latestId;
            this.url = url;
            this.fileHash = fileHash;
        }
    }
}
