package com.lobsterchops.redshell.debug.log;

import java.util.HashSet;
import java.util.Set;

/**
 * Centralized reporting for engine-level failures. The same failure (same context, type and message)
 * is logged only once, so an error raised every frame does not flood the log.
 */
public final class ExceptionReporter {

    private static final String TAG = "Engine";
    private static final int MAX_REMEMBERED = 64;

    private final Log log;
    private final Set<String> reported = new HashSet<>();

    public ExceptionReporter(Log log) {
        this.log = log;
    }

    /** Returns true if the failure was new and has been logged. */
    public boolean report(String context, Throwable throwable) {
        String key = context + "|" + throwable.getClass().getName() + "|" + throwable.getMessage();
        if (reported.contains(key)) {
            return false;
        }
        if (reported.size() >= MAX_REMEMBERED) {
            reported.clear();
        }
        reported.add(key);
        log.error(TAG, "Unhandled exception in " + context, throwable);
        return true;
    }
}
