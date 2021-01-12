package org.agh.diskstalker.model;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import org.agh.diskstalker.events.eventProcessor.EventProcessor;
import org.agh.diskstalker.events.eventProcessor.IEventProcessor;
import org.agh.diskstalker.events.filesystemEvents.FilesystemEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderErrorEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderRootAvailableEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderSizeChangedEvent;
import org.agh.diskstalker.filesystem.dirwatcher.DirWatcher;
import org.agh.diskstalker.filesystem.dirwatcher.IFilesystemWatcher;
import org.agh.diskstalker.filesystem.scanner.FileTreeScanner;
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
    private long maximumSize;
    private final SimpleBooleanProperty sizeExceededProperty = new SimpleBooleanProperty();
    private final PublishSubject<ObservedFolderEvent> eventStream = PublishSubject.create();
    private final SimpleStringProperty name;
    private final FilesTypeStatistics filesTypeStatistics;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ObservedFolder(Path dirToWatch, long maxSize) {
        this.dirToWatch = dirToWatch;
        this.filesystemWatcher = new DirWatcher(dirToWatch);
        this.treeBuilder = new TreeBuilder();
        this.filesTypeStatistics = new FilesTypeStatistics(treeBuilder.getPathToTreeMap());
        this.eventProcessor = new EventProcessor(treeBuilder, filesTypeStatistics);
        setMaximumSize(maxSize);
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
        var rootDisposable = treeBuilder.getRoot().subscribe(node -> {
            eventStream.onNext(new ObservedFolderRootAvailableEvent(this, node));
        });

        var scanDisposable = new FileTreeScanner(dirToWatch)
                .scan()
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(
                        treeBuilder::processNodeData,
                        this::errorHandler,
                        this::startMonitoring
                );

        compositeDisposable.addAll(rootDisposable, scanDisposable);
    }

    private void processEvent(FilesystemEvent event) {
        eventProcessor.processEvent(event);
        sendSizeChangedEvent();
    }

    private void sendSizeChangedEvent() {
        eventStream.onNext(new ObservedFolderSizeChangedEvent(this));
    }

    private void startMonitoring() {
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

    public void destroy() {
        compositeDisposable.dispose();
        filesystemWatcher.stop();
        eventStream.onComplete();
    }

    public boolean containsNode(Path path) {
        return treeBuilder.containsNode(path);
    }

    public Path getPath() {
        return dirToWatch;
    }

    public void setMaximumSize(long value) {
        maximumSize = value;
        sendSizeChangedEvent(); // force check size check
    }

    public Long getMaximumSize() {
        return maximumSize;
    }

    public Long getSize() {
        return Optional.ofNullable(treeBuilder.getRoot().getValue())
                .map(rootNode -> rootNode.getValue().getSize())
                .orElse(0L);
    }

    public boolean isSizeLimitExceeded() {
        var maxSize = this.getMaximumSize();
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
        if (!filesTypeStatistics.isStatisticsSet()) {
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
