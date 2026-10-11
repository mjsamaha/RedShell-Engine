package com.lobsterchops.redshell.events.types;

import com.lobsterchops.redshell.events.Event;

/** Published when an asset group has finished loading. */
public final class AssetsLoadedEvent implements Event {

    private final String groupName;

    public AssetsLoadedEvent(String groupName) {
        this.groupName = groupName;
    }

    public String groupName() {
        return groupName;
    }
}