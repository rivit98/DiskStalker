package org.agh.diskstalker.filesystem.scanner;

import io.reactivex.rxjava3.core.ObservableEmitter;
import org.agh.diskstalker.model.NodeData;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.*;

public class FileVisitorEmitter extends SimpleFileVisitor<Path> {
    private final ObservableEmitter<NodeData> observer;
    private boolean stopped = false;


    public FileVisitorEmitter(ObservableEmitter<NodeData> observer) {
        this.observer = observer;
    }

    public FileVisitResult emitPath(Path path) {
        System.out.println(path);
        if (observer.isDisposed() || stopped) {
            return TERMINATE;
        }

        observer.onNext(new NodeData(path));
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        return emitPath(file);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return emitPath(dir);
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return SKIP_SUBTREE;
    }

    public void stop() {
        stopped = true;
    }
}