package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.Urls;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DynamicRepoRemote extends Remote {
    private final Pack parent;

    protected String url;
    protected String buildUrl;
    protected String packUrl;
    protected boolean skipSign; // ! TODO: Add signing functional for safety users

    protected List<DynamicPackContent> contentList = new ArrayList<>();

    public DynamicRepoRemote(Pack pack, JSONObject remote) {
        this.parent = pack;
        this.url = remote.getString("url");
        this.buildUrl = url + "/dynamicmcpack.repo.build"; // todo maybe other?
        this.packUrl = url + "/dynamicmcpack.repo.json";
        this.skipSign = remote.optBoolean("sign_no_required", false);
    }

    @Override
    public boolean checkUpdateAvailable() throws IOException {
        String content = Urls.parseContent(buildUrl);
        return parent.getCurrentBuild() != Long.parseLong(content);
    }
}
