package com.adamcalculator.dynamicpack.sync.state;

public class StateDownloading implements SyncProgressState {
    private String name;
    private float percentage;

    public StateDownloading(String name) {
        this.name = name;
    }

    public void setPercentage(float i) {
        this.percentage = i;
    }

    public String getName() {
        return name;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "StateDownloading{" +
                "name='" + name + '\'' +
                ", percentage=" + percentage +
                '}';
    }
}
