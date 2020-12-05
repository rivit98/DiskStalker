package model;

import filesystem.DirWatcher;
import filesystem.FileTreeScanner;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;
import javafx.scene.control.TreeItem;

import java.io.IOException;
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
        eventProcessor = new EventProcessor();
    }

    public void scanDirectory() {
        var scanner = new FileTreeScanner(dirWatcher);
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
                .subscribe(this::processEvent,
                        System.out::println
                );
    }

    //TODO: if not valid, remove treeitem, remove watchkey from map - is it necessary?
    //TODO: case when user removes root folder!
    //TODO: when updating branch, update parents size!
    //TODO: better idea - use nodemap for inserting - requries updating size in reverse order (bottom-up)

    public void processEvent(EventObject eventObject) {
        Path resolvedPath = eventObject.getTargetDir();
        var eventType = eventObject.getEventType();
        System.out.println(eventType.name() + " | context: " + resolvedPath);

        switch (eventType) {
            case FILE_CREATED, DIR_CREATED -> handleCreateEvent(resolvedPath);
            case DIR_DELETED -> handleDeleteEventDir(resolvedPath);
            case FILE_DELETED -> handleDeleteEventFile(resolvedPath);
            case FILE_MODIFIED -> handleModifyEventFile(resolvedPath);
            case DIR_MODIFIED -> handleModifyEventDir(resolvedPath);
        }

        System.out.println("------------");
    }

    private void handleModifyEventFile(Path resolvedPath) {
        var modifiedNode = pathToTreeMap.get(resolvedPath);
        modifiedNode.updateMe();
    }

    private void handleModifyEventDir(Path resolvedPath) {
        //TODO
        var modifiedNode = pathToTreeMap.get(resolvedPath);
        System.out.println("handleModifyEvent directory - NOT IMPLEMENTED");
    }

    private void handleDeleteEventFile(Path resolvedPath) {
        var affectedNode = pathToTreeMap.remove(resolvedPath); // this is the folder where something has changed
        var fileData = affectedNode.getValue();
        fileData.getEventKey().ifPresent(key -> {
            eventProcessor.removeTrackedDirectory(key);
        });
        affectedNode.deleteMe();
    }

    private void handleDeleteEventDir(Path resolvedPath) {
        var affectedNode = pathToTreeMap.remove(resolvedPath); // this is the folder where something has changed
        eventProcessor.removeTrackedDirectoriesRecursively(affectedNode);
        removeMappedDirsRecursively(affectedNode);
        affectedNode.deleteMe();
    }

    public void handleCreateEvent(Path resolvedPath) {
        var newNode = treeBuilder.addItem(new FileData(resolvedPath));
        var fileData = newNode.getValue();
        pathToTreeMap.put(fileData.getPath(), newNode);
    }

    public void refresh() throws IOException { //TODO: test this!
        cleanup();
        scan();
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

    public void removeMappedDirsRecursively(TreeItem<FileData> node) {
        System.out.println("pathMap remove: " + node.getValue().getPath());
        pathToTreeMap.remove(node.getValue().getPath());
        node.getChildren().forEach(this::removeMappedDirsRecursively);
    }
}
