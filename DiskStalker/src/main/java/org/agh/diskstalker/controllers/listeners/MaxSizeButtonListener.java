package org.agh.diskstalker.controllers.listeners;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;
import org.apache.commons.io.FileUtils;

public class MaxSizeButtonListener implements ChangeListener<TreeItem<NodeData>> {

    private final TextField maxSizeField;
    private final FolderList folderList;

    public MaxSizeButtonListener(TextField maxSizeField, FolderList folderList) {
        this.maxSizeField = maxSizeField;
        this.folderList = folderList;
    }

    @Override
    public void changed(ObservableValue<? extends TreeItem<NodeData>> observable, TreeItem<NodeData> oldTreeItem, TreeItem<NodeData> newTreeItem) {
        folderList.getObservedFolderFromTreeItem(newTreeItem)
                .ifPresent(newObservedFolder -> {
                    newFolderPresentHandler(newObservedFolder, oldTreeItem);
                });
    }

    private void newFolderPresentHandler(ObservedFolder newObservedFolder, TreeItem<NodeData> oldTreeItem){
        var oldFolder = folderList.getObservedFolderFromTreeItem(oldTreeItem);
        if (oldFolder.isEmpty() || !oldFolder.get().equals(newObservedFolder)) {
            Platform.runLater(() ->
                    maxSizeField.setText(String.valueOf(newObservedFolder.getMaximumSize() / FileUtils.ONE_MB))
            );
        }
    }
}
