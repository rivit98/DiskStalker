package filesystem;

import io.reactivex.rxjava3.core.ObservableEmitter;
import model.FileData;

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


    public FileVisitorEmitter(ObservableEmitter<FileData> observer, WatchService watchService) {
        this.observer = observer;
        this.watchService = watchService;
    }

    public void emitPath(Path path) {
        if (observer.isDisposed()) {
            return;
        }

        var fileData = new FileData(path.toFile());
        if (fileData.isDirectory()) {
            try {
                var e = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                fileData.setEventKey(e);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        observer.onNext(fileData);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        emitPath(file);
        return CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        emitPath(dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        observer.onError(exc);
        //TODO: AccessDenied handle
        return CONTINUE;
    }
}