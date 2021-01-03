package org.agh.diskstalker.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.nio.file.Path;
import java.util.Optional;

public class FolderList {
    private final ObservableList<ObservedFolder> folderList = FXCollections.observableArrayList();

    public ObservableList<ObservedFolder> get(){
        return folderList;
    }

    public Optional<ObservedFolder> getObservedFolderFromTreePath(Path searchedPath) {
        return folderList.stream()
                .filter(observedFolder -> observedFolder.containsNode(searchedPath))
                .findFirst();
    }

//    public Optional<ObservedFolder> getObservedFolderFromSelection() {
//        return Optional.ofNullable(locationTreeView.getSelectionModel().getSelectedItem())
//                .flatMap(item -> getObservedFolderFromTreePath(item.getValue().getPath()));
//    }

    public Optional<ObservedFolder> getObservedFolderFromTreeItem(TreeItem<NodeData> treeItem) {
        return Optional.ofNullable(treeItem)
                .flatMap(item -> getObservedFolderFromTreePath(item.getValue().getPath()));
    }
}
