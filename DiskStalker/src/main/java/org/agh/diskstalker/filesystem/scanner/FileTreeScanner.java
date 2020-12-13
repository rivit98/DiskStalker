package org.agh.diskstalker.filesystem.scanner;

import io.reactivex.rxjava3.core.Observable;
import org.agh.diskstalker.model.NodeData;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileTreeScanner implements IFilesystemScanner {
    @Override
    public Observable<NodeData> scan(Path dirPath) {
        return Observable.create(emitter -> {
            Files.walkFileTree(dirPath, new FileVisitorEmitter(emitter));
            emitter.onComplete();
        });
    }
}
