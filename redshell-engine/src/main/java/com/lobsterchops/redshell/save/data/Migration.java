package com.lobsterchops.redshell.save.data;

/** Upgrades save data by exactly one version. */
public interface Migration {

    /** The version this migration reads. Data leaves it at {@code fromVersion() + 1}. */
    int fromVersion();

    void migrate(SaveData data);
}