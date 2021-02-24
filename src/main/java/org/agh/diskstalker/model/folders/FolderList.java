package org.agh.diskstalker.model.folders;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.scene.control.TreeItem;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;
import org.agh.diskstalker.model.tree.NodeData;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class FolderList extends ObservableListWrapper<ILimitableObservableFolder> {
    public FolderList() {
        super(new ArrayList<>());
    }

    public Optional<ILimitableObservableFolder> getObservedFolderFromTreePath(Path searchedPath) {
        return stream()
                .filter(observedFolder -> observedFolder.containsNode(searchedPath))
                .findFirst();
    }

    public Optional<ILimitableObservableFolder> getObservedFolderFromTreeItem(TreeItem<NodeData> treeItem) {
        return Optional.ofNullable(treeItem)
                .flatMap(item -> getObservedFolderFromTreePath(item.getValue().getPath()));
    }
}
