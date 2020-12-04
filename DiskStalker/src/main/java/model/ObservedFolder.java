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
import java.util.Optional;


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
        pathToTreeMap.put(fileData.getFile().toPath(), insertedNode);

        fileData.getEventKey().ifPresent(watchKey -> {
            eventProcessor.addTrackedDirectory(watchKey, fileData.getFile());
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
                });
    }

    //TODO: if not valid, remove treeitem, remove watchkey from map - is it necessary?
    //TODO: case when user removes root folder!
    //TODO: when updating branch, update parents size!
    //TODO: better idea - use nodemap for inserting
    //TODO: refactor this
    //TODO: TreeFileNode - add proxy method for getting path

    public void processEvent(EventObject eventObject) {
        Path from = eventObject.getTargetDir();
        var watchEvent = eventObject.getPathWatchEvent();
        var eventType = eventObject.getEventType();
        var path = watchEvent.context();
        var resolvedPath = from.resolve(path);
        System.out.println(eventType.name() + " | context: " + path);
        System.out.println("From: " + from);
        System.out.println("Resolved path: " + resolvedPath);

        if (eventType.equals(ENTRY_CREATE)) {
            handleCreateEvent(resolvedPath);
        } else if (eventType.equals(ENTRY_DELETE)) {
            handleDeleteEvent(resolvedPath);
        }else if(eventType.equals(ENTRY_MODIFY)){
            handleModifyEvent(resolvedPath);
        }else{
            throw new IllegalStateException("invalid event");
        }
        System.out.println("------------");
    }

    private void handleModifyEvent(Path resolvedPath) {
        var modifiedNode = pathToTreeMap.get(resolvedPath);
        var fileData = modifiedNode.getValue();
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
            //TODO: if directory
            //TODO: remove from eventprocessor
            //TODO: remove childs from pathToTreeMap
            //TODO: remove childs from eventprocessor
            System.out.println("handleDeleteEvent directory - NOT IMPLEMENTED");
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
                eventProcessor.addTrackedDirectory(watchKey, fileData.getFile());
            });
        }
    }

    private void updateParentSize(TreeItem<FileData> node, long deltaSize){
        var parentNode = Optional.ofNullable(node.getParent());
        parentNode.ifPresent(parent -> {
            if(parent.getValue()!=null){
            parent.getValue().modifySize(deltaSize);
            updateParentSize(parent, deltaSize);
            }
        });
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
            eventProcessor.getDirectoryMap().clear();
        } catch (IOException exception) {
            exception.printStackTrace(); //TODO: what should we do here? :/
        }
    }

    public SingleSubject<TreeFileNode> getTree() {
        return treeBuilder.getRoot();
    }

    public boolean checkIfNodeIsChild(Path path){
        return pathToTreeMap.get(path) != null;
    }

    public void deleteNodes(TreeItem<FileData> treeItem){
        var itemChildren = treeItem.getChildren().stream();
        itemChildren.forEach(this::deleteNodes);

        eventProcessor.getDirectoryMap().remove(treeItem.getValue().getEventKey());
        pathToTreeMap.remove(treeItem.getValue().getPath());
    }
}
