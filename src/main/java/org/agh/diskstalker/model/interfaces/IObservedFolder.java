package org.agh.diskstalker.model.interfaces;

import io.reactivex.rxjava3.subjects.PublishSubject;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderEvent;
import org.agh.diskstalker.model.tree.NodesTree;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;

public interface IObservedFolder {
    void scan();

    void destroy();

    boolean containsNode(Path path);

    TreeFileNode getNodeByPath(Path path);

    PublishSubject<ObservedFolderEvent> getEventStream();

    NodesTree getNodesTree();

    Path getPath();

    String getName();

    boolean isScanning();
}
