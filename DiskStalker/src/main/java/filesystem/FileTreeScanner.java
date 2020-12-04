package filesystem;

import io.reactivex.rxjava3.core.Observable;
import model.FileData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchService;

public class FileTreeScanner {
    private final WatchService watchService;

    public FileTreeScanner(WatchService watchService) {
        this.watchService = watchService;
    }

    public Observable<FileData> scanDirectory(Path dirPath) {
        return Observable.create(emitter -> {
            Files.walkFileTree(dirPath, new FileVisitorEmitter(emitter, watchService));
            emitter.onComplete();
        });
    }
}