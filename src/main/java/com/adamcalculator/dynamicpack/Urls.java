package com.adamcalculator.dynamicpack;

import  com.adamcalculator.dynamicpack.enc.GPGDetachedSignatureVerifier;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.LongConsumer;
import java.util.zip.GZIPInputStream;

public class Urls {
    public static boolean isFileDebugScheme() {
        return !Mod.isRelease();
    }

    public static String parseContentAndVerify(String signatureUrl, String url, String publicKeyBase64, long maxLimit) throws IOException {
        boolean isVerified = GPGDetachedSignatureVerifier
                .verify(_getInputStreamOfUrl(url, maxLimit),
                        _getInputStreamOfUrl(signatureUrl, maxLimit),
                        publicKeyBase64);

        if (!isVerified) {
            throw new SecurityException("Failed to verify " + url + " using signature at " + signatureUrl + " and publicKey: " + publicKeyBase64);
        }
        return _parseContentFromStream(_getInputStreamOfUrl(url, maxLimit));
    }

    /**
     * Parse text content from url
     * @param url url
     */
    public static String parseContent(String url, long limit) throws IOException {
        return _parseContentFromStream(_getInputStreamOfUrl(url, limit));
    }


    /**
     * Parse GZip compressed content from url
     * @param url url
     */
    public static String parseGZipContent(String url, long limit) throws IOException {
        return _parseContentFromStream(new GZIPInputStream(_getInputStreamOfUrl(url, limit)));
    }


    /**
     * Create temp zipFile and download to it from url.
     */
    public static File downloadFileToTemp(String url, String prefix, String suffix, long limit, LongConsumer progress) throws IOException {
        File file = File.createTempFile(prefix, suffix);

        InputStream inputStream = _getInputStreamOfUrl(url, limit, progress);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        _transferStreams(inputStream, fileOutputStream, progress);

        return file;
    }



    public static void downloadDynamicFile(String url, Path path, String hash, LongConsumer progress) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(path);
        }

        if (Files.exists(path)) {
            Files.delete(path);
        }
        Files.createFile(path);

        try {
            _transferStreamsWithHash(hash, _getInputStreamOfUrl(url, Mod.DYNAMIC_PACK_HTTPS_FILE_SIZE_LIMIT, progress), Files.newOutputStream(path), progress);
        } catch (Exception e) {
            throw new RuntimeException("File " + path + " download error. From url: " + url + ". Expected hash: " + hash, e);
        }
    }



    private static InputStream _getInputStreamOfUrl(String url, long sizeLimit) throws IOException {
        return _getInputStreamOfUrl(url, sizeLimit, null);
    }

    private static InputStream _getInputStreamOfUrl(String url, long sizeLimit, @Nullable LongConsumer progress) throws IOException {
        if (url.startsWith("file_debug_only://")) {
            if (!isFileDebugScheme()) {
                throw new RuntimeException("Not allowed scheme.");
            }

            final File gameDir = FabricLoader.getInstance().getGameDir().toFile();
            File file = new File(gameDir, url.replace("file_debug_only://", ""));
            if (progress != null){
                progress.accept(file.length());
            }
            return new FileInputStream(file);


        } else if (url.startsWith("http://")) {
            throw new RuntimeException("HTTP (not secure) not allowed scheme.");


        } else if (url.startsWith("https://")) {
            if (!Mod.isUrlHostTrusted(url)) {
                if (Mod.isBlockAllNotTrustedNetworks()) {
                    throw new SecurityException("Url host is not trusted!");
                }
            }

            if (url.contains(" ")) {
                Out.LOGGER.warn("URL " + url + " contains not encoded spaced! Use %20 for space symbol in links!");
            }
            URL urlObj = new URL(url);
            URLConnection connection = urlObj.openConnection();
            long length = connection.getContentLengthLong();
            if (length > sizeLimit) {
                throw new RuntimeException("File at " + url+ " so bigger. " + length + " > " + sizeLimit);
            }
            if (progress != null){
                progress.accept(length);
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

    private static void _transferStreams(InputStream inputStream, OutputStream outputStream, @Nullable LongConsumer progress) throws IOException {
        BufferedInputStream in = new BufferedInputStream(inputStream);

        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        long total = 0;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            outputStream.write(dataBuffer, 0, bytesRead);
            total += bytesRead;
            if (progress != null) {
                progress.accept(total);
            }
        }
        outputStream.flush();
        outputStream.close();
        in.close();
    }

    private static void _transferStreamsWithHash(String hash, InputStream inputStream, OutputStream outputStream, LongConsumer progress) throws IOException {
        BufferedInputStream in = new BufferedInputStream(inputStream);

        ByteArrayOutputStream checkStream = new ByteArrayOutputStream();

        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        long total = 0;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            checkStream.write(dataBuffer, 0, bytesRead);
            total += bytesRead;
            progress.accept(total);
        }
        checkStream.flush();
        in.close();

        String hashOfDownloaded = Hashes.calcHashForBytes(checkStream.toByteArray());
        if (hashOfDownloaded.equals(hash)) {
            _transferStreams(new ByteArrayInputStream(checkStream.toByteArray()), outputStream, null);
            return;
        }

        throw new SecurityException("Hash of pre-downloaded file not equal: remote: " + hash + "; real: " + hashOfDownloaded);
    }
}
