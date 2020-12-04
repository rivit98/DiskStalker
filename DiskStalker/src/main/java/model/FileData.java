package model;

import javafx.beans.property.SimpleLongProperty;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Optional;

public class FileData {
    private final Path file;
    private final boolean isDirectory;
    private final SimpleLongProperty sizeProperty;
    private WatchKey event;


    public FileData(Path file, WatchKey event) {
        this.event = event;
        this.file = file;
        var f = file.toFile();
        this.isDirectory = f.isDirectory();
        this.sizeProperty = new SimpleLongProperty(isFile() ? f.length() : 0);
    }

    public FileData(Path path) {
        this(path, null);
    }

    public Optional<WatchKey> getEventKey() {
        return Optional.ofNullable(event);
    }

    public void setEventKey(WatchKey event) {
        this.event = event;
    }

    public Path getPath() {
        return file;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isFile() {
        return !isDirectory;
    }

    public SimpleLongProperty sizePropertyProperty() {
        return sizeProperty;
    }

    public long getSize() {
        return sizeProperty.getValue();
    }

    public long getActualSize(){
        return file.toFile().length();
    }

    public long updateFileSize() { // update size and return the old one
        var actualSize = getActualSize();
        sizeProperty.set(actualSize);
        return actualSize;
    }

    public void modifySize(long size) {
        sizeProperty.set(getSize() + size);
    }
}
