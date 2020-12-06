package model;

import filesystem.dirwatcher.DirWatcher;
import filesystem.scanner.FileTreeScanner;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;
import javafx.scene.control.TreeItem;
import model.events.EventProcessor;
import model.tree.TreeBuilder;
import model.tree.TreeFileNode;

import java.nio.file.Path;
import java.util.HashMap;


public class ObservedFolder {
    private final HashMap<Path, TreeFileNode> pathToTreeMap = new HashMap<>();
    private final Path dirToWatch;
    private TreeBuilder treeBuilder;
    private DirWatcher dirWatcher;
    private EventProcessor eventProcessor;

    public ObservedFolder(Path dirToWatch) {
        this.dirToWatch = dirToWatch;

        scan();
    }

    public void initialize() {
        treeBuilder = new TreeBuilder();
        dirWatcher = new DirWatcher(dirToWatch);
        eventProcessor = new EventProcessor(this, treeBuilder);
    }

    public void scanDirectory() {
        var scanner = new FileTreeScanner();
        scanner
                .scanDirectory(dirToWatch)
                .subscribeOn(Schedulers.io())
//                .observeOn(JavaFxScheduler.platform())
                .subscribe(this::processFileData,
                        System.out::println,
                        this::startMonitoring
                );
    }

    public void processFileData(FileData fileData) {
        var insertedNode = treeBuilder.addItem(fileData);
        pathToTreeMap.put(fileData.getPath(), insertedNode);
    }

    public void startMonitoring() {
        System.out.println("Start monitoring");
        dirWatcher
                .watchForChanges()
                .subscribeOn(Schedulers.io())
//                .observeOn(JavaFxScheduler.platform())
                .subscribe(eventProcessor::processEvent,
                        System.out::println
                );
    }

    public void scan() {
        initialize();
        scanDirectory();
    }

    public void destroy() {
        cleanup();
    }

    public void cleanup() {
        dirWatcher.stop();
    }

    public SingleSubject<TreeFileNode> getTree() {
        return treeBuilder.getRoot();
    }

    public boolean containsNode(Path path) {
        return pathToTreeMap.containsKey(path);
    }

    public HashMap<Path, TreeFileNode> getPathToTreeMap() {
        return pathToTreeMap;
    }

    public void removeMappedDirsRecursively(TreeItem<FileData> node) {
        System.out.println("pathMap remove: " + node.getValue().getPath());
        pathToTreeMap.remove(node.getValue().getPath());
        node.getChildren().forEach(this::removeMappedDirsRecursively);
    }
}
