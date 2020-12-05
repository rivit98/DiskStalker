package filesystem;

import io.reactivex.rxjava3.core.Observable;
import model.FileData;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileTreeScanner {
    public Observable<FileData> scanDirectory(Path dirPath) {
        return Observable.create(emitter -> {
            Files.walkFileTree(dirPath, new FileVisitorEmitter(emitter));
            emitter.onComplete();
        });
    }
}
