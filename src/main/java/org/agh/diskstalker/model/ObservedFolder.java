package org.agh.diskstalker.model;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.events.eventProcessor.EventProcessor;
import org.agh.diskstalker.events.eventProcessor.IEventProcessor;
import org.agh.diskstalker.events.filesystemEvents.FilesystemEvent;
import org.agh.diskstalker.events.observedFolderEvents.*;
import org.agh.diskstalker.filesystem.dirwatcher.DirWatcher;
import org.agh.diskstalker.filesystem.dirwatcher.IFilesystemWatcher;
import org.agh.diskstalker.filesystem.scanner.FileTreeScanner;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;
import org.agh.diskstalker.model.statisctics.FilesTypeStatistics;
import org.agh.diskstalker.model.tree.NodesTree;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;
import java.util.Objects;

@Slf4j
public class ObservedFolder implements ILimitableObservableFolder {
    private static final long pollingInterval = 4000; //ms

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Getter private final PublishSubject<ObservedFolderEvent> eventStream = PublishSubject.create();
    @Getter private final NodesTree nodesTree = new NodesTree();

    @Getter private final Path path;
    private final IFilesystemWatcher filesystemWatcher;
    private final IEventProcessor eventProcessor;
    private final FileTreeScanner scanner;
    @Getter private final FilesTypeStatistics filesTypeStatistics;
    @Getter private final String name;
    @Getter @Setter private FolderLimits limits;
    @Getter private boolean scanning = false;


    public ObservedFolder(Path path) {
        this.path = path;
        this.filesystemWatcher = new DirWatcher(path, pollingInterval);
        this.filesTypeStatistics = new FilesTypeStatistics(nodesTree.getPathToTreeMap());
        this.eventProcessor = new EventProcessor(nodesTree, filesTypeStatistics);
        this.name = path.getFileName().toString();
        this.scanner = new FileTreeScanner(path);
        this.limits = new FolderLimits(this);
    }

    @Override
    public void scan() {
        scanning = true;
        eventStream.onNext(new ObservedFolderScanStartedEvent(this));

        log.info("Initial scan started " + path);

        var initScanDisposable = filesystemWatcher
                .initScan()
                .doOnComplete(this::performFullScan)
                .subscribe();

        compositeDisposable.add(initScanDisposable);
    }

    private void performFullScan() {
        log.info("performFullScan " + path);

        var scanDisposable = scanner
                .scan()
                .subscribeOn(Schedulers.io())
                .doOnComplete(this::startMonitoring)
                .subscribe(
                        nodesTree::processNodeData,
                        this::errorHandler
                );

        compositeDisposable.add(scanDisposable);
    }

    private void startMonitoring() {
        log.info("startMonitoring " + path);
        scanning = false;
        eventStream.onNext(new ObservedFolderScanFinishedEvent(this, nodesTree.getRoot()));

        var watchDisposable = filesystemWatcher
                .start()
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(
                        this::processEvent,
                        this::errorHandler
                );

        compositeDisposable.add(watchDisposable);
    }

    private void errorHandler(Throwable t) {
        log.error("ObservedFolder error ", t);
        eventStream.onNext(new ObservedFolderErrorEvent(this, t.getClass().getCanonicalName()));
    }

    private void processEvent(FilesystemEvent event) {
        eventProcessor.processEvent(event);
        limits.updateIfNecessary(event);
    }

    @Override
    public void destroy() {
        log.info("Destroying ObservedFolder " + path);
        eventStream.onComplete();
        compositeDisposable.dispose();
        scanner.stop();
        filesystemWatcher.stop();
    }

    @Override
    public boolean containsNode(Path path) {
        return nodesTree.containsNode(path);
    }

    @Override
    public TreeFileNode getNodeByPath(Path path) {
        return nodesTree.getPathToTreeMap().get(path);
    }

    @Override
    public void emitEvent(AbstractObservedFolderEvent event) {
        eventStream.onNext(event);
    }

    @Override
    public long getSize() {
        return nodesTree.getSize();
    }

    @Override
    public long getFilesAmount() {
        return nodesTree.getFilesAmount();
    }

    @Override
    public long getBiggestFileSize() {
        return nodesTree.getBiggestFileSize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObservedFolder that = (ObservedFolder) o;
        return Objects.equals(path, that.path);
    }

    public void createTypeStatistics() {
        if (!filesTypeStatistics.isStatisticsSet()) {
            filesTypeStatistics.setTypeStatistics();
        }
    }
}
