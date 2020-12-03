package model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Optional;

//TODO: figure out if deriving from File is better idea
public class FileData {
    private final File file;
    private WatchKey event;

    public FileData(File file, WatchKey event) {
        this.event = event;
        this.file = file;
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
        return file.isDirectory();
    }

    public boolean isFile(){
        return !isDirectory();
    }

    public long size(){
        return file.length();
    }

    @Override
    public String toString() {
        return file.getName() + " " + size();
    }
}
