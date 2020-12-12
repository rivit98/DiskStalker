package org.agh.diskstalker.filesystem.dirwatcher;

import io.reactivex.rxjava3.core.Observable;
import org.agh.diskstalker.model.events.EventObject;
import org.agh.diskstalker.model.events.EventType;

import java.nio.file.Path;

public interface IFilesystemWatcher {
    void emitEvent(Path path, EventType eventType);

    void stop();

    Observable<EventObject> start();
}
