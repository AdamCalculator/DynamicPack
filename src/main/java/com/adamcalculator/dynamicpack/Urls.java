package com.adamcalculator.dynamicpack;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.OptionalLong;

public class Urls {
    public static boolean isFileDebugScheme() {
        return true; // DISABLE IN RELEASE!
    }

    public static String parseContent(String url) throws IOException {
        Out.println("parseContent: " + url);
        if (url.startsWith("file_debug_only://")) {
            if (!isFileDebugScheme()) {
                throw new RuntimeException("Not allowed.");
            }

            File gameDir = FabricLoader.getInstance().getGameDir().toFile();

            return Files.read(new File(gameDir, url.replace("file_debug_only://", "")));


        } else if (url.startsWith("http://")) {
            throw new RuntimeException("HTTP (not secure) not allowed.");


        } else if (url.startsWith("https://")) {
             return IOUtils.toString(URI.create(url), StandardCharsets.UTF_8);
        }

        throw new RuntimeException("Error while parse content...");
    }

    public static File downloadFileToTemp(String url, String prefix, String suffix, DownloadListener listener) throws IOException {
        Out.println("Downloading...");
        File file = File.createTempFile(prefix, suffix);

        listener.onStart();
        URL urlObj = new URL(url);
        URLConnection connection = urlObj.openConnection();
        try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            listener.onContentLength(OptionalLong.of(connection.getContentLength()));
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            long total = 0;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                total += bytesRead;
                listener.onProgress(total);
            }

            listener.onFinish(true);

        } catch (IOException e) {
            Out.e(e);
            listener.onFinish(false);
        }
        return file;
    }
}
