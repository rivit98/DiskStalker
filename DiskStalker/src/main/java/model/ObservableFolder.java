package model;

import filesystemWatcher.FileData;
import filesystemWatcher.FileTreeScanner;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ObservableFolder {
    private final ObservableList<File> files = FXCollections.observableArrayList();
    private final ObservableList<WatchKey> keys = FXCollections.observableArrayList();
    private final HashMap<WatchKey, File> directoryMap = new HashMap<>(); //TODO: remove proper key after deleting node
    private final WatchService watchService; //TODO: remember to close this after deleting node
    //TODO: map File - TreeItem
    private final Path directoryPath;
    private final TreeBuilder treeBuilder;

    public ObservableFolder(Path dirToWatch) throws IOException {
        directoryPath = dirToWatch;
        watchService = dirToWatch.getFileSystem().newWatchService();
        treeBuilder = new TreeBuilder(dirToWatch);

        scanDirectory();
    }

    public void scanDirectory() {
        var scanner = new FileTreeScanner(watchService);
        scanner
                .scanDirectory(directoryPath)
                .zipWith(Observable.interval(300, TimeUnit.MILLISECONDS), (item, notUsed) -> item)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        (FileData fd) -> {
                            directoryMap.put(fd.getEvent(), fd.getFile());
                            treeBuilder.addItem(fd);
//                            files.add(f);
//                            keys.add(wk);
//                            System.out.println(f);
                        },
                        (Throwable e) -> System.out.println(e),
                        () -> System.out.println("Scanning finished")
                );


        // TODO: implement watching system
        // TODO: add new watch item for newly created directories
    }

    public TreeFileNode getTree() {
        return treeBuilder.getRoot();
    }

    public void closeFolder(){
        try {
            watchService.close();
        } catch (IOException exception) {
            exception.printStackTrace(); //TODO: what should we do here? :/
        }
    }
}
