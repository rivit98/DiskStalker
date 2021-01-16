package org.agh.diskstalker.model;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
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
    private final SimpleBooleanProperty sizeExceededProperty = new SimpleBooleanProperty(false);
    @Getter
    private final TreeBuilder treeBuilder;
    @Getter
    private final String name;
    @Getter
    private long maximumSize;


    public ObservedFolder(Path path, long maxSize) {
        this.path = path;
        this.filesystemWatcher = new DirWatcher(path);
        this.treeBuilder = new TreeBuilder();
        this.filesTypeStatistics = new FilesTypeStatistics(treeBuilder.getPathToTreeMap());
        this.eventProcessor = new EventProcessor(treeBuilder, filesTypeStatistics);
        setMaximumSize(maxSize);
        this.name = path.getFileName().toString();
        this.scanner = new FileTreeScanner(path);

        scanDirectory();
    }

    public ObservedFolder(Path path) {
        this(path, 0);
    }

    private void errorHandler(Throwable t) {
        t.printStackTrace();
        eventStream.onNext(new ObservedFolderErrorEvent(this, t.getClass().getCanonicalName()));
    }

    private void scanDirectory() {
        var rootDisposable = treeBuilder.getRootSubject().subscribe(node -> {
            eventStream.onNext(new ObservedFolderRootAvailableEvent(this, node));
        });

        var scanDisposable = scanner
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
        scanner.stop();
        filesystemWatcher.stop();
        eventStream.onComplete();
    }

    public boolean containsNode(Path path) {
        return treeBuilder.containsNode(path);
    }

    public void setMaximumSize(long value) {
        maximumSize = value;
        sendSizeChangedEvent(); // force check size check
    }

    public Long getSize() {
        return Optional.ofNullable(treeBuilder.getRootSubject().getValue())
                .map(rootNode -> rootNode.getValue().getSize())
                .orElse(0L);
    }

    public boolean isSizeLimitExceeded() {
        var maxSize = this.getMaximumSize();
        return maxSize > 0 && getSize() > maxSize;
    }

    public void setSizeExceeded(boolean sizeExceededFlag) {
        sizeExceededProperty.set(sizeExceededFlag);
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
