package com.adamcalculator.dynamicpack;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Files {
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

    public static void unzip(File zipFilePath, File dir) throws ZipException {
        UnzipParameters unzipParameters = new UnzipParameters();
        unzipParameters.setExtractSymbolicLinks(false);
        new ZipFile(zipFilePath).extractAll(dir.getPath(), unzipParameters);
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
}
