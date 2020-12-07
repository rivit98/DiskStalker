package model;

import filesystem.dirwatcher.DirWatcher;
import filesystem.dirwatcher.IFilesystemWatcher;
import filesystem.scanner.FileTreeScanner;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;
import javafx.scene.control.TreeItem;
import model.events.EventProcessor;
import model.events.IEventProcessor;
import model.tree.TreeBuilder;
import model.tree.TreeFileNode;

import java.nio.file.Path;
import java.util.HashMap;


public class ObservedFolder {
    private final HashMap<Path, TreeFileNode> pathToTreeMap = new HashMap<>();
    private final Path dirToWatch;
    private final TreeBuilder treeBuilder;
    private final IFilesystemWatcher filesystemWatcher;
    private final IEventProcessor eventProcessor;
    private TreeFileNode root;

    public ObservedFolder(Path dirToWatch) {
        this.dirToWatch = dirToWatch;

        treeBuilder = new TreeBuilder();
        filesystemWatcher = new DirWatcher(dirToWatch);
        eventProcessor = new EventProcessor(this);
        scanDirectory();
    }

    private void scanDirectory() {
        var scanner = new FileTreeScanner();
        scanner
                .scan(dirToWatch)
                .subscribeOn(Schedulers.io())
//                .observeOn(JavaFxScheduler.platform())
                .subscribe(this::processFileData,
                        System.out::println,
                        this::startMonitoring
                );
    }

    private void processFileData(FileData fileData) {
        var insertedNode = treeBuilder.addItem(fileData);
        pathToTreeMap.put(fileData.getPath(), insertedNode);
    }

    private void startMonitoring() {
        System.out.println("Start monitoring");
        filesystemWatcher
                .start()
                .subscribeOn(Schedulers.io())
//                .observeOn(JavaFxScheduler.platform())
                .subscribe(eventProcessor::processEvent,
                        System.out::println
                );
    }

    public void destroy() {
        filesystemWatcher.stop();
    }

    public SingleSubject<TreeFileNode> getTree() {
        var observableRoot = treeBuilder.getRoot();
        observableRoot.subscribe(node -> root = node);
        return observableRoot;
    }

    public boolean containsNode(Path path) {
        return pathToTreeMap.containsKey(path);
    }

    public HashMap<Path, TreeFileNode> getPathToTreeMap() {
        return pathToTreeMap;
    }

    public void removeMappedDirsRecursively(TreeItem<FileData> node) {
        removeMappedDirs(node);
        node.getChildren().forEach(this::removeMappedDirsRecursively);
    }

    public void removeMappedDirs(TreeItem<FileData> node) {
        pathToTreeMap.remove(node.getValue().getPath());
    }
}
