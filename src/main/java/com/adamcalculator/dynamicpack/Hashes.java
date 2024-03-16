package com.adamcalculator.dynamicpack;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Hashes {
    public static String calcHashForFile(File file) throws IOException {
        return DigestUtils.sha1Hex(Files.newInputStream(file.toPath()));
    }

    public static String calcHashForInputStream(InputStream inputStream) throws IOException {
        return DigestUtils.sha1Hex(inputStream);
    }

    public static String calcHashForBytes(byte[] bytes) {
        return DigestUtils.sha1Hex(bytes);
    }
}
