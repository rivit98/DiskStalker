package org.agh.diskstalker.filesystem.scanner;

import io.reactivex.rxjava3.core.Observable;
import org.agh.diskstalker.model.NodeData;

import java.nio.file.Path;

public interface IFilesystemScanner {
    Observable<NodeData> scan(Path dirPath);
}
