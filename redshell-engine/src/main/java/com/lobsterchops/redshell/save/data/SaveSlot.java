package com.lobsterchops.redshell.save.data;

/** A named save slot and the data read from it. */
public final class SaveSlot {

    private final String name;
    private final SaveData data;

    public SaveSlot(String name, SaveData data) {
        this.name = name;
        this.data = data;
    }

    public String name() {
        return name;
    }

    public SaveData data() {
        return data;
    }
}