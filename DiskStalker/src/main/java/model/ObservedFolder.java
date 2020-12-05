package model;

import filesystem.DirWatcher;
import filesystem.FileTreeScanner;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;
import javafx.scene.control.TreeItem;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.HashMap;


// TODO: CLOSE WATCHSERVICE (OBSERVABLE FOLDER) WHEN DELETING ITEM FROM TREEVIEW OR ****CLOSING APP****

public class ObservedFolder {
    private final HashMap<Path, TreeFileNode> pathToTreeMap = new HashMap<>();
    private final Path dirToWatch;
    private WatchService watchService;
    private TreeBuilder treeBuilder;
    private DirWatcher dirWatcher;
    private EventProcessor eventProcessor;

    public ObservedFolder(Path dirToWatch) throws IOException {
        this.dirToWatch = dirToWatch;

        scan();
    }

    public void initialize() throws IOException {
        watchService = dirToWatch.getFileSystem().newWatchService();
        treeBuilder = new TreeBuilder();
        dirWatcher = new DirWatcher(watchService);
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

        fileData.getEventKey().ifPresent(watchKey -> {
            eventProcessor.addTrackedDirectory(watchKey, fileData.getPath());
        });
    }

    public void startMonitoring() {
        System.out.println("Start monitoring");
        dirWatcher
                .watchForChanges()
                .subscribeOn(Schedulers.io())
//                .observeOn(JavaFxScheduler.platform())
                .subscribe(watchKey -> {
                    eventProcessor
                            .processEvents(watchKey)
                            .forEach(this::processEvent);
                },
                        System.out::println);
    }

    //TODO: if not valid, remove treeitem, remove watchkey from map - is it necessary?
    //TODO: case when user removes root folder!
    //TODO: when updating branch, update parents size!
    //TODO: better idea - use nodemap for inserting - requries updating size in reverse order (bottom-up)

    public void processEvent(EventObject eventObject) {
        Path from = eventObject.getTargetDir();
        var watchEvent = eventObject.getPathWatchEvent();
        var eventType = eventObject.getEventType();
        var path = watchEvent.context();
        var resolvedPath = from.resolve(path);
        System.out.println(eventType.name() + " | context: " + path + "(cnt: " + watchEvent.count() + ")");
        System.out.println("From: " + from);
        System.out.println("Resolved path: " + resolvedPath);

        if (eventType.equals(ENTRY_CREATE)) {
            handleCreateEvent(resolvedPath);
        } else if (eventType.equals(ENTRY_DELETE)) {
            handleDeleteEvent(resolvedPath);
        }else if(eventType.equals(ENTRY_MODIFY)){
            handleModifyEvent(resolvedPath);
        }
        System.out.println("------------");
    }

    private void handleModifyEvent(Path resolvedPath) {
        var modifiedNode = pathToTreeMap.get(resolvedPath);
        var fileData = modifiedNode.getValue(); //TODO: nullptr when copying data into observed folder
        if(fileData.isFile()){
            modifiedNode.updateMe();
        } else {
            //TODO: if directory
            System.out.println("handleModifyEvent directory - NOT IMPLEMENTED");
        }
    }

    private void handleDeleteEvent(Path resolvedPath) {
        var affectedNode = pathToTreeMap.remove(resolvedPath); // this is the folder where something has changed
        var fileData = affectedNode.getValue();
        if(fileData.isFile()){
            fileData.getEventKey().ifPresent(key -> {
                eventProcessor.removeTrackedDirectory(key);
            });
            affectedNode.deleteMe();
        } else {
            eventProcessor.removeTrackedDirectoriesRecursively(affectedNode);
            removeMappedDirsRecursively(affectedNode);
            affectedNode.deleteMe();
        }
    }

    public void handleCreateEvent(Path resolvedPath){
        var newNode = treeBuilder.addItem(new FileData(resolvedPath));
        var fileData = newNode.getValue();
        pathToTreeMap.put(fileData.getPath(), newNode);
        if (fileData.isDirectory()) {
            // new dir created, we have to register watcher for it
            // no need to register this for files
            dirWatcher.registerWatchedDirectory(fileData.getPath()).ifPresent(watchKey -> {
                eventProcessor.addTrackedDirectory(watchKey, fileData.getPath());
            });
        }
    }

    public void refresh() throws IOException { //TODO: test this!
        cleanup();
        scan();
    }

    public void scan() throws IOException {
        initialize();
        scanDirectory();
    }

    public void destroy() {
        cleanup();
    }

    public void cleanup() {
        try {
            dirWatcher.stop();
            watchService.close();
            eventProcessor.clearTrackedDirectories();
        } catch (IOException exception) {
            exception.printStackTrace(); //TODO: what should we do here? :/
        }
    }

    public SingleSubject<TreeFileNode> getTree() {
        return treeBuilder.getRoot();
    }

    public boolean containsNode(Path path){
        return pathToTreeMap.containsKey(path);
    }

    public void removeMappedDirsRecursively(TreeItem<FileData> node) {
        System.out.println("pathMap remove: " + node.getValue().getPath());
        pathToTreeMap.remove(node.getValue().getPath());
        node.getChildren().forEach(this::removeMappedDirsRecursively);
    }
}
