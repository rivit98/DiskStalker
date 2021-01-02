package org.agh.diskstalker.filesystem.scanner;

import io.reactivex.rxjava3.core.Observable;
import org.agh.diskstalker.model.NodeData;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileTreeScanner implements IFilesystemScanner {
    private final Path dirPath;

    public FileTreeScanner(Path dirPath) {
        this.dirPath = dirPath;
    }

    @Override
    public Observable<NodeData> scan() {
        return Observable.create(emitter -> {
            Files.walkFileTree(dirPath, new FileVisitorEmitter(emitter));
            emitter.onComplete();
        });
    }
}
