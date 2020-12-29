package org.agh.diskstalker.filesystem.scanner;

import io.reactivex.rxjava3.core.ObservableEmitter;
import org.agh.diskstalker.model.NodeData;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

public class FileVisitorEmitter extends SimpleFileVisitor<Path> {
    private final ObservableEmitter<NodeData> observer;


    public FileVisitorEmitter(ObservableEmitter<NodeData> observer) {
        this.observer = observer;
    }

    public void emitPath(Path path) {
        if (observer.isDisposed()) {
            return;
        }

        observer.onNext(new NodeData(path));
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
        return SKIP_SUBTREE;
    }
}