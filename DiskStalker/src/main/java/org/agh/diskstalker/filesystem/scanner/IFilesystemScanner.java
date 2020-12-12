package org.agh.diskstalker.filesystem.scanner;

import io.reactivex.rxjava3.core.Observable;
import org.agh.diskstalker.model.FileData;

import java.nio.file.Path;

public interface IFilesystemScanner {
    Observable<FileData> scan(Path dirPath);
}
