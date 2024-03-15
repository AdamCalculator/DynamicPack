package com.adamcalculator.dynamicpack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Hashes {
    public static String calcHashFor(File file) throws IOException, NoSuchAlgorithmException {
        byte[] data = Files.readAllBytes(file.toPath());
        byte[] hash = MessageDigest.getInstance("SHA-1").digest(data);
        return new BigInteger(1, hash).toString(16);
    }

    public static String calcHashInZip(ZipFile zipFile, ZipEntry zipEntry) throws IOException, NoSuchAlgorithmException {
        byte[] data = zipFile.getInputStream(zipEntry).readAllBytes();
        byte[] hash = MessageDigest.getInstance("SHA-1").digest(data);
        return new BigInteger(1, hash).toString(16);
    }

    public static String calcHashForInputStream(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        byte[] data = inputStream.readAllBytes();
        byte[] hash = MessageDigest.getInstance("SHA-1").digest(data);
        return new BigInteger(1, hash).toString(16);
    }
}
