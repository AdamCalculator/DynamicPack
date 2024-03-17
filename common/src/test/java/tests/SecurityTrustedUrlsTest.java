package tests;

import com.adamcalculator.dynamicpack.pack.DynamicRepoSyncProcessV1;
import com.adamcalculator.dynamicpack.util.Out;
import com.adamcalculator.dynamicpack.util.Urls;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SecurityTrustedUrlsTest {

    @Test
    public void d() {
        Out.USE_SOUT = true;

        Assertions.assertThrows(Exception.class, () -> Urls.parseContent("https://google.com", 1732132132));
        Assertions.assertThrows(Exception.class, () -> Out.println(Urls.parseContent("https://modrinth.com", 6)));

        Assertions.assertThrows(Exception.class, () -> Urls.parseContent("https://modrinth.com.google.com", 1732132132));

        Assertions.assertThrows(Exception.class, () -> Urls.parseContent("https://fakemodrinth.com.com", 1732132132));


        Assertions.assertDoesNotThrow(() -> {
            DynamicRepoSyncProcessV1.getPath("assets", "minecraft/lang/en_us.json");
            DynamicRepoSyncProcessV1.getPath("assets/", "minecraft/lang/en_us.json");
            DynamicRepoSyncProcessV1.getPath("assets", "/minecraft/lang/en_us.json");
            DynamicRepoSyncProcessV1.getPath("assets", "/minecraft/lang/en_us.json");
            DynamicRepoSyncProcessV1.getPath("/assets/", "///minecraft/lang/en_us.json");
        });



        Assertions.assertThrows(Exception.class, () -> {
            DynamicRepoSyncProcessV1.getPath("assets/../../../../", "minecraft/lang/en_us.json");
        });

        Assertions.assertThrows(Exception.class, () -> {
            DynamicRepoSyncProcessV1.getPath("assets/../../../../", "minecraft/lang/en_us.json");
        });

        Assertions.assertThrows(Exception.class, () -> {
            DynamicRepoSyncProcessV1.getPath("assets/../../../../", "minecraft/lang/en_us.json");
        });

    }
}
