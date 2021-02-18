package org.agh.diskstalker.filesystem.scanner;

import io.reactivex.rxjava3.core.Observable;
import org.agh.diskstalker.model.tree.NodeData;

public interface IFilesystemScanner {
    Observable<NodeData> scan();
}
