package com.adamcalculator.dynamicpack.util;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.UnzipParameters;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

public class AFiles {
    public static File[] lists(File file) {
        return file.listFiles();
    }

    /**
     * Move a file source to dest place
     * @param source file from
     * @param dest file to
     */
    public static void moveFile(File source, File dest) throws IOException {
        Files.move(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Extract a zipFilePath to dir
     * @param zipFilePath file.zip
     * @param dir for example /resourcepacks/Pack1/
     */
    public static void unzip(File zipFilePath, File dir) throws IOException {
        UnzipParameters unzipParameters = new UnzipParameters();
        unzipParameters.setExtractSymbolicLinks(false);
        ZipFile zip = new ZipFile(zipFilePath);
        zip.extractAll(dir.getPath(), unzipParameters);
        zip.close();
    }


    /**
     * Force delete a directory
     * @param file directory only!
     */
    public static void recursiveDeleteDirectory(File file) {
        try {
            if (!file.isDirectory()) {
                throw new RuntimeException("File not a directory.");
            }
            FileUtils.deleteDirectory(file);

        } catch (IOException e) {
            throw new RuntimeException("Exception while recursive delete dir " + file, e);
        }
    }

    private static boolean _nioIsDirExistsAndEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return entries.findFirst().isEmpty();
            }
        }

        return false;
    }

    /**
     * Delete path and remove empty parent dirs
     */
    public static void nioSmartDelete(Path toDel) throws IOException {
        Path toDelParent = toDel.getParent();
        Files.deleteIfExists(toDel);
        if (toDelParent != null && _nioIsDirExistsAndEmpty(toDelParent)) {
            nioSmartDelete(toDelParent);
        }
    }


    /**
     * Write a text to path
     */
    public static void nioWriteText(Path path, String text) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
            writer.write(text);

        } catch (IOException e) {
            throw new RuntimeException("nioWriteText exception!", e);
        }
    }

    /**
     * Read text from file
     */
    public static String nioReadText(Path path) {
        try {
            if (!Files.exists(path) || Files.isDirectory(path)) {
                throw new RuntimeException("This is not a file. Not found or directory. Cannot be read as text.");
            }
            return Files.readString(path);

        } catch (IOException e) {
            throw new RuntimeException("nioReadText exception!", e);
        }
    }
}
