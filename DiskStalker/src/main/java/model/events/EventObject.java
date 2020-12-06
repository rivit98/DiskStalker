package model.events;

import java.nio.file.Path;

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
