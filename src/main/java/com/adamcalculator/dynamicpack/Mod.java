package com.adamcalculator.dynamicpack;

public class Mod {
    // NOTE: for increase contact to mod developer.
    public static final long DYNAMIC_PACK_HTTPS_FILE_SIZE_LIMIT = megabyte(8); // kb -> mb -> 5MB (for files in resourcepack)
    public static final long MODRINTH_HTTPS_FILE_SIZE_LIMIT = megabyte(1024); // 1 GB (for .zip files from modrinth)
    public static final long MOD_MODTINTH_API_LIMIT = megabyte(8); // 8 MB of api
    public static final long GZIP_LIMIT = megabyte(50); // 50 MB of .gz file
    public static final long MOD_FILES_LIMIT = megabyte(8);

    private static long megabyte(long mb) {
        return 1024L * 1024L * mb;
    }

    public static boolean isRelease() {
        return false;
    }
}
