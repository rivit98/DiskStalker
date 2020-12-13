package org.agh.diskstalker.model.events;

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

    public boolean isModifyEvent(){
        return type == FilesystemEventType.FILE_MODIFIED || type == FilesystemEventType.DIR_MODIFIED;
    }

    public boolean isCreateEvent(){
        return type == FilesystemEventType.FILE_CREATED || type == FilesystemEventType.DIR_CREATED;
    }

    public boolean isDeleteEvent(){
        return type == FilesystemEventType.FILE_DELETED || type == FilesystemEventType.DIR_DELETED;
    }
}
