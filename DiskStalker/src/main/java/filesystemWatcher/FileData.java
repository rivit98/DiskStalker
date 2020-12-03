package filesystemWatcher;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchKey;

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

    public WatchKey getEvent() {
        return event;
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
        return file.isFile();
    }

    public long size(){
        return file.length();
    }

    @Override
    public String toString() {
        return "[" + (isDirectory() ? "DIR" : "FILE") + "] " + file.getName() + " " + size();
    }
}
