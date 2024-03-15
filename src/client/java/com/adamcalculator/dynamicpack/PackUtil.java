package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

public class PackUtil {

    public static JSONObject readJson(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return new JSONObject(new String(bytes, StandardCharsets.UTF_8));
    }


    public static void addFileToZip(File zipFile, String name, String text) throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        java.nio.file.Path path = zipFile.toPath();
        URI uri = URI.create("jar:" + path.toUri());
        try (FileSystem fs = FileSystems.newFileSystem(uri, env))
        {
            Path nf = fs.getPath(name);
            try (Writer writer = java.nio.file.Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                writer.write(text);
            }
        }
    }
}
