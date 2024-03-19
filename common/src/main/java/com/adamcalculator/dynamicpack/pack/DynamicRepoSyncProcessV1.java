package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.DynamicPackModBase;
import com.adamcalculator.dynamicpack.IDValidator;
import com.adamcalculator.dynamicpack.Mod;
import com.adamcalculator.dynamicpack.PackUtil;
import com.adamcalculator.dynamicpack.sync.PackSyncProgress;
import com.adamcalculator.dynamicpack.sync.state.StateFileDeleted;
import com.adamcalculator.dynamicpack.util.AFiles;
import com.adamcalculator.dynamicpack.util.FileDownloadConsumer;
import com.adamcalculator.dynamicpack.util.Hashes;
import com.adamcalculator.dynamicpack.util.Urls;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DynamicRepoSyncProcessV1 {
    private final Pack parent;
    private final DynamicRepoRemote remote;
    private final PackSyncProgress progress;
    private final JSONObject repoJson;

    private final Set<String> oldestFilesList = new HashSet<>();
    private final Path packRootPath;

    public DynamicRepoSyncProcessV1(Pack pack, DynamicRepoRemote dynamicRepoRemote, PackSyncProgress progress, JSONObject repoJson, Path path) {
        this.parent = pack;
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
            if (s.contains(DynamicPackModBase.CLIENT_FILE)) continue;
            Path path = packRootPath.resolve(s);
            progress.stateChanged(new StateFileDeleted(path));

            progress.textLog("File deleted from resource-pack: " + s);
            AFiles.nioSmartDelete(path);
        }
    }

    public void close() throws IOException {
    }

    private void processContent(JSONObject jsonContent) throws IOException {
        String id = jsonContent.getString("id");
        if (!IDValidator.isValid(id)) {
            throw new RuntimeException("Id of content is not valid.");
        }

        String contentRemoteHash = jsonContent.getString("hash");
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

        int processedFiles = 0;
        for (final String _relativePath : files.keySet()) {
            var path = getAndCheckPath(par, _relativePath); // parent / path.     assets/minecraft
            var filePath = packRootPath.resolve(path);
            var fileRemoteUrl = getUrlFromPath(rem, path);

            JSONObject fileExtra = files.getJSONObject(_relativePath);
            String hash = fileExtra.getString("hash");

            // remove from unused list
            oldestFilesList.remove(filePath.toString());

            boolean isOverwrite = false;
            if (Files.exists(filePath)) {
                String localHash = Hashes.nioCalcHashForPath(filePath);
                if (!localHash.equals(hash)) {
                    isOverwrite = true;
                    this.progress.textLog(filePath + ": overwrite! hash not equal: local:" + localHash+ " remote:"+hash);
                }
            } else {
                this.progress.textLog("Overwrite! Not exists: " + filePath);
                isOverwrite = true;
            }

            if (isOverwrite) {
                if (filePath.getFileName().toString().contains(DynamicPackModBase.CLIENT_FILE)) {
                    continue;
                }

                this.progress.textLog("Overwriting: " + filePath);
                Urls.downloadDynamicFile(fileRemoteUrl, filePath, hash, new FileDownloadConsumer() {
                    @Override
                    public void onUpdate(FileDownloadConsumer it) {
                        progress.downloading(filePath.getFileName().toString(), it.getPercentage());
                    }
                });
            }

            processedFiles++;
        }
        this.progress.textLog("Files processed in this content: " + processedFiles);
    }

    private String getUrlFromPath(String remoteParent, String path) {
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
            if (remote.isContentActive(id) || content.optBoolean("required", false)) {
                activeContents.add(content);
            }
            i++;
        }
        
        return activeContents;
    }
}
