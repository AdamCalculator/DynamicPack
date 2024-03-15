package com.adamcalculator.dynamicpack;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        Set<String> strings = new HashSet<>();
        Path packRootPath;

        PackUtil.walkScan(strings, new File("").toPath());

        System.out.println(strings);
    }
}
