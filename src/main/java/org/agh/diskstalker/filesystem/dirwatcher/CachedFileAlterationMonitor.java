package org.agh.diskstalker.filesystem.dirwatcher;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/*
 This is basically reimplemented FileAlterationMonitor from Apache commons IO.
*/

@Slf4j
public class CachedFileAlterationMonitor {
    private final long interval;
    private final List<FileAlterationObserver> observersList = new CopyOnWriteArrayList<>();
    private final Runnable task;
    private boolean running = false;

    public CachedFileAlterationMonitor(long interval) {
        this.interval = interval;
        this.task = () -> {
            for (var observer : observersList) {
                observer.checkAndNotify();
            }
        };
    }

    public Observable<Object> initScan() {
        Runnable initTask = () -> {
            for (var observer : observersList) {
                try {
                    observer.initialize();
                } catch (Exception e) {
                    log.error("observer initialize " + e.getMessage());
                }
            }
        };

        return Completable.fromRunnable(initTask).toObservable().subscribeOn(Schedulers.io());
    }

    public CachedFileAlterationMonitor(long interval, FileAlterationObserver... observers) {
        this(interval);
        if (observers != null) {
            observersList.addAll(Arrays.asList(observers));
        }
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

    public void stop() throws Exception {
        if (!running) {
            log.info("Monitor is not running");
            return;
        }

        for (var observer : observersList) {
            observer.destroy();
        }
    }
}
