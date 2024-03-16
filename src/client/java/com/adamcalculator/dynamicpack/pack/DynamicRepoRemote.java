package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.Urls;
import org.json.JSONObject;

import java.io.IOException;

public class DynamicRepoRemote extends Remote {
    private final Pack parent;

    protected String url;
    protected String buildUrl;
    protected String packUrl;
    protected String packSigUrl;
    public String publicKey;
    protected boolean skipSign;

    public DynamicRepoRemote(Pack pack, JSONObject remote) {
        this.parent = pack;
        this.url = remote.getString("url");
        this.buildUrl = url + "/dynamicmcpack.repo.build";
        this.packUrl = url + "/dynamicmcpack.repo.json";
        this.packSigUrl = url + "/dynamicmcpack.repo.json.sig";
        this.publicKey = remote.optString("public_key", "").replace("\n", "").trim();
        this.skipSign = remote.optBoolean("sign_no_required", false);
    }

    @Override
    public boolean checkUpdateAvailable() throws IOException {
        String content = Urls.parseContent(buildUrl, 64).trim();
        return parent.getCurrentBuild() != Long.parseLong(content);
    }

    public String getUrl() {
        return url;
    }
}
