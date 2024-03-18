package tests;

import com.adamcalculator.dynamicpack.PackUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class PackUtilTest {
    @Test
    public void test() throws IOException {
        PackUtil.openPackFileSystem(new File("tests_files/filedir"), new Consumer<Path>() {
            @Override
            public void accept(Path path) {
                Path resolve = path.resolve("file.txt");
                try {
                    System.out.println(Files.readString(resolve));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        PackUtil.openPackFileSystem(new File("tests_files/filezip.zip"), new Consumer<Path>() {
            @Override
            public void accept(Path path) {
                Path resolve = path.resolve("file.txt");
                try {
                    System.out.println(Files.readString(resolve));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
