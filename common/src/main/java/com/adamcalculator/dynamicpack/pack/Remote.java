package com.adamcalculator.dynamicpack.pack;

import java.io.IOException;

public abstract class Remote {

    public abstract boolean checkUpdateAvailable() throws IOException;
}
