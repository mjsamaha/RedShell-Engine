package com.lobsterchops.redshell.debug.log;

/** Destination for log messages: console, file or memory. */
public interface LogSink {

    /** Writes one message. {@code throwable} may be null. */
    void write(LogLevel level, String tag, String message, Throwable throwable);
}
