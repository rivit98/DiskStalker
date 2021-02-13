package org.agh.diskstalker.model;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.events.eventProcessor.EventProcessor;
import org.agh.diskstalker.events.eventProcessor.IEventProcessor;
import org.agh.diskstalker.events.filesystemEvents.FilesystemEvent;
import org.agh.diskstalker.events.observedFolderEvents.AbstractObservedFolderEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderErrorEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderRootAvailableEvent;
import org.agh.diskstalker.filesystem.dirwatcher.DirWatcher;
import org.agh.diskstalker.filesystem.dirwatcher.IFilesystemWatcher;
import org.agh.diskstalker.filesystem.scanner.FileTreeScanner;
import org.agh.diskstalker.model.statisctics.FilesTypeStatistics;
import org.agh.diskstalker.model.tree.TreeBuilder;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class ObservedFolder {
    private static final long pollingInterval = 2000; //ms

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final IFilesystemWatcher filesystemWatcher;
    private final IEventProcessor eventProcessor;
    private final FileTreeScanner scanner;

    @Getter
    private final Path path;
    @Getter
    private final FilesTypeStatistics filesTypeStatistics;
    @Getter
    private final PublishSubject<ObservedFolderEvent> eventStream = PublishSubject.create();
    @Getter
    private final TreeBuilder treeBuilder;
    @Getter
    private final String name;
    @Getter @Setter
    private FolderLimits limits;


    public ObservedFolder(Path path) {
        this.path = path;
        this.filesystemWatcher = new DirWatcher(path);
        this.treeBuilder = new TreeBuilder();
        this.filesTypeStatistics = new FilesTypeStatistics(treeBuilder.getPathToTreeMap());
        this.eventProcessor = new EventProcessor(treeBuilder, filesTypeStatistics);
        this.name = path.getFileName().toString();
        this.scanner = new FileTreeScanner(path);
        this.limits = new FolderLimits(this);

        scanDirectory();
    }

    private void errorHandler(Throwable t) {
        log.error("ObservedFolder Error", t);
        eventStream.onNext(new ObservedFolderErrorEvent(this, t.getClass().getCanonicalName()));
    }

    private void scanDirectory() {
        var rootDisposable = treeBuilder.getRootSubject().subscribe(node -> {
            eventStream.onNext(new ObservedFolderRootAvailableEvent(this, node));
        });

        var scanDisposable = scanner
                .scan()
                .subscribeOn(Schedulers.io())
                .doOnComplete(this::startMonitoring)
                .observeOn(JavaFxScheduler.platform())
                .subscribe(
                        treeBuilder::processNodeData,
                        this::errorHandler
//                        ,this::startMonitoring
                );

        compositeDisposable.addAll(rootDisposable, scanDisposable);
    }

    private void processEvent(FilesystemEvent event) {
        eventProcessor.processEvent(event);
        limits.updateIfNecessary(event);
    }

    public void sendEvent(AbstractObservedFolderEvent event){
        eventStream.onNext(event);
    }

    private void startMonitoring() {
        var watchDisposable = filesystemWatcher
                .start(pollingInterval)
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(
                        this::processEvent,
                        this::errorHandler
                );

        compositeDisposable.add(watchDisposable);
    }

    public void destroy() {
        compositeDisposable.dispose();
        scanner.stop();
        filesystemWatcher.stop(); //FIXME: this takes quite much time
        eventStream.onComplete();
    }

    public boolean containsNode(Path path) {
        return treeBuilder.containsNode(path);
    }

    public TreeFileNode getNodeByPath(Path path){
        return treeBuilder.getPathToTreeMap().get(path);
    }

    public long getSize() {
        return Optional.ofNullable(treeBuilder.getRoot())
                .map(rootNode -> rootNode.getValue().getAccumulatedSize())
                .orElse(0L);
    }

    public long getFilesAmount() {
        return treeBuilder.getPathToTreeMap().values().stream()
                .filter(node -> !node.getValue().isDirectory())
                .count();
    }

    public long getBiggestFileSize(){
        return treeBuilder.getPathToTreeMap().values().stream()
                .map(TreeItem::getValue)
                .filter(NodeData::isFile)
                .map(NodeData::getAccumulatedSize)
                .max(Long::compare)
                .orElse(0L);
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
