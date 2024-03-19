package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.util.Out;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class Mod {
    // NOTE: for increase contact to mod developer.
    public static final long DYNAMIC_PACK_HTTPS_FILE_SIZE_LIMIT = megabyte(8); // kb -> mb -> 5MB (for files in resourcepack)
    public static final long MODRINTH_HTTPS_FILE_SIZE_LIMIT = megabyte(1024); // 1 GB (for .zip files from modrinth)
    public static final long MOD_MODTINTH_API_LIMIT = megabyte(8); // 8 MB of api
    public static final long GZIP_LIMIT = megabyte(50); // 50 MB of .gz file
    public static final long MOD_FILES_LIMIT = megabyte(8);

    public static final Set<String> ALLOWED_HOSTS = new HashSet<>();
    static {
        ALLOWED_HOSTS.add("modrinth.com");
        ALLOWED_HOSTS.add("github.com");
        ALLOWED_HOSTS.add("github.io");
        ALLOWED_HOSTS.add("githubusercontent.com");
        if (isLocalHostAllowed()) {
            ALLOWED_HOSTS.add("localhost");
        }
    }

    public static boolean isUrlHostTrusted(String url) throws IOException {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            for (String allowedHost : ALLOWED_HOSTS) {
                if (host.equals(allowedHost)) {
                    Out.println("Check trusted(true): " + host);
                    return true;
                }
                if (host.endsWith("." + allowedHost)) {
                    Out.println("Check trusted(true): " + host);
                    return true;
                }
            }
            Out.println("Check trusted(false): " + host);
            return false;
        } catch (Exception e) {
            throw new IOException("Error", e);
        }
    }

    public static boolean isBlockAllNotTrustedNetworks() {
        return true;
    }

    private static long megabyte(long mb) {
        return 1024L * 1024L * mb;
    }

    // TRUE FOR ALL PUBLIC VERSION!!!!!!
    // false is equal not safe!1!!!
    public static boolean isRelease() {
        return true;
    }

    // localhost allowed
    private static boolean isLocalHostAllowed() {
        return false;
    }

    // file_debug_only:// allowed RELEASE=false
    public static boolean isFileDebugSchemeAllowed() {
        return false;
    }

    // http:// allowed RELEASE=false
    public static boolean isHTTPTrafficAllowed() {
        return false;
    }

    // DebugScreen allowed
    public static boolean isDebugScreenAllowed() {
        return true;
    }

    public static void debugNetwork() {
        if (isRelease()) return;

        try {
            Thread.sleep(13);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
