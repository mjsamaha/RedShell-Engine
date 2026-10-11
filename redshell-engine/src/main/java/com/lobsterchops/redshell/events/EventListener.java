package com.lobsterchops.redshell.events;

/** Receives events of one type from the {@link EventBus}. */
public interface EventListener<E extends Event> {

    void onEvent(E event);
}