package org.agh.diskstalker.model.events;

import org.agh.diskstalker.model.ObservedFolder;

import java.nio.file.Path;

public class FolderEvent {
    private final FolderEventType type;
    private final String message;

    public FolderEvent(FolderEventType eventType) {
        this(eventType, null);
    }

    public FolderEvent(FolderEventType eventType, String message) {
        this.type = eventType;
        this.message = message;
    }


    public FolderEventType getEventType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
