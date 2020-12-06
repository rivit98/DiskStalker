package filesystem.dirwatcher;

import io.reactivex.rxjava3.core.Observable;
import model.events.EventObject;
import model.events.EventType;

import java.nio.file.Path;

public interface IFilesystemWatcher {
    void emitEvent(Path path, EventType eventType);

    void stop();

    Observable<EventObject> start();
}
