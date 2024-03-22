package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.util.FailedOpenPackFileSystemException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static void openPackFileSystem(File pack, Consumer<Path> consumer) throws Exception {
        if (!pack.exists()) {
            throw new FileNotFoundException(pack.getCanonicalPath());
        }

        if (pack.isDirectory()) {
            consumer.accept(pack.toPath());


        } else if (pack.isFile() && pack.getName().toLowerCase().endsWith(".zip")) {
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");

            URI uri = URI.create("jar:" + pack.toPath().toUri());
            Exception ex = null;
            try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
                try {
                    consumer.accept(fs.getPath(""));
                } catch (Exception e) {
                    ex = e;
                }
            }
            if (ex != null) {
                throw ex;
            }

        } else {
            throw new FailedOpenPackFileSystemException("Failed to open pack file system");
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
