package filesystem;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import model.EventObject;
import model.EventType;
import model.FileChangeListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.nio.file.Path;

public class DirWatcher {
    private final PublishSubject<EventObject> subject = PublishSubject.create();
    private final FileAlterationMonitor monitor;
    private final long pollingInterval = 2 * 1000;

    public DirWatcher(Path path) {
        monitor = new FileAlterationMonitor(pollingInterval);
        var listener = new FileChangeListener(this);

        var observer = new FileAlterationObserver(path.toFile());
        observer.addListener(listener);
        monitor.addObserver(observer);
    }

    public void emitKey(Path path, EventType eventType) {
        subject.onNext(new EventObject(path, eventType));
    }

    public void stop() {
        try {
            monitor.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Observable<EventObject> watchForChanges() {
        try {
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subject;
    }
}
