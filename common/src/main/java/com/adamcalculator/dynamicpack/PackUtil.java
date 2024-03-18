package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.util.AFiles;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PackUtil {

    public static JSONObject readJson(InputStream inputStream) throws IOException {
        return new JSONObject(readString(inputStream));
    }

    public static String readString(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static JSONObject readJson(Path path) throws IOException {
        return new JSONObject(readString(path));
    }

    public static String readString(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }


    public static void addFileToZip(File zipFile, String name, String text) throws IOException {
        openPackFileSystem(zipFile, path -> {
            AFiles.nioWriteText(path.resolve(name), text);
        });
    }

    public static void openPackFileSystem(File pack, Consumer<Path> consumer) throws IOException {
        if (!pack.exists()) {
            throw new FileNotFoundException(pack.getCanonicalPath());
        }

        if (pack.isDirectory()) {
            consumer.accept(pack.toPath());


        } else if (pack.isFile() && pack.getName().toLowerCase().endsWith(".zip")) {
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");

            URI uri = URI.create("jar:" + pack.toPath().toUri());
            try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
                consumer.accept(fs.getPath(""));
            }

        } else {
            throw new RuntimeException("Failed to open pack file system");
        }
    }

    public static void walkScan(Set<String> buffer, Path path) throws IOException {
        Stream<Path> entries = Files.walk(path, Integer.MAX_VALUE);
        entries.forEach(path1 -> {
            if (!Files.isDirectory(path1)) {
                buffer.add(path1.toString());
            }
        });
    }

    // if path exist and isFile
    public static boolean isPathFileExists(Path path) {
        if (Files.exists(path)) {
            return !Files.isDirectory(path);
        }
        return false;
    }
}
