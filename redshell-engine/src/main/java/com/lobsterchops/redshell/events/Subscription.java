package com.lobsterchops.redshell.events;

/** Handle returned by {@link EventBus#subscribe}; cancel it when the owner is disposed. */
public interface Subscription {

    void cancel();

    boolean isActive();
}