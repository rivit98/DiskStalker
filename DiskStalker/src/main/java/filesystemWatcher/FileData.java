package filesystemWatcher;

import java.io.File;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

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

    public void setEvent(WatchKey event) {
        this.event = event;
    }

    public File getFile() {
        return file;
    }
}
