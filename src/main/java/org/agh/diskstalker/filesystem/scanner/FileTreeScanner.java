package org.agh.diskstalker.filesystem.scanner;

import io.reactivex.rxjava3.core.Observable;
import org.agh.diskstalker.model.tree.NodeData;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileTreeScanner implements IFilesystemScanner {
    private final Path dirPath;
    private FileVisitorEmitter fileVisitorEmitter;

    public FileTreeScanner(Path dirPath) {
        this.dirPath = dirPath;
    }

    @Override
    public Observable<NodeData> scan() {
        return Observable.create(emitter -> {
            fileVisitorEmitter = new FileVisitorEmitter(emitter);
            Files.walkFileTree(dirPath, fileVisitorEmitter);
            emitter.onComplete();
        });
    }

    public void stop() {
        if (fileVisitorEmitter != null) {
            fileVisitorEmitter.stop();
        }
    }
}
