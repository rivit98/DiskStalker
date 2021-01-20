package org.agh.diskstalker.events.filesystemEvents;

public enum FilesystemEventType {
    FILE_CREATED,
    DIR_CREATED,

    FILE_MODIFIED,
    DIR_MODIFIED,

    FILE_DELETED,
    DIR_DELETED;

    public boolean isDeleteEvent(){
        return this == FILE_DELETED || this == DIR_DELETED;
    }

    public boolean isCreateEvent(){
        return this == FILE_CREATED || this == DIR_CREATED;
    }

    public boolean isModifyEvent(){
        return this == FILE_MODIFIED || this == DIR_MODIFIED;
    }

    public boolean isFileEvent(){
        return this == FILE_CREATED || this == FILE_DELETED || this == FILE_MODIFIED;
    }

    public boolean isDirEvent(){
        return this == DIR_CREATED || this == DIR_DELETED || this == DIR_MODIFIED;
    }
}
