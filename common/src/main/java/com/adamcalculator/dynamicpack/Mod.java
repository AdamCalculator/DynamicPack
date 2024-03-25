package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.util.Out;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class Mod {
    public static final long VERSION_BUILD = 30;
    public static final String VERSION_NAME_MOD = "1.0.30";

    public static final String VERSION_NAME_BRANCH = "mc1.16.5";
    public static final String VERSION_NAME =  VERSION_NAME_MOD + "+" + VERSION_NAME_BRANCH;
    public static final String MOD_ID = "dynamicpack";


    // NOTE: for increase contact to mod developer.
    public static final long DYNAMIC_PACK_HTTPS_FILE_SIZE_LIMIT = megabyte(8); // kb -> mb -> 5MB (for files in resourcepack)
    public static final long MODRINTH_HTTPS_FILE_SIZE_LIMIT = megabyte(1024); // 1 GB (for .zip files from modrinth)
    public static final long MOD_MODTINTH_API_LIMIT = megabyte(8); // 8 MB of api
    public static final long GZIP_LIMIT = megabyte(50); // 50 MB of .gz file
    public static final long MOD_FILES_LIMIT = megabyte(8);
    public static final String MODRINTH_URL = "https://modrinth.com/mod/dynamicpack";

    private static final Set<String> ALLOWED_HOSTS = new HashSet<>();
    static {
        ALLOWED_HOSTS.add("modrinth.com");
        ALLOWED_HOSTS.add("github.com");
        ALLOWED_HOSTS.add("github.io");
        ALLOWED_HOSTS.add("githubusercontent.com"); // use github pages instead of this
        if (isLocalHostAllowed()) {
            ALLOWED_HOSTS.add("localhost");
        }
    }

    /**
     * API FOR MODPACKERS etc all-in-one packs
     * @param host host to add.
     * @param requester any object. It is recommended that .toString explicitly give out your name.
     */
    protected static void addAllowedHosts(String host, Object requester) throws Exception {
        if (host == null || requester == null) {
            Out.securityWarning("Try to add allowed hosts is failed: null host or requester");
            throw new Exception("Try to add allowed hosts is failed: null host or requester");
        }

        Out.securityWarning("==== SECURITY WARNING ====");
        Out.securityWarning("# The DynamicPack mod limits the hosts it can interact with.");
        Out.securityWarning("# But a certain requester allowed the mod another host to interact with");
        Out.securityWarning("# ");
        Out.securityWarning("# Host: " + host);
        Out.securityWarning("# Requester: " + requester);
        Out.securityWarning("# StackTrace:");
        Out.securityStackTrace();
        Out.securityWarning("# ");
        Out.securityWarning("===========================");

        ALLOWED_HOSTS.add(host);
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
    // false is equal not safe!1!!! RELEASE=true
    public static boolean isRelease() {
        return true;
    }

    // localhost allowed RELEASE=false
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
        return false;
    }

    public static void debugNetwork() {
        if (isRelease()) return;

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isDebugLogs() {
        return false;
    }

    public static boolean isDebugMessageOnWorldJoin() {
        return false;
    }
}
