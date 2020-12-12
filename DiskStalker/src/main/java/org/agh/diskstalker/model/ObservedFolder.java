package org.agh.diskstalker.model;

import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import org.agh.diskstalker.filesystem.dirwatcher.DirWatcher;
import org.agh.diskstalker.filesystem.dirwatcher.IFilesystemWatcher;
import org.agh.diskstalker.filesystem.scanner.FileTreeScanner;
import org.agh.diskstalker.model.events.EventProcessor;
import org.agh.diskstalker.model.events.IEventProcessor;
import org.agh.diskstalker.model.tree.TreeBuilder;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;


public class ObservedFolder {
    private final Path dirToWatch;
    private final IFilesystemWatcher filesystemWatcher;
    private final IEventProcessor eventProcessor;
    private final TreeBuilder treeBuilder;

    public ObservedFolder(Path dirToWatch) {
        this.dirToWatch = dirToWatch;
        this.filesystemWatcher = new DirWatcher(dirToWatch);
        this.treeBuilder = new TreeBuilder();
        this.eventProcessor = new EventProcessor(treeBuilder);
        scanDirectory();
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
        System.out.println("Start monitoring");
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

    public Path getDirToWatch() {
        return this.dirToWatch;
    }
}
