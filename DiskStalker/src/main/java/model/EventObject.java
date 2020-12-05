package model;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class EventObject {
    private final Path targetDir;
    private final EventType type;

    public EventObject(Path targetDir, EventType eventType) {
        this.targetDir = targetDir;
        this.type = eventType;
    }

    public Path getTargetDir() {
        return targetDir;
    }

    public EventType getEventType() {
        return type;
    }
}
