package models;

import filesystemWatcher.FileData;
import filesystemWatcher.FileTreeScanner;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

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
    private final HashMap<WatchKey, File> directoryMap = new HashMap<>();
    private final WatchService watchService;
    private final Path directoryPath;
    private final TreeBuilder<File> treeBuilder;

    public ObservableFolder(Path dirToWatch) throws IOException {
        directoryPath = dirToWatch;
        watchService = dirToWatch.getFileSystem().newWatchService();
        treeBuilder = new TreeBuilder<>(dirToWatch.toFile());

        scanDirectory();
    }

    public void scanDirectory() {
        var scanner = new FileTreeScanner(watchService);
//        scanner
//                .scanDirectory(directoryPath)
//                .zipWith(Observable.interval(2, TimeUnit.SECONDS), (item, notUsed) -> item)
        scanner
                .scanDirectory(directoryPath)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        (FileData fd) -> {
                            var f = fd.getFile();
                            var wk = fd.getEvent();
                            directoryMap.put(wk, f);
                            treeBuilder.addItem(f);
                            files.add(f);
                            keys.add(wk);
                            System.out.println(f);

//                            System.out.println(f);
                            // add to three view, take size?
                            // watch file here, maybe add to watcher in model
                            // ?
                        },
                        (Throwable e) -> System.out.println(e)
                );

        // TODO: implement watching system
        // TODO: add new watch item for newly created directories
    }

    public TreeItem<File> getTree() {
        return treeBuilder.getFilesTree();
    }
}
