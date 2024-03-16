package com.adamcalculator.dynamicpack;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.UnzipParameters;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class AFiles {
    public static File[] lists(File file) {
        return file.listFiles();
    }

    public static boolean exists(File dynamic) {
        return dynamic.exists();
    }

    public static String read(File file) throws IOException {
        return java.nio.file.Files.readString(file.toPath());
    }

    public static void moveFile(File source, File dest) throws IOException {
        com.google.common.io.Files.move(source, dest);
    }

    public static void unzip(File zipFilePath, File dir) throws IOException {
        UnzipParameters unzipParameters = new UnzipParameters();
        unzipParameters.setExtractSymbolicLinks(false);
        ZipFile zip = new ZipFile(zipFilePath);
        zip.extractAll(dir.getPath(), unzipParameters);
        zip.close();
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static boolean isEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return !entries.findFirst().isPresent();
            }
        }

        return false;
    }
    public static void noEmptyDirDelete(Path toDel) throws IOException {
        Path toDelParent = toDel.getParent();
        Files.deleteIfExists(toDel);
        if (toDelParent != null && isEmpty(toDelParent)) {
            noEmptyDirDelete(toDelParent);
        }
    }

    public static void write(File file, String string) throws IOException {
        FileOutputStream close;
        IOUtils.write(string, (close = new FileOutputStream(file)), StandardCharsets.UTF_8);
        close.close();
    }
}
