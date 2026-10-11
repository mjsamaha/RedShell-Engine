package com.lobsterchops.redshell.debug.filelog;

import com.badlogic.gdx.files.FileHandle;
import com.lobsterchops.redshell.debug.log.LogLevel;
import com.lobsterchops.redshell.debug.log.LogSink;

/**
 * Appends log lines to a file. Desktop only: browsers have no writable file system, so the application adds this
 * sink on desktop and relies on the memory sink on the web.
 */
public final class FileLogSink implements LogSink {

    private final FileHandle file;

    /** The file is replaced on construction so each run starts with a fresh log. */
    public FileLogSink(FileHandle file) {
        this.file = file;
        file.writeString("", false);
    }

    @Override
    public void write(LogLevel level, String tag, String message, Throwable throwable) {
        StringBuilder line = new StringBuilder();
        line.append(level).append(" [").append(tag).append("] ").append(message);
        if (throwable != null) {
            line.append(" (").append(throwable).append(')');
        }
        line.append('\n');
        file.writeString(line.toString(), true);
    }
}