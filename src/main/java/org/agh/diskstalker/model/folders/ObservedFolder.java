package org.agh.diskstalker.model.folders;

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
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderErrorEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderScanFinishedEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderScanStartedEvent;
import org.agh.diskstalker.filesystem.dirwatcher.DirWatcher;
import org.agh.diskstalker.filesystem.dirwatcher.IFilesystemWatcher;
import org.agh.diskstalker.filesystem.scanner.FileTreeScanner;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;
import org.agh.diskstalker.model.limits.FolderLimits;
import org.agh.diskstalker.model.tree.NodesTree;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.agh.diskstalker.statistics.TypeRecognizer;
import org.agh.diskstalker.statistics.TypeStatistics;
import org.agh.diskstalker.statistics.messages.AbstractRecognizeTypeMessage;
import org.agh.diskstalker.statistics.messages.AddRecognizeTypeMessage;

import java.nio.file.Path;
import java.util.Objects;

@Slf4j
public class ObservedFolder implements ILimitableObservableFolder {
    private static final long pollingInterval = 6000; //ms

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Getter private final TypeStatistics typeStatistics = new TypeStatistics();
    @Getter private final PublishSubject<ObservedFolderEvent> eventStream = PublishSubject.create();
    @Getter private final NodesTree nodesTree = new NodesTree();
    @Getter @Setter private TypeRecognizer typeRecognizer;

    @Getter private final Path path;
    private final IFilesystemWatcher filesystemWatcher;
    private final IEventProcessor eventProcessor;
    private final FileTreeScanner scanner;
    @Getter private final String name;
    @Getter @Setter private FolderLimits limits;
    @Getter private boolean scanning = false;


    public ObservedFolder(Path path) {
        this.path = path;
        this.filesystemWatcher = new DirWatcher(path, pollingInterval);
        this.eventProcessor = new EventProcessor(this);
        this.name = path.getFileName().toString();
        this.scanner = new FileTreeScanner(path);
        this.limits = new FolderLimits(this);
    }

    @Override
    public void scan() {
        log.info("initial scan " + path);
        scanning = true;
        eventStream.onNext(new ObservedFolderScanStartedEvent(this));

        var initScanDisposable = filesystemWatcher
                .initScan()
                .doOnComplete(this::performFullScan)
                .subscribe();

        compositeDisposable.add(initScanDisposable);
    }

    private void performFullScan() {
        log.info("performFullScan " + path);
        var typeRecognizerDisposable = typeRecognizer
                .register(this)
//                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(AbstractRecognizeTypeMessage::doAction);

        var scanDisposable = scanner
                .scan()
                .subscribeOn(Schedulers.io())
                .doOnComplete(this::startMonitoring)
                .subscribe(
                        nodesTree::addNode,
                        this::errorHandler
                );

        compositeDisposable.addAll(scanDisposable, typeRecognizerDisposable);
    }

    private void startMonitoring() {
        log.info("startMonitoring " + path);
        scanning = false;
        eventStream.onNext(new ObservedFolderScanFinishedEvent(this));

        nodesTree.getPathToTreeMap().values()
                .stream()
                .map(TreeItem::getValue)
                .forEach(nodeData -> typeRecognizer.recognize(new AddRecognizeTypeMessage(this, nodeData)));

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
        eventStream.onNext(new ObservedFolderErrorEvent(this, t.getMessage()));
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
        typeRecognizer.unregister(this);
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
    public void emitEvent(ObservedFolderEvent event) {
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
    public long getLargestFileSize() {
        return nodesTree.getLargestFileSize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObservedFolder that = (ObservedFolder) o;
        return Objects.equals(path, that.path);
    }
}
