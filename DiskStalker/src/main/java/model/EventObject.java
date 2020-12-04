package model;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

class EventObject {
    private final Path targetDir;
    private final WatchEvent<Path> pathWatchEvent;

    public EventObject(Path targetDir, WatchEvent<Path> pathWatchEvent) {
        this.targetDir = targetDir;
        this.pathWatchEvent = pathWatchEvent;
    }

    public Path getTargetDir() {
        return targetDir;
    }

    public WatchEvent<Path> getPathWatchEvent() {
        return pathWatchEvent;
    }

    public WatchEvent.Kind<Path> getEventType() {
        return pathWatchEvent.kind();
    }
}
