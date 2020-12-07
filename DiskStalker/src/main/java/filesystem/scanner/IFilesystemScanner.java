package filesystem.scanner;

import io.reactivex.rxjava3.core.Observable;
import model.FileData;

import java.nio.file.Path;

public interface IFilesystemScanner {
    Observable<FileData> scan(Path dirPath);
}
