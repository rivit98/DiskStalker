package org.agh.diskstalker.model;

import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.beans.property.SimpleLongProperty;
import org.agh.diskstalker.filesystem.dirwatcher.DirWatcher;
import org.agh.diskstalker.filesystem.dirwatcher.IFilesystemWatcher;
import org.agh.diskstalker.filesystem.scanner.FileTreeScanner;
import org.agh.diskstalker.model.events.EventProcessor;
import org.agh.diskstalker.model.events.IEventProcessor;
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
    private final SimpleLongProperty maximumSizeProperty;

    public ObservedFolder(Path dirToWatch) {
        this.dirToWatch = dirToWatch;
        this.filesystemWatcher = new DirWatcher(dirToWatch);
        this.treeBuilder = new TreeBuilder();
        this.eventProcessor = new EventProcessor(treeBuilder);
        this.maximumSizeProperty = new SimpleLongProperty(0);

        scanDirectory();
    }

    public ObservedFolder(Path dirToWatch, long maxSize){
        this(dirToWatch);
        setMaximumSizeProperty(maxSize);
    }

    private void scanDirectory() {
        var scanner = new FileTreeScanner();
        scanner
                .scan(dirToWatch)
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(treeBuilder::processFileData,
                        System.out::println,
                        this::startMonitoring
                );
    }


    private void startMonitoring() {
        filesystemWatcher
                .start()
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(
                        eventProcessor::processEvent,
                        System.out::println
                );
    }

    public void destroy() {
        filesystemWatcher.stop();
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
        return this.maximumSizeProperty;
    }

    public void setMaximumSizeProperty(long maximumSizeProperty) {
        this.maximumSizeProperty.set(maximumSizeProperty);
    }

    public Long getMaximumSize() {
        return maximumSizeProperty.getValue();
    }

    public boolean isSizeLimitExceeded(){
        var maxSize = getMaximumSize();
        return maxSize > 0 &&
                Optional.ofNullable(treeBuilder.getRoot().getValue())
                .map(rootNode -> rootNode.getValue().getSize())
                .orElse(0L) > maxSize;
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
