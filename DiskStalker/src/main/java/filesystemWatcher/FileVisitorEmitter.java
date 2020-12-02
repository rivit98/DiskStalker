package filesystemWatcher;

import io.reactivex.rxjava3.core.ObservableEmitter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardWatchEventKinds.*;

public class FileVisitorEmitter extends SimpleFileVisitor<Path> {
    private final ObservableEmitter<FileData> observer;
    private final WatchService watchService;


    public FileVisitorEmitter(Path basePath, ObservableEmitter<FileData> observer, WatchService watchService) {
        this.observer = observer;
        this.watchService = watchService;
    }

    public void emitPath(Path value) {
        if (observer.isDisposed()) {
            return;
        }

        var f = value.toFile();
        var fdata = new FileData(f);
        if (f.isDirectory()) {
            try {
                var e = value.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                fdata.setEvent(e);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        observer.onNext(fdata);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        emitPath(file);
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        emitPath(dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        observer.onError(exc);
        return CONTINUE;
    }
}