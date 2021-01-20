package org.agh.diskstalker.controllers.listeners;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;

public class MaxFilesAmountListener implements ChangeListener<TreeItem<NodeData>> {

    private final TextField maxFilesAmountField;
    private final FolderList folderList;

    public MaxFilesAmountListener(TextField maxFilesAmountField, FolderList folderList) {
        this.maxFilesAmountField = maxFilesAmountField;
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
                    maxFilesAmountField.setText(String.valueOf(newObservedFolder.getLimits().getFilesAmountLimit()))
            );
        }
    }
}
