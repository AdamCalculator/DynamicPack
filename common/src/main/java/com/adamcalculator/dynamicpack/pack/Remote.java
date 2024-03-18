package com.adamcalculator.dynamicpack.pack;

import com.adamcalculator.dynamicpack.sync.PackSyncProgress;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public abstract class Remote {

    public abstract boolean checkUpdateAvailable() throws IOException;

    public abstract boolean sync(PackSyncProgress progress) throws IOException, NoSuchAlgorithmException;
}
