package org.agh.diskstalker.filesystem.dirwatcher;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.events.filesystemEvents.FilesystemEvent;
import org.agh.diskstalker.events.filesystemEvents.FilesystemEventType;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.nio.file.Path;

@Slf4j
public class DirWatcher implements IFilesystemWatcher {
    private final PublishSubject<FilesystemEvent> subject = PublishSubject.create();
    private final CachedFileAlterationMonitor monitor;
    private boolean running = false;
    private Disposable monitoringDisposable;

    public DirWatcher(Path path, long pollingTimeMs) {
        if (pollingTimeMs < 500) {
            log.info("polling time cannot be less than 500ms");
            pollingTimeMs = 500;
        }

        var listener = new FileChangeListener(this);
        var observer = new FileAlterationObserver(path.toFile());
        observer.addListener(listener);
        monitor = new CachedFileAlterationMonitor(pollingTimeMs, observer);
    }

    @Override
    public void emitEvent(Path path, FilesystemEventType filesystemEventType) {
        subject.onNext(new FilesystemEvent(path, filesystemEventType));
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }

        try {
            subject.onComplete();
            monitoringDisposable.dispose();
            monitor.stop();
        } catch (Exception ignored) {
            log.warn("Cannot stop DirWatcher");
        }
    }

    @Override
    public Observable<Object> initScan() {
        return monitor.initScan();
    }

    @Override
    public Observable<FilesystemEvent> start() {
        try{
            monitoringDisposable = monitor.start().subscribe();
            running = true;
        }catch (IllegalStateException e){
            log.warn("DirWatcher" + e.getMessage());
        }

        return subject;
    }
}
