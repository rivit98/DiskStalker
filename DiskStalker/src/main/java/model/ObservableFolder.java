package model;

import filesystem.DirWatcher;
import filesystem.FileTreeScanner;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;

// TODO: CLOSE WATCHSERVICE (OBSERVABLE FOLDER) WHEN DELETING ITEM FROM TREEVIEW OR ****CLOSING APP****

public class ObservableFolder {
    private final ObservableList<WatchKey> keys = FXCollections.observableArrayList();
    private final HashMap<WatchKey, File> directoryMap = new HashMap<>(); //TODO: remove proper key after deleting node
    private WatchService watchService; //TODO: remember to close this after deleting node
    //TODO: map File - TreeItem
    private final Path dirToWatch;
    private TreeBuilder treeBuilder;
    private DirWatcher dirWatcher;

    public ObservableFolder(Path dirToWatch) throws IOException {
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
                .subscribe(this::processFileData,
                        System.out::println,
                        this::startMonitoring
                );
    }

    public void processFileData(FileData fileData) {
        directoryMap.put(fileData.getEvent(), fileData.getFile());
        treeBuilder.addItem(fileData);
        keys.add(fileData.getEvent());
        System.out.println(fileData.getFile().getName() + " " +  fileData.getEvent());
    }

    public void startMonitoring(){
        // TODO: implement watching system
        // TODO: add new watch key for newly created directories
        System.out.println("Start monitoring");
        dirWatcher.watchForChanges().subscribe(this::processKey);
    }

    public void processKey(WatchKey key){
        System.out.println(key);
        for (final WatchEvent<?> event : key.pollEvents()) {
            System.out.println(event.kind().name() + " " + event.context() + " count " + event.count());
        }

        var valid = key.reset();
        //TODO: if not valid, remove treeitem, remove watchkey from map
        System.out.println(valid);

        //TODO: removing could be done as getParent.remove(me)
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

    public void cleanup() {
        try {
            dirWatcher.stop();
            watchService.close();
            keys.clear();
            directoryMap.clear();
        } catch (IOException exception) {
            exception.printStackTrace(); //TODO: what should we do here? :/
        }
    }

    public TreeFileNode getTree() {
        return treeBuilder.getRoot(); //TODO: after refreshing tree we have to notify main view about change, maybe binding?
    }
}
