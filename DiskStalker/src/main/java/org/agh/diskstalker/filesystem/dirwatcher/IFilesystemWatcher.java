package org.agh.diskstalker.filesystem.dirwatcher;

import io.reactivex.rxjava3.core.Observable;
import org.agh.diskstalker.model.events.filesystemEvents.FilesystemEvent;
import org.agh.diskstalker.model.events.filesystemEvents.FilesystemEventType;

import java.nio.file.Path;

public interface IFilesystemWatcher {
    void emitEvent(Path path, FilesystemEventType filesystemEventType);

    void stop();

    Observable<FilesystemEvent> start();
}
