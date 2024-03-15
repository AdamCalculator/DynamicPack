package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class DynamicRepoSyncProcessV1 {
    private final Pack parent;
    private final DynamicRepoRemote remote;
    private final SyncProgress progress;
    private final JSONObject j;

    private final Set<String> oldestFilesList = new HashSet<>();
    private FileSystem zipFileSystem;
    private Path packRootPath;

    public DynamicRepoSyncProcessV1(Pack pack, DynamicRepoRemote dynamicRepoRemote, SyncProgress progress, JSONObject j) throws IOException {
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
        Out.println("scanFiles... files: " + Arrays.toString(oldestFilesList.toArray()));

        List<JSONObject> jsonObjects = calcActiveContents();

        for (JSONObject jsonObject : jsonObjects) {
            Out.println("process activeContent: " + jsonObject);
            processContent(jsonObject);
        }

        for (String s : oldestFilesList) {
            if (s.contains(DynamicPackMod.CLIENT_FILE)) continue;

            System.out.println("File " + s + " deleted.");
            AFiles.noEmptyDirDelete(packRootPath.resolve(s));
        }
    }

    public void close() throws IOException {
        zipFileSystem.close();
    }

    private void processContent(JSONObject object) throws IOException, NoSuchAlgorithmException {
        String url = object.getString("url");
        String urlCompressed = object.optString("urlCompressed", null);
        boolean compressSupported = urlCompressed != null;

        checkPathSafety(url);
        url = remote.getUrl() + "/" + url;

        if (compressSupported) {
            checkPathSafety(urlCompressed);
            urlCompressed = remote.getUrl() + "/" + urlCompressed;
        }

        processContentParsed(new JSONObject(compressSupported ? Urls.parseGZipContent(urlCompressed) : Urls.parseContent(url)));
    }

    private void processContentParsed(JSONObject j) throws IOException, NoSuchAlgorithmException {
        if (j.getLong("formatVersion") != 1) {
            throw new RuntimeException("Incompatible formatVersion");
        }

        JSONObject c = j.getJSONObject("content");
        String par = c.optString("parent", "");
        JSONObject files = c.getJSONObject("files");
        for (String localPath : files.keySet()) {
            String path = getPath(par, localPath);
            String realUrl = getUrlFromPath(path);
            JSONObject fileExtra = files.getJSONObject(localPath);
            String hash = fileExtra.getString("hash");


            Path filePath = packRootPath.resolve(path);
            Out.println(filePath.toString());
            oldestFilesList.remove(filePath.toString());

            boolean isOverwrite = false;
            if (!Files.notExists(filePath)) {
                String localHash = Hashes.calcHashForInputStream(Files.newInputStream(filePath));
                if (!localHash.equals(hash)) {
                    isOverwrite = true;
                }
            } else {
                isOverwrite = true;
            }

            if (isOverwrite) {
                Urls.downloadDynamicFile(realUrl, filePath);
            }

        }
    }

    private String getUrlFromPath(String path) {
        return remote.getUrl() + "/" + path;
    }

    private String getPath(String parent, String path) {
        checkPathSafety(path);
        checkPathSafety(parent);

        if (parent.isEmpty()) {
            return path;
        }
        return parent + "/" + path;
    }

    private void checkPathSafety(String s) {
        if (s.contains("://") || s.contains("..")) {
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
