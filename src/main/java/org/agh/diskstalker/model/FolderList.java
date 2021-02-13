package org.agh.diskstalker.model;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.scene.control.TreeItem;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class FolderList extends ObservableListWrapper<ObservedFolder> {
    public FolderList() {
        super(new ArrayList<>());
    }

    public Optional<ObservedFolder> getObservedFolderFromTreePath(Path searchedPath) {
        return stream()
                .filter(observedFolder -> observedFolder.containsNode(searchedPath))
                .findFirst();
    }

    public Optional<ObservedFolder> getObservedFolderFromTreeItem(TreeItem<NodeData> treeItem) {
        return Optional.ofNullable(treeItem)
                .flatMap(item -> getObservedFolderFromTreePath(item.getValue().getPath()));
    }
}
