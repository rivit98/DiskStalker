package model;

import javafx.scene.control.TreeItem;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class EventProcessor {
    private final HashMap<WatchKey, Path> keyToFileMap = new HashMap<>(); //TODO: remove proper key after deleting node

    public void clearTrackedDirectories(){
        keyToFileMap.clear();
    }

    public void addTrackedDirectory(WatchKey key, Path f){
        keyToFileMap.put(key, f);
    }

    public Path removeTrackedDirectory(WatchKey key){
        key.cancel();
        return keyToFileMap.remove(key);
    }

    public void removeTrackedDirectoriesRecursively(TreeItem<FileData> node) {

        node.getValue().getEventKey().ifPresent(this::removeTrackedDirectory);
        node.getChildren().forEach(this::removeTrackedDirectoriesRecursively);
    }
}
