package com.lobsterchops.redshell.save.data;

import com.lobsterchops.redshell.save.KeyValueStore;
import java.util.ArrayList;
import java.util.List;

/** Reads, writes, lists and deletes versioned save slots, upgrading old versions through migrations. */
public final class SaveService {

    private static final String PREFIX = "save.slot.";

    private final KeyValueStore store;
    private final int currentVersion;
    private final List<Migration> migrations = new ArrayList<>();

    public SaveService(KeyValueStore store, int currentVersion, Migration... migrations) {
        this.store = store;
        this.currentVersion = currentVersion;
        for (Migration migration : migrations) {
            this.migrations.add(migration);
        }
    }

    public int currentVersion() {
        return currentVersion;
    }

    /** Writes the data at the current version. */
    public void write(String slotName, SaveData data) {
        data.setVersion(currentVersion);
        store.put(PREFIX + slotName, data.serialize());
        store.flush();
    }

    /** Reads and upgrades a slot, or returns null if it does not exist. */
    public SaveSlot read(String slotName) {
        String text = store.get(PREFIX + slotName);
        if (text == null) {
            return null;
        }
        SaveData data = SaveData.parse(text);
        if (data.version() > currentVersion) {
            throw new IllegalStateException("Slot '" + slotName + "' was written by a newer version: " + data.version());
        }
        while (data.version() < currentVersion) {
            Migration migration = find(data.version());
            if (migration == null) {
                throw new IllegalStateException("No migration from save version " + data.version());
            }
            migration.migrate(data);
            data.setVersion(migration.fromVersion() + 1);
        }
        return new SaveSlot(slotName, data);
    }

    public List<String> list() {
        List<String> names = new ArrayList<>();
        List<String> keys = store.keys();
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).startsWith(PREFIX)) {
                names.add(keys.get(i).substring(PREFIX.length()));
            }
        }
        return names;
    }

    public void delete(String slotName) {
        store.remove(PREFIX + slotName);
        store.flush();
    }

    private Migration find(int fromVersion) {
        for (int i = 0; i < migrations.size(); i++) {
            if (migrations.get(i).fromVersion() == fromVersion) {
                return migrations.get(i);
            }
        }
        return null;
    }
}