package filesystem;

import io.reactivex.rxjava3.core.Observable;
import model.FileData;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileTreeScanner {
    private final DirWatcher dirWatcher;

    public FileTreeScanner(DirWatcher dirWatcher) {
        this.dirWatcher = dirWatcher;
    }

    public Observable<FileData> scanDirectory(Path dirPath) {
        return Observable.create(emitter -> {
            Files.walkFileTree(dirPath, new FileVisitorEmitter(emitter, dirWatcher));
            emitter.onComplete();
        });
    }
}
