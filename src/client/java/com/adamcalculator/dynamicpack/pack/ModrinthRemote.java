package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.PackUtil;
import com.adamcalculator.dynamicpack.Urls;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class ModrinthRemote extends Remote {
    private final Pack parent;

    private String projectId;



    public ModrinthRemote(Pack parent, JSONObject json) {
        this.parent = parent;
        this.projectId = json.getString("modrinth_project_id");
    }

    public String getVersionsUrl() {
        return "https://api.modrinth.com/v2/project/" + projectId + "/version";
    }

    public JSONObject parseLatestVersionJson() throws IOException {
        String content = Urls.parseContent(getVersionsUrl());
        JSONArray j = new JSONArray(content);
        return j.getJSONObject(0);
    }

    @Override
    public boolean checkUpdateAvailable() throws IOException {
        JSONObject latest = parseLatestVersionJson();
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
                return new LatestModrinthVersion(latestId, url);
            }
            i++;
        }
        return null;
    }

    public static class LatestModrinthVersion {

        public final String latestId;
        public final String url;

        public LatestModrinthVersion(String latestId, String url) {
            this.latestId = latestId;

            this.url = url;
        }
    }
}
