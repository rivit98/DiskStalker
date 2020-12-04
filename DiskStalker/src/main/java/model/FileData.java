package model;

import javafx.beans.property.SimpleLongProperty;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Optional;

//TODO: figure out if deriving from File is better idea
public class FileData {
    private final File file;
    private WatchKey event;
    private final boolean isDirectory;
    private final SimpleLongProperty size;
    public FileData(File file, WatchKey event) {
        this.event = event;
        this.file = file;
        this.isDirectory = file.isDirectory();
        if(isFile()){
            size = new SimpleLongProperty(file.length());
        }
        else {
            size = new SimpleLongProperty(0);
        }
    }

    public FileData(File file) {
        this(file, null);
    }

    public FileData(Path path) {
        this(path.toFile(), null);
    }

    public Optional<WatchKey> getEvent() {
        return Optional.ofNullable(event);
    }

    public void setEventKey(WatchKey event) {
        this.event = event;
    }

    public File getFile() {
        return file;
    }

    public Path getPath(){
        return file.toPath();
    }

    public boolean isDirectory(){
        return isDirectory;
    }

    public boolean isFile(){
        return !isDirectory;
    }

    public SimpleLongProperty size(){
        return size;
    }

    public void refreshFileSize() {
        this.size.set(file.length());
    }

    public void modifySize(long size) {
        this.size.set(this.size.getValue() + size);
    }
}
