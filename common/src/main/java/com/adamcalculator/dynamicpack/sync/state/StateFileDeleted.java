package com.adamcalculator.dynamicpack.sync.state;

import java.nio.file.Path;

public class StateFileDeleted implements SyncProgressState {
    private final Path path;

    public StateFileDeleted(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "StateFileDeleted{" +
                "path=" + path +
                '}';
    }
}
