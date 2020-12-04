package model;

import javafx.scene.control.TreeItem;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

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
                System.out.println("invalid event");
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

    public File removeTrackedDirectory(WatchKey key){
        key.cancel();
        return keyToFileMap.remove(key);
    }

    public void removeTrackedDirectoriesRecursively(TreeItem<FileData> node) {
        System.out.println("eventprocessor remove: " + node.getValue().getFile().getName());

        node.getValue().getEventKey().ifPresent(this::removeTrackedDirectory);
        node.getChildren().forEach(this::removeTrackedDirectoriesRecursively);
    }
}
