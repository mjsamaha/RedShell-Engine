package com.lobsterchops.redshell.core.lifecycle;

/**
 * Receives the start and end of named scopes, such as one system's update. The engine only knows this
 * interface; the debug profiler implements it, so removing the profiler changes no behaviour.
 */
public interface ScopeListener {

    void begin(String scope);

    void end(String scope);
}