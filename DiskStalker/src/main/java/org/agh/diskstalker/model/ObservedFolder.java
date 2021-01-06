package org.agh.diskstalker.model;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import org.agh.diskstalker.eventProcessor.EventProcessor;
import org.agh.diskstalker.eventProcessor.IEventProcessor;
import org.agh.diskstalker.filesystem.dirwatcher.DirWatcher;
import org.agh.diskstalker.filesystem.dirwatcher.IFilesystemWatcher;
import org.agh.diskstalker.filesystem.scanner.FileTreeScanner;
import org.agh.diskstalker.model.events.filesystemEvents.FilesystemEvent;
import org.agh.diskstalker.model.events.observedFolderEvents.ObservedFolderErrorEvent;
import org.agh.diskstalker.model.events.observedFolderEvents.ObservedFolderEvent;
import org.agh.diskstalker.model.events.observedFolderEvents.ObservedFolderRootAvailableEvent;
import org.agh.diskstalker.model.events.observedFolderEvents.ObservedFolderSizeChangedEvent;
import org.agh.diskstalker.model.statisctics.FilesTypeStatistics;
import org.agh.diskstalker.model.tree.TreeBuilder;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;


public class ObservedFolder {
    private final Path dirToWatch;
    private final IFilesystemWatcher filesystemWatcher;
    private final IEventProcessor eventProcessor;
    private final TreeBuilder treeBuilder;
    private final SimpleLongProperty maximumSizeProperty = new SimpleLongProperty(0); //TODO: this might be just long
    private final SimpleBooleanProperty sizeExceededProperty = new SimpleBooleanProperty();
    private final PublishSubject<ObservedFolderEvent> eventStream = PublishSubject.create();
    private final SimpleStringProperty name;
    private final FilesTypeStatistics filesTypeStatistics;

    public ObservedFolder(Path dirToWatch, long maxSize) {
        this.dirToWatch = dirToWatch;
        this.filesystemWatcher = new DirWatcher(dirToWatch);
        this.treeBuilder = new TreeBuilder();
        this.filesTypeStatistics = new FilesTypeStatistics(treeBuilder.getPathToTreeMap());
        this.eventProcessor = new EventProcessor(treeBuilder, filesTypeStatistics);
        setMaximumSizeProperty(maxSize);
        this.sizeExceededProperty.set(false);
        this.name = new SimpleStringProperty(dirToWatch.getFileName().toString());

        scanDirectory();
    }

    public ObservedFolder(Path dirToWatch) {
        this(dirToWatch, 0);
    }

    private void errorHandler(Throwable t) {
        t.printStackTrace();
        eventStream.onNext(new ObservedFolderErrorEvent(this, t.getClass().getCanonicalName()));
    }

    private void scanDirectory() {
        treeBuilder.getRoot().subscribe(node -> {
            eventStream.onNext(new ObservedFolderRootAvailableEvent(this, node));
        });

        new FileTreeScanner(dirToWatch)
                .scan()
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(
                        treeBuilder::processNodeData,
                        this::errorHandler,
                        this::startMonitoring
                );
    }

    private void processEvent(FilesystemEvent event) {
        eventProcessor.processEvent(event);
        sendSizeChangedEvent();
    }

    private void sendSizeChangedEvent() {
        eventStream.onNext(new ObservedFolderSizeChangedEvent(this));
    }

    private void startMonitoring() {
        filesystemWatcher
                .start()
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(
                        this::processEvent,
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
        sendSizeChangedEvent(); // force check size check
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

    public SimpleBooleanProperty getSizeExceededProperty() {
        return sizeExceededProperty;
    }

    public void setSizeExceeded(boolean sizeExceededFlag) {
        sizeExceededProperty.set(sizeExceededFlag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObservedFolder that = (ObservedFolder) o;
        return Objects.equals(dirToWatch, that.dirToWatch);
    }

    public String getName() {
        return name.get();
    }

    public void createTypeStatistics() {
        if(!filesTypeStatistics.isStatisticsSet()) {
            filesTypeStatistics.setTypeStatistics();
        }
    }

    public void createDateModificationStatistics() {
        treeBuilder.getPathToTreeMap().forEach((key, val) -> val.getValue().setModificationDate());
    }

    public FilesTypeStatistics getFilesTypeStatistics() {
        return filesTypeStatistics;
    }

    public TreeBuilder getTreeBuilder() {
        return treeBuilder;
    }
}
