package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.Mod;
import com.adamcalculator.dynamicpack.util.Out;
import com.adamcalculator.dynamicpack.util.Urls;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class ModrinthRemote extends Remote {
    private final Pack parent;

    private String projectId;
    private String gameVersion;



    public ModrinthRemote(Pack parent, JSONObject json) {
        this.parent = parent;
        this.projectId = json.getString("modrinth_project_id");
        this.gameVersion = json.getString("game_version");
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
