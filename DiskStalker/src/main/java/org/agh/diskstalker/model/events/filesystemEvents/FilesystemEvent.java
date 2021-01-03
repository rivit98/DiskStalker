package org.agh.diskstalker.model.events.filesystemEvents;

import java.nio.file.Path;

public class FilesystemEvent {
    private final Path targetDir;
    private final FilesystemEventType type;

    public FilesystemEvent(Path targetDir, FilesystemEventType filesystemEventType) {
        this.targetDir = targetDir;
        this.type = filesystemEventType;
    }

    public Path getTargetDir() {
        return targetDir;
    }

    public FilesystemEventType getEventType() {
        return type;
    }
}
