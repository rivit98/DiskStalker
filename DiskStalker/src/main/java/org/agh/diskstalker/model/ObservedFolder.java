package org.agh.diskstalker.model;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.beans.property.SimpleBooleanProperty;
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
    private final SimpleBooleanProperty sizeExceededFlag = new SimpleBooleanProperty();
    private final PublishSubject<ObservedFolderEvent> eventStream = PublishSubject.create();

    public ObservedFolder(Path dirToWatch, long maxSize) {
        this.dirToWatch = dirToWatch;
        this.filesystemWatcher = new DirWatcher(dirToWatch);
        this.treeBuilder = new TreeBuilder();
        this.eventProcessor = new EventProcessor(treeBuilder);
        setMaximumSizeProperty(maxSize);
        this.sizeExceededFlag.set(false);

        scanDirectory();
    }

    public ObservedFolder(Path dirToWatch) {
        this(dirToWatch, 0);
    }

    private void errorHandler(Throwable t) {
        eventStream.onNext(new ObservedFolderErrorEvent(this, t.getClass().getCanonicalName()));
    }

    private void scanDirectory() {
        var scanner = new FileTreeScanner();
        scanner
                .scan(dirToWatch)
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(treeBuilder::processNodeData,
                        this::errorHandler,
                        this::startMonitoring
                );

        treeBuilder.getRoot().subscribe(node -> {
            eventStream.onNext(new ObservedFolderRootAvailableEvent(this, node));
        });
    }

    private void processEvent(FilesystemEvent event){
        eventProcessor.processEvent(event);
        if (event.isModifyEvent() || event.isCreateEvent()) {
            sendSizeChangedEvent();
        }
    }

    private void sendSizeChangedEvent(){
        eventStream.onNext(new ObservedFolderSizeChangedEvent(this));
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
        sendSizeChangedEvent();
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

    public Observable<ObservedFolderEvent> getEventStream() {
        return eventStream;
    }

    public SimpleBooleanProperty isSizeExceededFlag() {
        return this.sizeExceededFlag;
    }

    public void setSizeExceededFlag(boolean sizeExceededFlag) {
        this.sizeExceededFlag.set(sizeExceededFlag);
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
