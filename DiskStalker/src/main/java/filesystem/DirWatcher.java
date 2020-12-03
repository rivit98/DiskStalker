package filesystem;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.io.IOException;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class DirWatcher {
    private final WatchService watchService;
    private boolean shutdown = false;
    private final Thread monitor;
    private final PublishSubject<WatchKey> subject = PublishSubject.create();

    public DirWatcher(WatchService watchService) {
        this.watchService = watchService;
        this.monitor = new Thread(this::loop);
    }

    public void stop() {
        shutdown = true;
        monitor.interrupt();
    }

    public Observable<WatchKey> watchForChanges() {
        monitor.start();
        return subject;
    }

    private void emitKey(WatchKey key) {
        subject.onNext(key);
    }

    private WatchKey takeKey() {
        try {
            return watchService.take();
        } catch (InterruptedException exception) {
            return null;
        }
    }

    private void loop() {
        for (;;) {
            var watchKey = takeKey();
            if (watchKey != null) {
                emitKey(watchKey);
            } else { //thread was interrupted, finish work
                if (shutdown) {
                    try {
                        watchService.close();
                    } catch (IOException exception) {
                        //we dont care
                    }
                    break;
                }
            }
        }
    }

}
