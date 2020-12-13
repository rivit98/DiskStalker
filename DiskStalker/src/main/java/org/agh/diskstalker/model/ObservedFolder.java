package org.agh.diskstalker.model;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.beans.property.SimpleLongProperty;
import org.agh.diskstalker.filesystem.dirwatcher.DirWatcher;
import org.agh.diskstalker.filesystem.dirwatcher.IFilesystemWatcher;
import org.agh.diskstalker.filesystem.scanner.FileTreeScanner;
import org.agh.diskstalker.model.events.*;
import org.agh.diskstalker.model.tree.TreeBuilder;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;


public class ObservedFolder {
    private final Path dirToWatch;
    private final IFilesystemWatcher filesystemWatcher;
    private final IEventProcessor eventProcessor;
    private final TreeBuilder treeBuilder;
    private final SimpleLongProperty maximumSizeProperty = new SimpleLongProperty(0);
    ;
    private final PublishSubject<FolderEvent> eventStream = PublishSubject.create();

    public ObservedFolder(Path dirToWatch, long maxSize) {
        this.dirToWatch = dirToWatch;
        this.filesystemWatcher = new DirWatcher(dirToWatch);
        this.treeBuilder = new TreeBuilder();
        this.eventProcessor = new EventProcessor(treeBuilder);
        setMaximumSizeProperty(maxSize);

        scanDirectory();
    }

    public ObservedFolder(Path dirToWatch) {
        this(dirToWatch, 0);
    }

    private void errorHandler(Throwable t) {
        eventStream.onNext(new FolderEvent(FolderEventType.ERROR, t.getClass().getCanonicalName()));
    }

    private void scanDirectory() {
        var scanner = new FileTreeScanner();
        scanner
                .scan(dirToWatch)
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(treeBuilder::processnodeData,
                        this::errorHandler,
                        this::startMonitoring
                );
    }

    private void processEvent(FilesystemEvent event){
        eventProcessor.processEvent(event);
        if (event.isModifyEvent() || event.isCreateEvent()) {
            eventStream.onNext(new FolderEvent(FolderEventType.SIZE_CHANGED));
        }
    }

    private void startMonitoring() {
        filesystemWatcher
                .start()
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(this::processEvent, //TODO: how to do this better only with eventsprocessor
                        this::errorHandler
                );
    }

    public void destroy() {
        filesystemWatcher.stop();
        eventStream.onComplete();
    }

    public SingleSubject<TreeFileNode> getTree() {
        return treeBuilder.getRoot();
    }

    public boolean containsNode(Path path) {
        return treeBuilder.containsNode(path);
    }

    public Path getPath() {
        return dirToWatch;
    }

    public SimpleLongProperty getMaximumSizeProperty() {
        return maximumSizeProperty;
    }

    public void setMaximumSizeProperty(long value) {
        maximumSizeProperty.set(value);
    }

    public Long getMaximumSize() {
        return maximumSizeProperty.getValue();
    }

    public Long getSize() {
        return Optional.ofNullable(treeBuilder.getRoot().getValue())
                .map(rootNode -> rootNode.getValue().getSize())
                .orElse(0L);
    }

    public boolean isSizeLimitExceeded() {
        var maxSize = getMaximumSize();
        return maxSize > 0 && getSize() > maxSize;
    }

    public Observable<FolderEvent> getEventStream() {
        return eventStream;
    }

    @Override
    public String toString() {
        return dirToWatch.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObservedFolder that = (ObservedFolder) o;
        return Objects.equals(dirToWatch, that.dirToWatch);
    }
}
