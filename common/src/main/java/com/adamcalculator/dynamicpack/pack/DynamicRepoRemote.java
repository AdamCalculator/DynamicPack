package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.DynamicPackModBase;
import com.adamcalculator.dynamicpack.Mod;
import com.adamcalculator.dynamicpack.PackUtil;
import com.adamcalculator.dynamicpack.sync.PackSyncProgress;
import com.adamcalculator.dynamicpack.util.AFiles;
import com.adamcalculator.dynamicpack.util.Out;
import com.adamcalculator.dynamicpack.util.Urls;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

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


        if (skipSign != this.publicKey.isBlank()) {
            throw new RuntimeException("Incompatible parameters set. Select one of: sign_no_required or public_key");
        }
    }

    @Override
    public boolean checkUpdateAvailable() throws IOException {
        String content = Urls.parseContent(buildUrl, 64).trim();
        return parent.getCurrentBuild() != Long.parseLong(content);
    }

    @Override
    public boolean sync(PackSyncProgress progress) throws IOException, NoSuchAlgorithmException {
        String packUrlContent;
        progress.downloading("dynamicmcpack.repo.json", 0);
        if (skipSign) {
            packUrlContent = Urls.parseContent(packUrl, Mod.MOD_FILES_LIMIT);
            Out.warn("Dynamic pack " + parent.getLocation().getName() + " is skipping signing.");
            progress.textLog("File parsed, verify skipped.");

        } else {
            packUrlContent = Urls.parseContentAndVerify(packSigUrl, packUrl, publicKey, Mod.MOD_FILES_LIMIT);
            progress.textLog("Success parse and verify file.");
        }
        progress.downloading("dynamicmcpack.repo.json", 100);

        JSONObject j = new JSONObject(packUrlContent);
        if (j.getLong("formatVersion") != 1) {
            throw new RuntimeException("Incompatible formatVersion!");
        }


        DynamicRepoSyncProcessV1 dynamicRepoSyncProcessV1 = new DynamicRepoSyncProcessV1(parent, this, progress, j);
        try {
            dynamicRepoSyncProcessV1.run();
            dynamicRepoSyncProcessV1.close();

        } catch (Exception e) {
            dynamicRepoSyncProcessV1.close();
            throw e;
        }

        parent.current_build = j.getLong("build");
        parent.cachedJson.getJSONObject("current").put("build", parent.current_build);

        if (parent.isZip()) {
            PackUtil.addFileToZip(parent.getLocation(), DynamicPackModBase.CLIENT_FILE, parent.cachedJson.toString(2));
        } else {
            AFiles.write(new File(parent.getLocation(), DynamicPackModBase.CLIENT_FILE), parent.cachedJson.toString(2));
        }
        return true;
    }

    public String getUrl() {
        return url;
    }
}
