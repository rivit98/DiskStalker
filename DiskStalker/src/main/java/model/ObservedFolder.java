package model;

import filesystem.DirWatcher;
import filesystem.FileTreeScanner;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

// TODO: CLOSE WATCHSERVICE (OBSERVABLE FOLDER) WHEN DELETING ITEM FROM TREEVIEW OR ****CLOSING APP****

public class ObservedFolder {
    private final HashMap<WatchKey, File> directoryMap = new HashMap<>(); //TODO: remove proper key after deleting node
    private final HashMap<Path, TreeFileNode> nodeMap = new HashMap<>();
    private final Path dirToWatch;
    private WatchService watchService;
    private TreeBuilder treeBuilder;
    private DirWatcher dirWatcher;

    public ObservedFolder(Path dirToWatch) throws IOException {
        this.dirToWatch = dirToWatch;
        this.watchService = dirToWatch.getFileSystem().newWatchService();
        this.treeBuilder = new TreeBuilder(dirToWatch);
        this.dirWatcher = new DirWatcher(watchService);

        scan();
    }

    public void scanDirectory() {
        var scanner = new FileTreeScanner(watchService);
        scanner
                .scanDirectory(dirToWatch)
//                .zipWith(Observable.interval(300, TimeUnit.MILLISECONDS), (item, notUsed) -> item)
                .subscribeOn(Schedulers.io())
//                .observeOn(JavaFxScheduler.platform())
                .buffer(300, TimeUnit.MILLISECONDS)
                .subscribe(list -> list.forEach(this::processFileData),
                        System.out::println,
                        this::startMonitoring
                );
    }

    public void processFileData(FileData fileData) {
        var insertedNode = treeBuilder.addItem(fileData);
        nodeMap.put(fileData.getFile().toPath(), insertedNode);

        fileData.getEvent().ifPresent(watchKey -> {
            directoryMap.put(watchKey, fileData.getFile());
        });
    }

    public void startMonitoring() {
        // TODO: implement watching system
        System.out.println("Start monitoring");
        dirWatcher.watchForChanges()
                .subscribeOn(Schedulers.io())
//                .observeOn(JavaFxScheduler.platform())
                .subscribe(this::processKey);
    }

    public boolean validateEvents(List<WatchEvent<?>> events) {
        for (var event : events) {
            if (event.kind() == OVERFLOW) { //events may be corrupted
                return false;
            }
        }

        return true;
    }

    public void processKey(WatchKey key) {
        if (!directoryMap.containsKey(key)) {
            key.cancel();
            return;
        }

        var triggeredDir = (Path) key.watchable();
        var events = key.pollEvents();
        var eventsValid = validateEvents(events);
        if (eventsValid) {
            for (final WatchEvent<?> event : events) {
                System.out.println(event.kind().name() + " | " + event.context());
                processEvent(triggeredDir, event);
            }
        }

        var valid = key.reset();
        if (!valid) {
            // processEvent should remove this node automatically, because event fires for parent folder
            directoryMap.remove(key);
        }
        //TODO: if not valid, remove treeitem, remove watchkey from map - is it necessary?
        //TODO: case when user removes root folder!
        //TODO: when updating branch, update parents size!
    }

    public void processEvent(Path from, WatchEvent<?> watchEvent) {
        var eventType = watchEvent.kind();
        var path = ((WatchEvent<Path>) watchEvent).context();
        var resolvedPath = from.resolve(path);

        if (eventType.equals(ENTRY_CREATE)) {
            var newNode = treeBuilder.addItem(new FileData(resolvedPath));
            nodeMap.put(newNode.getValue().getPath(), newNode);
            if (newNode.getValue().isDirectory()) { //new dir created, we have to register watcher for it
                //TODO: refactor this
                //TODO: TreeFileNode - add proxy method for getting path
                try {
                    var key = newNode.getValue().getPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    directoryMap.put(key, newNode.getValue().getFile());
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        } else if (eventType.equals(ENTRY_DELETE)) {
            var node = nodeMap.remove(resolvedPath); // this is the folder where something has changed
            node.getParent().getChildren().remove(node);
            //TODO: updateSize!
            //TODO: remove from directoryMap?
        }else if(eventType.equals(ENTRY_MODIFY)){
            var modifiedNode = Optional.ofNullable(nodeMap.get(resolvedPath));
            modifiedNode.ifPresent(node -> {
                if(node.getValue().isFile()){
                    updateParentSize(node, node.getValue().getFile().length() - node.getValue().size());
                } else {
                    //TODO:if directory
                }
            });
        }
    }

    private void updateParentSize(TreeItem<FileData> node, long deltaSize){
        var parentNode = Optional.ofNullable(node.getParent());
        parentNode.ifPresent(parent -> {if(parent.getValue()!=null){
            parent.getValue().modifySize(deltaSize);
            updateParentSize(parent, deltaSize);
            System.out.println(parent.getValue().getPath());
        }
        });
        //TODO:don't know why parent.getValue() can be null ;(
    }


    public void refresh() throws IOException { //TODO: test this!
        cleanup();
        scan();
    }

    public void scan() throws IOException {
        watchService = dirToWatch.getFileSystem().newWatchService();
        treeBuilder = new TreeBuilder(dirToWatch);
        dirWatcher = new DirWatcher(watchService);

        scanDirectory();
    }

    public void destroy() {
        cleanup();
    }

    public void cleanup() {
        try {
            dirWatcher.stop();
            watchService.close();
            directoryMap.clear();
        } catch (IOException exception) {
            exception.printStackTrace(); //TODO: what should we do here? :/
        }
    }

    public TreeFileNode getTree() {
        return treeBuilder.getRoot(); //TODO: after refreshing tree we have to notify main view about change, maybe binding?
    }

    public Path getPath(){
        return dirToWatch;
    }
}
