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
import org.agh.diskstalker.model.statisctics.FilesTypeStatistics;
import org.agh.diskstalker.model.tree.NodesTree;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;
import java.util.Objects;

@Slf4j
@Getter
public class ObservedFolder {
    private static final long pollingInterval = 2000; //ms

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final PublishSubject<ObservedFolderEvent> eventStream = PublishSubject.create();
    private final NodesTree nodesTree = new NodesTree();

    private final Path path;
    private final IFilesystemWatcher filesystemWatcher;
    private final IEventProcessor eventProcessor;
    private final FileTreeScanner scanner;
    private final FilesTypeStatistics filesTypeStatistics;
    private final String name;
    @Setter
    private FolderLimits limits;
    private boolean scanning = false;


    public ObservedFolder(Path path) {
        this.path = path;
        this.filesystemWatcher = new DirWatcher(path);
        this.filesTypeStatistics = new FilesTypeStatistics(nodesTree.getPathToTreeMap());
        this.eventProcessor = new EventProcessor(nodesTree, filesTypeStatistics);
        this.name = path.getFileName().toString();
        this.scanner = new FileTreeScanner(path);
        this.limits = new FolderLimits(this);
    }

    private void errorHandler(Throwable t) {
        log.error("ObservedFolder Error ", t);
        eventStream.onNext(new ObservedFolderErrorEvent(this, t.getClass().getCanonicalName()));
    }

    public void scanDirectory() {
        scanning = true;
        System.out.println("scanDirectory" + Thread.currentThread());

        eventStream.onNext(new ObservedFolderScanStartedEvent(this));

        var scanDisposable = scanner
                .scan()
                .subscribeOn(Schedulers.io())
                .doOnComplete(this::startMonitoring)
//                .observeOn(JavaFxScheduler.platform())
                .subscribe(
                        nodesTree::processNodeData,
                        this::errorHandler
                );

        compositeDisposable.add(scanDisposable);
    }

    private void processEvent(FilesystemEvent event) {
        System.out.println("processEvent " + Thread.currentThread());

        eventProcessor.processEvent(event);
        limits.updateIfNecessary(event);
    }

    public void sendEvent(AbstractObservedFolderEvent event){
        eventStream.onNext(event);
    }

    private void startMonitoring() {
        scanning = false;
        System.out.println("startMonitoring" + Thread.currentThread());
        eventStream.onNext(new ObservedFolderScanFinishedEvent(this, nodesTree.getRoot()));

        var watchDisposable = filesystemWatcher
                .start(pollingInterval)
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(
                        this::processEvent,
                        this::errorHandler
                );

        compositeDisposable.addAll(watchDisposable);
    }

    public void destroy() {
        compositeDisposable.dispose();
        scanner.stop();
        filesystemWatcher.stop(); //FIXME: this takes quite much time (onAppExit)
        eventStream.onComplete();
    }

    public boolean containsNode(Path path) {
        return nodesTree.containsNode(path);
    }

    public TreeFileNode getNodeByPath(Path path){
        return nodesTree.getPathToTreeMap().get(path);
    }

    public long getSize() {
        return nodesTree.getSize();
    }

    public long getFilesAmount() {
        return nodesTree.getFilesAmount();
    }

    public long getBiggestFileSize(){
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
