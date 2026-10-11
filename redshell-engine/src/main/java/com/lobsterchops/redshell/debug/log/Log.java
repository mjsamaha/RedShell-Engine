package com.lobsterchops.redshell.debug.log;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin logging facade with levels and tags. Messages below the minimum level are dropped,
 * the rest are forwarded to every registered {@link LogSink}. A failing sink never breaks the caller.
 */
public final class Log {

    private LogLevel level;
    private final List<LogSink> sinks = new ArrayList<>();

    public Log(LogLevel level, LogSink... initialSinks) {
        this.level = level;
        for (LogSink sink : initialSinks) {
            sinks.add(sink);
        }
    }

    public LogLevel level() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public boolean isEnabled(LogLevel candidate) {
        return candidate.ordinal() >= level.ordinal();
    }

    public void addSink(LogSink sink) {
        sinks.add(sink);
    }

    public void removeSink(LogSink sink) {
        sinks.remove(sink);
    }

    public void debug(String tag, String message) {
        log(LogLevel.DEBUG, tag, message, null);
    }

    public void info(String tag, String message) {
        log(LogLevel.INFO, tag, message, null);
    }

    public void warn(String tag, String message) {
        log(LogLevel.WARN, tag, message, null);
    }

    public void error(String tag, String message) {
        log(LogLevel.ERROR, tag, message, null);
    }

    public void error(String tag, String message, Throwable throwable) {
        log(LogLevel.ERROR, tag, message, throwable);
    }

    public void log(LogLevel messageLevel, String tag, String message, Throwable throwable) {
        if (!isEnabled(messageLevel)) {
            return;
        }
        for (int i = 0; i < sinks.size(); i++) {
            try {
                sinks.get(i).write(messageLevel, tag, message, throwable);
            } catch (RuntimeException ignored) {
                // A broken sink must not take the engine down.
            }
        }
    }
}
