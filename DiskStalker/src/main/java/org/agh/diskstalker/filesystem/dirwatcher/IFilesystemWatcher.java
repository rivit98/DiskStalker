package org.agh.diskstalker.filesystem.dirwatcher;

import io.reactivex.rxjava3.core.Observable;
import org.agh.diskstalker.model.events.FilesystemEvent;
import org.agh.diskstalker.model.events.FilesystemEventType;

import java.nio.file.Path;

public interface IFilesystemWatcher {
    void emitEvent(Path path, FilesystemEventType filesystemEventType);

    void stop();

    Observable<FilesystemEvent> start();
}
