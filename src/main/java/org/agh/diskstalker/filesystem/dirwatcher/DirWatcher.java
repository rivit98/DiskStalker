package org.agh.diskstalker.filesystem.dirwatcher;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.events.filesystemEvents.FilesystemEvent;
import org.agh.diskstalker.events.filesystemEvents.FilesystemEventType;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.nio.file.Path;

@Slf4j
public class DirWatcher implements IFilesystemWatcher {
    private final PublishSubject<FilesystemEvent> subject = PublishSubject.create();
    private final FileAlterationMonitor monitor;
    private final long pollingInterval = 1700;

    public DirWatcher(Path path) {
        var listener = new FileChangeListener(this);
        var observer = new FileAlterationObserver(path.toFile());
        observer.addListener(listener);
        monitor = new FileAlterationMonitor(pollingInterval, observer);
    }

    @Override
    public void emitEvent(Path path, FilesystemEventType filesystemEventType) {
        subject.onNext(new FilesystemEvent(path, filesystemEventType));
    }

    @Override
    public void stop() {
        try {
            subject.onComplete();
            monitor.stop();
        } catch (Exception ignored) {
            log.warn("Cannot stop DirWatcher");
        }
    }

    @Override
    public Observable<FilesystemEvent> start() {
        try {
            monitor.start();
        } catch (Exception ignored) {
            log.warn("Cannot start DirWatcher");
        }
        return subject;
    }
}
