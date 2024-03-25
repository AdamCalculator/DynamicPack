package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.DynamicPackMod;
import com.adamcalculator.dynamicpack.InputValidator;
import com.adamcalculator.dynamicpack.Mod;
import com.adamcalculator.dynamicpack.PackUtil;
import com.adamcalculator.dynamicpack.sync.PackSyncProgress;
import com.adamcalculator.dynamicpack.sync.state.StateFileDeleted;
import com.adamcalculator.dynamicpack.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DynamicRepoSyncProcessV1 {
    private final DynamicRepoRemote remote;
    private final PackSyncProgress progress;
    private final JSONObject repoJson;

    private final Set<String> oldestFilesList = new HashSet<>();
    private final Path packRootPath;
    private boolean isReloadRequired = false;

    public DynamicRepoSyncProcessV1(Pack pack, DynamicRepoRemote dynamicRepoRemote, PackSyncProgress progress, JSONObject repoJson, Path path) {
        this.remote = dynamicRepoRemote;
        this.progress = progress;
        this.repoJson = repoJson;
        this.packRootPath = path;
    }

    public void run() throws IOException {
        PackUtil.walkScan(oldestFilesList, packRootPath);

        List<JSONObject> activeContents = calcActiveContents();

        for (JSONObject jsonContent : activeContents) {
            processContent(jsonContent);
        }

        for (String s : oldestFilesList) {
            if (s.contains(DynamicPackMod.CLIENT_FILE)) continue;
            Path path = packRootPath.resolve(s);
            String logPath;
            if (remote.parent.isZip()) {
                logPath = path.toString();
            } else {
                logPath = remote.parent.getLocation().toPath().relativize(path).toString();
            }
            progress.stateChanged(new StateFileDeleted(path));

            progress.textLog("File deleted from resource-pack: " + logPath);
            AFiles.nioSmartDelete(path);
            markReloadRequired();
        }

        try {
            remote.updateCurrentKnownContents(repoJson.getJSONArray("contents"));
        } catch (Exception e) {
            Out.error("Error while update known_packs. Not fatal", e);
        }
    }

    public void close() throws IOException {
    }

    private void processContent(JSONObject jsonContent) throws IOException {
        String id = jsonContent.getString("id");
        if (!InputValidator.isContentIdValid(id)) {
            throw new RuntimeException("Id of content is not valid.");
        }

        String contentRemoteHash = jsonContent.getString("hash");
        String localCache = remote.getCurrentPackContentHash(id);
        if (Objects.equals(localCache, contentRemoteHash)) {
            progress.textLog("Content '" + id + "' local hash is equal with remote...");
        } else {
            progress.textLog("Content '" + id + "' local hash different with remote or null.");
        }

        String url = jsonContent.getString("url");
        String urlCompressed = jsonContent.optString("url_compressed", null);
        boolean compressSupported = urlCompressed != null;

        checkPathSafety(url);
        url = remote.getUrl() + "/" + url;

        if (compressSupported) {
            checkPathSafety(urlCompressed);
            urlCompressed = remote.getUrl() + "/" + urlCompressed;
        }

        progress.textLog("process content id:" + id);
        var contentDownloadProgress = new FileDownloadConsumer() {
            @Override
            public void onUpdate(FileDownloadConsumer it) {
                progress.downloading("<content:"+ id +">.json", it.getPercentage());
            }
        };
        String content = compressSupported ? Urls.parseGZipContent(urlCompressed, Mod.GZIP_LIMIT, contentDownloadProgress) : Urls.parseContent(url, Mod.MOD_FILES_LIMIT, contentDownloadProgress);
        String receivedHash = Hashes.calcHashForBytes(content.getBytes(StandardCharsets.UTF_8));
        if (!contentRemoteHash.equals(receivedHash)) {
            throw new SecurityException("Hash of content at " + url + " not verified. remote: " + contentRemoteHash + "; received: " + receivedHash);
        }
        processContentParsed(new JSONObject(content));
    }

    private void processContentParsed(JSONObject jsonContent) throws IOException {
        long formatVersion;
        if ((formatVersion = jsonContent.getLong("formatVersion")) != 1) {
            throw new RuntimeException("Incompatible formatVersion: " + formatVersion);
        }

        JSONObject c = jsonContent.getJSONObject("content");
        String par = c.optString("parent", "");
        String rem = c.optString("remote_parent", "");
        JSONObject files = c.getJSONObject("files");

        InputValidator.validOrThrownPath(par);
        InputValidator.validOrThrownPath(rem);

        int processedFiles = 0;
        for (final String _relativePath : files.keySet()) {
            try {
                var path = getAndCheckPath(par, _relativePath); // parent / path.     assets/minecraft
                InputValidator.validOrThrownPath(path);

                var filePath = packRootPath.resolve(path);
                Path filePathForLogs;
                if (remote.parent.isZip()) {
                    filePathForLogs = filePath;
                } else {
                    filePathForLogs = remote.parent.getLocation().toPath().relativize(filePath);
                }
                var fileRemoteUrl = getUrlFromPathAndCheck(rem, path);

                JSONObject fileExtra = files.getJSONObject(_relativePath);
                String hash = fileExtra.getString("hash");
                if (!InputValidator.isHashValid(hash)) {
                    progress.textLog("Skipping file because hash not valid");
                    continue;
                }

                // remove from unused list
                oldestFilesList.remove(filePath.toString());

                boolean isOverwrite = false;
                if (Files.exists(filePath)) {
                    String localHash = Hashes.nioCalcHashForPath(filePath);
                    if (!localHash.equals(hash)) {
                        isOverwrite = true;
                        this.progress.textLog(filePathForLogs + ": overwrite! hash not equal: local:" + localHash + " remote:" + hash);
                    }
                } else {
                    this.progress.textLog("Overwrite! Not exists: " + filePathForLogs);
                    isOverwrite = true;
                }

                if (isOverwrite) {
                    if (filePath.getFileName().toString().contains(DynamicPackMod.CLIENT_FILE)) {
                        continue;
                    }

                    markReloadRequired();
                    this.progress.textLog("Overwriting: " + filePathForLogs);
                    Urls.downloadDynamicFile(fileRemoteUrl, filePath, hash, new FileDownloadConsumer() {
                        @Override
                        public void onUpdate(FileDownloadConsumer it) {
                            progress.downloading(filePath.getFileName().toString(), it.getPercentage());
                        }
                    });
                }

                processedFiles++;
            } catch (Exception e) {
                progress.textLog("Error " + e);
                Out.error("Error while process file in pack...", e);
            }
        }
        this.progress.textLog("Files processed in this content: " + processedFiles);
    }

    private String getUrlFromPathAndCheck(String remoteParent, String path) {
        checkPathSafety(remoteParent);

        if (remoteParent.isEmpty()) {
            return remote.getUrl() + "/" + path;
        }

        return remote.getUrl() + "/" + remoteParent + "/" + path;
    }

    public static String getAndCheckPath(String parent, String path) {
        checkPathSafety(path);
        checkPathSafety(parent);

        if (parent.isEmpty()) {
            return path;
        }
        return parent + "/" + path;
    }

    public static void checkPathSafety(String s) {
        if (s.contains("://") || s.contains("..") || s.contains("  ") || s.contains(".exe") || s.contains(":") || s.contains(".jar")) {
            throw new SecurityException("This url not safe: " + s);
        }
    }

    private List<JSONObject> calcActiveContents() {
        List<JSONObject> activeContents = new ArrayList<>();
        JSONArray contents = repoJson.getJSONArray("contents");
        int i = 0;
        while (i < contents.length()) {
            JSONObject content = contents.getJSONObject(i);
            String id = content.getString("id");
            boolean defaultActive = content.optBoolean("default_active", true);

            if (!InputValidator.isContentIdValid(id)) {
                throw new RuntimeException("Id of content is not valid.");
            }

            if (remote.isContentActive(id, defaultActive) || content.optBoolean("required", false)) {
                activeContents.add(content);
            }
            i++;
        }
        return activeContents;
    }

    public boolean isReloadRequired() {
        return isReloadRequired;
    }

    private void markReloadRequired() {
        if (!isReloadRequired) {
            Out.debug("Now reload is required in " + this);
        }
        this.isReloadRequired = true;
    }
}
