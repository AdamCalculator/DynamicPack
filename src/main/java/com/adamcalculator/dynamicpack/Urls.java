package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.enc.GPGDetachedSignatureVerifier;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class Urls {
    public static boolean isFileDebugScheme() {
        return !Mod.isRelease();
    }

    public static String parseContentAndVerify(String signatureUrl, String url, String publicKeyBase64) throws IOException {
        boolean isVerified = GPGDetachedSignatureVerifier
                .verify(_getInputStreamOfUrl(url),
                        _getInputStreamOfUrl(signatureUrl),
                        publicKeyBase64);

        if (!isVerified) {
            throw new SecurityException("Failed to verify " + url + " using signature at " + signatureUrl + " and publicKey: " + publicKeyBase64);
        }
        return _parseContentFromStream(_getInputStreamOfUrl(url));
    }

    /**
     * Parse text content from url
     * @param url url
     */
    public static String parseContent(String url) throws IOException {
        return _parseContentFromStream(_getInputStreamOfUrl(url));
    }


    /**
     * Parse GZip compressed content from url
     * @param url url
     */
    public static String parseGZipContent(String url) throws IOException {
        return _parseContentFromStream(new GZIPInputStream(_getInputStreamOfUrl(url)));
    }


    /**
     * Create temp zipFile and download to it from url.
     */
    public static File downloadFileToTemp(String url, String prefix, String suffix) throws IOException {
        File file = File.createTempFile(prefix, suffix);

        InputStream inputStream = _getInputStreamOfUrl(url);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        _transferStreams(inputStream, fileOutputStream);

        return file;
    }



    public static void downloadDynamicFile(String url, Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(path);
        }

        if (Files.exists(path)) {
            Files.delete(path);
        }
        Files.createFile(path);

        _transferStreams(_getInputStreamOfUrl(url), Files.newOutputStream(path));
    }



    private static InputStream _getInputStreamOfUrl(String url) throws IOException {
        if (url.startsWith("file_debug_only://")) {
            if (!isFileDebugScheme()) {
                throw new RuntimeException("Not allowed scheme.");
            }

            final File gameDir = FabricLoader.getInstance().getGameDir().toFile();
            return new FileInputStream(new File(gameDir, url.replace("file_debug_only://", "")));


        } else if (url.startsWith("http://")) {
            throw new RuntimeException("HTTP (not secure) not allowed scheme.");


        } else if (url.startsWith("https://")) {
            URL urlObj = new URL(url);
            URLConnection connection = urlObj.openConnection();
            if (connection.getContentLengthLong() > Mod.HTTPS_FILE_SIZE_LIMIT) {
                throw new RuntimeException("File at " + url+ " so bigger. l: " + connection.getContentLengthLong());
            }
            return connection.getInputStream();

        } else {
            throw new RuntimeException("Unknown scheme.");
        }
    }

    private static String _parseContentFromStream(InputStream stream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = stream.read(dataBuffer, 0, 1024)) != -1) {
            byteArrayOutputStream.write(dataBuffer, 0, bytesRead);
        }
        byteArrayOutputStream.flush();
        String s = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        byteArrayOutputStream.close();
        stream.close();
        return s;
    }

    private static void _transferStreams(InputStream inputStream, OutputStream outputStream) throws IOException {
        BufferedInputStream in = new BufferedInputStream(inputStream);

        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            outputStream.write(dataBuffer, 0, bytesRead);
        }
        outputStream.flush();
        outputStream.close();
        in.close();
    }
}
