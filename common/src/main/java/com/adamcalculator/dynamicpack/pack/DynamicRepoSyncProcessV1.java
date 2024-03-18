package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.*;
import com.adamcalculator.dynamicpack.sync.PackSyncProgress;
import com.adamcalculator.dynamicpack.sync.state.StateFileDeleted;
import com.adamcalculator.dynamicpack.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class DynamicRepoSyncProcessV1 {
    private final Pack parent;
    private final DynamicRepoRemote remote;
    private final PackSyncProgress progress;
    private final JSONObject j;

    private final Set<String> oldestFilesList = new HashSet<>();
    private FileSystem zipFileSystem;
    private Path packRootPath;

    public DynamicRepoSyncProcessV1(Pack pack, DynamicRepoRemote dynamicRepoRemote, PackSyncProgress progress, JSONObject j) throws IOException {
        this.parent = pack;
        this.remote = dynamicRepoRemote;
        this.progress = progress;
        this.j = j;
        if (parent.isZip()) {
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            URI uri = URI.create("jar:" + parent.getLocation().toPath().toUri());
            zipFileSystem = FileSystems.newFileSystem(uri, env);
            packRootPath = zipFileSystem.getPath("");

        } else {
            packRootPath = parent.getLocation().toPath();
        }
    }

    public void run() throws IOException, NoSuchAlgorithmException {
        PackUtil.walkScan(oldestFilesList, packRootPath);

        List<JSONObject> jsonObjects = calcActiveContents();

        for (JSONObject jsonObject : jsonObjects) {
            Out.println("process activeContent: " + jsonObject);
            processContent(jsonObject);
        }

        for (String s : oldestFilesList) {
            if (s.contains(DynamicPackModBase.CLIENT_FILE)) continue;
            Path path = packRootPath.resolve(s);
            progress.stateChanged(new StateFileDeleted(path));

            progress.textLog("File deleted from resource-pack: " + s);
            AFiles.noEmptyDirDelete(path);
        }
    }

    public void close() throws IOException {
        if (zipFileSystem != null && zipFileSystem.isOpen()) {
            zipFileSystem.close();
        }
    }

    private void processContent(JSONObject object) throws IOException, NoSuchAlgorithmException {
        String id = object.getString("id");
        String contentRemoteHash = object.getString("hash");
        if (!IDValidator.isValid(id)) {
            throw new RuntimeException("Id of content is not valid.");
        }
        String url = object.getString("url");
        String urlCompressed = object.optString("url_compressed", null);
        boolean compressSupported = urlCompressed != null;

        checkPathSafety(url);
        url = remote.getUrl() + "/" + url;

        if (compressSupported) {
            checkPathSafety(urlCompressed);
            urlCompressed = remote.getUrl() + "/" + urlCompressed;
        }

        progress.textLog("process content id:" + id);
        progress.downloading("<content>.json", 0);
        String content = compressSupported ? Urls.parseGZipContent(urlCompressed, Mod.GZIP_LIMIT) : Urls.parseContent(url, Mod.MOD_FILES_LIMIT);
        String receivedHash = Hashes.calcHashForBytes(content.getBytes(StandardCharsets.UTF_8));
        if (!contentRemoteHash.equals(receivedHash)) {
            throw new SecurityException("Hash of content at " + url + " not verified. remote: " + contentRemoteHash + "; received: " + receivedHash);
        }
        progress.downloading("<content>.json", 100);
        processContentParsed(new JSONObject(content));
    }

    private void processContentParsed(JSONObject j) throws IOException {
        if (j.getLong("formatVersion") != 1) {
            throw new RuntimeException("Incompatible formatVersion");
        }

        JSONObject c = j.getJSONObject("content");
        String par = c.optString("parent", "");
        JSONObject files = c.getJSONObject("files");
        int processedFiles = 0;
        for (String localPath : files.keySet()) {
            String path = getPath(par, localPath);
            String realUrl = getUrlFromPath(path);
            JSONObject fileExtra = files.getJSONObject(localPath);
            String hash = fileExtra.getString("hash");


            Path filePath = packRootPath.resolve(path);
            oldestFilesList.remove(filePath.toString());

            boolean isOverwrite = false;
            if (!Files.notExists(filePath)) {
                String localHash = Hashes.calcHashForInputStream(Files.newInputStream(filePath));
                if (!localHash.equals(hash)) {
                    isOverwrite = true;
                    this.progress.textLog("hash not equal: local:" + localHash+ " remote:"+hash);
                }
            } else {
                this.progress.textLog("Not exists: " + filePath);
                isOverwrite = true;
            }

            if (isOverwrite) {
                if (filePath.getFileName().toString().contains(DynamicPackModBase.CLIENT_FILE)) continue;
                this.progress.textLog("(over)write file: " + filePath);
                Urls.downloadDynamicFile(realUrl, filePath, hash, new FileDownloadConsumer() {
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

    private String getUrlFromPath(String path) {
        return remote.getUrl() + "/" + path;
    }

    public static String getPath(String parent, String path) {
        checkPathSafety(path);
        checkPathSafety(parent);

        if (parent.isEmpty()) {
            return path;
        }
        return parent + "/" + path;
    }

    public static void checkPathSafety(String s) {
        if (s.contains("://") || s.contains("..") || s.contains("  ") || s.contains(".exe")) {
            throw new SecurityException("This url not supported redirects to other servers or jump-up!");
        }
    }

    private List<JSONObject> calcActiveContents() {
        List<JSONObject> activeContents = new ArrayList<>();
        JSONArray contents = j.getJSONArray("contents");
        int i = 0;
        while (i < contents.length()) {
            JSONObject content = contents.getJSONObject(i);
            String id = content.getString("id");
            if (parent.isContentActive(id)) {
                activeContents.add(content);
            }
            i++;
        }
        
        return activeContents;
    }
}
