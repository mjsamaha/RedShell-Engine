package com.lobsterchops.redshell.debug.log;

/** Writes to standard output (errors and warnings to standard error). Appears in the browser console on web. */
public final class ConsoleLogSink implements LogSink {

    @Override
    public void write(LogLevel level, String tag, String message, Throwable throwable) {
        String line = "[" + level + "] [" + tag + "] " + message;
        if (level == LogLevel.WARN || level == LogLevel.ERROR) {
            System.err.println(line);
            if (throwable != null) {
                throwable.printStackTrace();
            }
        } else {
            System.out.println(line);
            if (throwable != null) {
                throwable.printStackTrace(System.out);
            }
        }
    }
}
