package model;

import javafx.beans.property.SimpleLongProperty;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Optional;

public class FileData {
    private final File file;
    private final boolean isDirectory;
    private final SimpleLongProperty sizeProperty;
    private WatchKey event;


    public FileData(File file, WatchKey event) {
        this.event = event;
        this.file = file;
        this.isDirectory = file.isDirectory();
        this.sizeProperty = new SimpleLongProperty(isFile() ? file.length() : 0);
    }

    public FileData(File file) {
        this(file, null);
    }

    public FileData(Path path) {
        this(path.toFile(), null);
    }

    public Optional<WatchKey> getEventKey() {
        return Optional.ofNullable(event);
    }

    public void setEventKey(WatchKey event) {
        this.event = event;
    }

    public File getFile() {
        return file;
    }

    public Path getPath() {
        return file.toPath();
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
        return file.length();
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
