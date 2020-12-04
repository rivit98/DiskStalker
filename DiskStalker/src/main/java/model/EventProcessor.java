package model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

class EventObject{
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

    public WatchEvent.Kind<Path> getEventType(){
        return pathWatchEvent.kind();
    }
}

public class EventProcessor {
    private final HashMap<WatchKey, File> keyToFileMap = new HashMap<>(); //TODO: remove proper key after deleting node

    public List<EventObject> processEvents(WatchKey key){
        List<EventObject> returnEvents = new ArrayList<>();
        if (!keyToFileMap.containsKey(key)) {
            key.cancel();
            return returnEvents;
        }

        var triggeredDir = (Path) key.watchable();
        var events = key.pollEvents();
        var eventsValid = validateEvents(events);
        if (eventsValid) {
            for (final WatchEvent<?> event : events) {
                @SuppressWarnings("unchecked")
                var castedEvent = (WatchEvent<Path>)event;
                returnEvents.add(new EventObject(triggeredDir, castedEvent));
            }
        }

        var valid = key.reset();
        if (!valid) {
            // processEvent should remove this node automatically, because event fires also for parent folder
            keyToFileMap.remove(key);
        }

        return returnEvents;
    }

    private boolean validateEvents(List<WatchEvent<?>> events) {
        for (var event : events) {
            if (event.kind() == OVERFLOW) { //events may be corrupted
                return false;
            }
        }

        return true;
    }

    public HashMap<WatchKey, File> getDirectoryMap() {
        return keyToFileMap;
    }

    public void addTrackedDirectory(WatchKey key, File f){
        keyToFileMap.put(key, f);
    }

}
