package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;

import java.io.IOException;

public class DebugThread extends Thread {
    public static void startDebug() {
        new DebugThread().start();
    }

    public DebugThread() {
        setName("DynamicPack-DebugThread");
    }

    @Override
    public void run() {
        while (true) {
            try {
                debug();
                Thread.sleep(5000);
            } catch (Exception e) {
                Out.println("Error: " + e);
                Out.e(e);
            }
        }
    }

    private void debug() throws IOException {
        for (Pack pack : DynamicPackMod.packs) {
            Out.println("[Debug] pack " + pack.getLocation().getName() + ": updateAvailable: " + pack.checkIsUpdateAvailable());
        }
    }
}
