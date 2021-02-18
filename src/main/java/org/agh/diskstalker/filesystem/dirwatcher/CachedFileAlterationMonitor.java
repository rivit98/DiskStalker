package org.agh.diskstalker.filesystem.dirwatcher;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.util.concurrent.TimeUnit;

/*
 This is basically reimplemented FileAlterationMonitor from Apache commons IO.
*/

@Slf4j
public class CachedFileAlterationMonitor {
    private final long interval;
    private final FileAlterationObserver observer;
    private final Runnable task;
    private boolean running = false;

    public CachedFileAlterationMonitor(long interval, FileAlterationObserver observer) {
        this.interval = interval;
        this.observer = observer;
        this.task = observer::checkAndNotify;
    }

    public Observable<Object> initScan() {
        Runnable initTask = () -> {
            try {
                observer.initialize();
            } catch (Exception e) {
                log.error("observer initialize " + e.getMessage());
            }
        };

        return Completable.fromRunnable(initTask)
                .toObservable()
                .subscribeOn(Schedulers.io());
    }

    public Observable<Long> start() {
        if (running) {
            throw new IllegalStateException("Monitor is already running");
        }
        running = true;

        return Observable.concat(
                Observable.timer(interval, TimeUnit.MILLISECONDS),
                Completable.fromRunnable(task).subscribeOn(Schedulers.io()).toObservable()
        ).repeat();
    }

    public void stop() {
        if (!running) {
            log.info("Monitor is not running");
            return;
        }
        try {
            observer.destroy();
        } catch (Exception ignored) {
            log.warn("Cannot stop DirWatcher");
        }
    }
}
