package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.DynamicPackModBase;
import com.adamcalculator.dynamicpack.Mod;
import com.adamcalculator.dynamicpack.PackUtil;
import com.adamcalculator.dynamicpack.sync.PackSyncProgress;
import com.adamcalculator.dynamicpack.util.AFiles;
import com.adamcalculator.dynamicpack.util.Out;
import com.adamcalculator.dynamicpack.util.Urls;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public class DynamicRepoRemote extends Remote {
    public static final String REPO_JSON = "dynamicmcpack.repo.json";
    public static final String REPO_BUILD = "dynamicmcpack.repo.build";
    public static final String REPO_SIGNATURE = "dynamicmcpack.repo.json.sig";


    private Pack parent;

    protected String url;
    protected String buildUrl;
    protected String packUrl;
    protected String packSigUrl;
    public String publicKey;
    protected boolean skipSign;

    public DynamicRepoRemote() {
    }

    public void init(Pack pack, JSONObject remote) {
        this.parent = pack;
        this.url = remote.getString("url");
        this.buildUrl = url + "/" + REPO_BUILD;
        this.packUrl = url + "/" + REPO_JSON;
        this.packSigUrl = url + "/" + REPO_SIGNATURE;
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
        PackUtil.openPackFileSystem(parent.getLocation(), path -> {
            try {
                sync0(progress, path);

            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
        return true;
    }

    public void sync0(PackSyncProgress progress, Path path) throws IOException, NoSuchAlgorithmException {
        String packUrlContent;
        progress.downloading(REPO_JSON, 0);
        if (skipSign) {
            packUrlContent = Urls.parseContent(packUrl, Mod.MOD_FILES_LIMIT);
            Out.warn("Dynamic pack " + parent.getName() + " is skipping signing.");
            progress.textLog("File parsed, verify skipped.");

        } else {
            packUrlContent = Urls.parseContentAndVerify(packSigUrl, packUrl, publicKey, Mod.MOD_FILES_LIMIT);
            progress.textLog("Success parse and verify file.");
        }
        progress.downloading(REPO_JSON, 100);

        JSONObject repoJson = new JSONObject(packUrlContent);
        long formatVersion;
        if ((formatVersion = repoJson.getLong("formatVersion")) != 1) {
            throw new RuntimeException("Incompatible formatVersion: " + formatVersion);
        }


        DynamicRepoSyncProcessV1 dynamicRepoSyncProcessV1 = new DynamicRepoSyncProcessV1(parent, this, progress, repoJson, path);
        try {
            dynamicRepoSyncProcessV1.run();
            dynamicRepoSyncProcessV1.close();

        } catch (Exception e) {
            dynamicRepoSyncProcessV1.close();
            throw e;
        }
        parent.cachedJson.getJSONObject("current").put("build", repoJson.getLong("build"));

        AFiles.nioWriteText(path.resolve(DynamicPackModBase.CLIENT_FILE), parent.cachedJson.toString(2));
    }

    public String getUrl() {
        return url;
    }
}
