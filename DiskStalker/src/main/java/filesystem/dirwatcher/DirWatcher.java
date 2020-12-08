package filesystem.dirwatcher;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import model.events.EventObject;
import model.events.EventType;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.nio.file.Path;

public class DirWatcher implements IFilesystemWatcher {
    private final PublishSubject<EventObject> subject = PublishSubject.create();
    private final FileAlterationMonitor monitor;
    private final long pollingInterval = 1600;

    public DirWatcher(Path path) {
        monitor = new FileAlterationMonitor(pollingInterval);
        var listener = new FileChangeListener(this);

        var observer = new FileAlterationObserver(path.toFile());
        observer.addListener(listener);
        monitor.addObserver(observer);
    }

    @Override
    public void emitEvent(Path path, EventType eventType) {
        subject.onNext(new EventObject(path, eventType));
    }

    @Override
    public void stop() {
        try {
            monitor.stop();
        } catch (Exception ignored) {
        }
    }

    @Override
    public Observable<EventObject> start() {
        try {
            monitor.start();
        } catch (Exception ignored) {
        }
        return subject;
    }
}
