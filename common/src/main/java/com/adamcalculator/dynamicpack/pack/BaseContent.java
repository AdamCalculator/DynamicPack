package com.adamcalculator.dynamicpack.pack;

public class BaseContent {
    private final DynamicRepoRemote parent;
    private final String id;
    private final boolean required;
    private OverrideType overrideType;
    private final String name;
    private final boolean defaultStatus;

    public BaseContent(DynamicRepoRemote parent, String id, boolean required, OverrideType overrideType, String name, boolean defaultStatus) {
        this.parent = parent;
        this.id = id;
        this.required = required;
        this.overrideType = overrideType;
        this.name = name;
        this.defaultStatus = defaultStatus;
    }

    public String getId() {
        return id;
    }

    public boolean isRequired() {
        return required;
    }

    public void nextOverride() throws Exception {
        setOverrideType(overrideType.next());
    }

    public OverrideType getOverride() {
        return overrideType;
    }

    public boolean getWithDefaultState() {
        return defaultStatus;
    }

    public String getName() {
        return name;
    }

    public void setOverrideType(OverrideType overrideType) throws Exception {
        this.overrideType = overrideType;
        parent.setContentOverride(this, overrideType);
    }
}
