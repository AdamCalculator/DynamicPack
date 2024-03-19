package com.adamcalculator.dynamicpack.util;

import com.adamcalculator.dynamicpack.DynamicPackModBase;
import com.adamcalculator.dynamicpack.Mod;
import com.adamcalculator.dynamicpack.util.enc.GPGSignatureVerifier;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.LongConsumer;
import java.util.zip.GZIPInputStream;

public class Urls {
    public static boolean isFileDebugSchemeAllowed() {
        return Mod.isFileDebugSchemeAllowed();
    }

    public static boolean isHTTPTrafficAllowed() {
        return Mod.isHTTPTrafficAllowed();
    }

    /**
     * parse content and verify it
     * @param signatureUrl signature binary url
     * @param url url of source file
     * @param publicKeyBase64 base64 public key
     * @param maxLimit max limit of download
     * @param progress progress
     * @return parsed content or throw error
     * @throws IOException IOException
     * @throws SecurityException is signature not valid
     */
    public static String parseContentAndVerify(String signatureUrl, String url, String publicKeyBase64, long maxLimit, LongConsumer progress) throws IOException {
        if (!(url + ".sig").equalsIgnoreCase(signatureUrl)) {
            throw new SecurityException("The signature and the file being checked are in different locations!");
        }

        InputStream inputStream = _getInputStreamOfUrl(url, maxLimit, progress);

        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        _transferStreams(inputStream, temp, progress);

        boolean isVerified = GPGSignatureVerifier
                .verify(new ByteArrayInputStream(temp.toByteArray()),
                        _getInputStreamOfUrl(signatureUrl, maxLimit, null),
                        publicKeyBase64);

        if (!isVerified) {
            throw new SecurityException("Failed to verify " + url + " using signature at " + signatureUrl + " and publicKey: " + publicKeyBase64);
        }
        Out.println("Urls.parseContentAndVerify success verified at " + url);
        return _parseContentFromStream(new ByteArrayInputStream(temp.toByteArray()), maxLimit, null);
    }

    /**
     * Parse text content from url with no progress
     * @param url url
     */
    public static String parseContent(String url, long limit) throws IOException {
        return parseContent(url, limit, null);
    }

    /**
     * Parse text content from url
     * @param url url
     * @param progress progress
     */
    public static String parseContent(String url, long limit, LongConsumer progress) throws IOException {
        return _parseContentFromStream(_getInputStreamOfUrl(url, limit, progress), limit, progress);
    }


    /**
     * Parse GZip compressed content from url
     * @param url url
     */
    public static String parseGZipContent(String url, long limit, LongConsumer progress) throws IOException {
        return _parseContentFromStream(new GZIPInputStream(_getInputStreamOfUrl(url, limit, progress)), limit, progress);
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
        final int maxI = 3;
        int i = maxI;
        while (i > 0) {
            try {
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
                break;
            } catch (Exception e) {
                Out.error("downloadDynamicFile. Attempt=" + (maxI - i + 1) + "/" + maxI, e);
            }

            i--;
        }
    }


    private static InputStream _getInputStreamOfUrl(String url, long sizeLimit, /*@Nullable*/ LongConsumer progress) throws IOException {
        if (url.contains(" ")) {
            throw new IOException("URL can't contains spaces!");
        }


        if (url.startsWith("file_debug_only://")) {
            if (!isFileDebugSchemeAllowed()) {
                throw new RuntimeException("Not allowed scheme.");
            }

            final File gameDir = DynamicPackModBase.INSTANCE.getGameDir();
            File file = new File(gameDir, url.replace("file_debug_only://", ""));
            if (progress != null){
                progress.accept(file.length());
            }
            return new FileInputStream(file);


        } else if (url.startsWith("http://")) {
            if (!isHTTPTrafficAllowed()) {
                throw new RuntimeException("HTTP (not secure) not allowed scheme.");
            }

            if (!Mod.isUrlHostTrusted(url)) {
                if (Mod.isBlockAllNotTrustedNetworks()) {
                    throw new SecurityException("Url host is not trusted!");
                }
            }

            URL urlObj = new URL(url);
            URLConnection connection = urlObj.openConnection();
            long length = connection.getContentLengthLong();
            if (length > sizeLimit) {
                throw new RuntimeException("[HTTP] File at " + url+ " so bigger. " + length + " > " + sizeLimit);
            }
            if (progress != null){
                progress.accept(length);
            }
            return connection.getInputStream();


        } else if (url.startsWith("https://")) {
            if (!Mod.isUrlHostTrusted(url)) {
                if (Mod.isBlockAllNotTrustedNetworks()) {
                    throw new SecurityException("Url host is not trusted!");
                }
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

    private static String _parseContentFromStream(InputStream stream, long maxLimit, /* Nullable */ LongConsumer progress) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        long total = 0;
        while ((bytesRead = stream.read(dataBuffer, 0, 1024)) != -1) {
            byteArrayOutputStream.write(dataBuffer, 0, bytesRead);
            total += bytesRead;

            if (total > maxLimit) {
                throw new SecurityException("Download limit! " + total + " > " + maxLimit);
            }

            if (progress != null) {
                progress.accept(total);
            }

            Mod.debugNetwork();
        }
        String s = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        byteArrayOutputStream.close();
        stream.close();
        return s;
    }

    private static void _transferStreams(InputStream inputStream, OutputStream outputStream, /*@Nullable*/ LongConsumer progress) throws IOException {
        BufferedInputStream in = new BufferedInputStream(inputStream);
        boolean isNetwork = !(inputStream instanceof ByteArrayInputStream);
        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        long total = 0;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            outputStream.write(dataBuffer, 0, bytesRead);
            total += bytesRead;
            if (progress != null) {
                progress.accept(total);
            }


            if (isNetwork) Mod.debugNetwork();
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
            Mod.debugNetwork();
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
