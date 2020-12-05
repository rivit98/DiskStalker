package filesystem;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import model.EventObject;
import model.EventType;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Optional;

import static java.nio.file.StandardWatchEventKinds.*;

public class DirWatcher {
    private final PublishSubject<EventObject> subject = PublishSubject.create();
    private final FileAlterationMonitor monitor;
    private final long pollingInterval = 2 * 1000;

    public DirWatcher(Path path) {
        FileAlterationObserver observer = new FileAlterationObserver(path.toFile());
        monitor = new FileAlterationMonitor(pollingInterval);
        FileAlterationListener listener = new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                emitKey(file.toPath(), EventType.FILE_CREATED);
            }

            @Override
            public void onFileChange(File file) {
                emitKey(file.toPath(), EventType.FILE_MODIFIED);
            }

            @Override
            public void onFileDelete(File file) {
                emitKey(file.toPath(), EventType.FILE_DELETED);
            }

            @Override
            public void onDirectoryCreate(File directory) {
                emitKey(directory.toPath(), EventType.DIR_CREATED);
            }

            @Override
            public void onDirectoryChange(File directory) {
                emitKey(directory.toPath(), EventType.DIR_MODIFIED);
            }

            @Override
            public void onDirectoryDelete(File directory) {
                emitKey(directory.toPath(), EventType.DIR_DELETED);
            }
        };

        observer.addListener(listener);
        monitor.addObserver(observer);
    }

    private void emitKey(Path path, EventType eventType) {
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
        try{
            monitor.start();
        }catch (Exception e){
            e.printStackTrace();
        }
        return subject;
    }

    public Optional<WatchKey> registerWatchedDirectory(Path watchedPath){
        return Optional.empty();
    }
}
