package org.agh.diskstalker.controllers.listeners;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;

public class MaxFilesAmountListener extends AbstractLimitButtonListener {
    public MaxFilesAmountListener(TextField textField, FolderList folderList) {
        super(textField, folderList);
    }

    protected void handle(ObservedFolder newObservedFolder, TreeItem<NodeData> oldTreeItem){
        var oldFolder = folderList.getObservedFolderFromTreeItem(oldTreeItem);
        if (oldFolder.isEmpty() || !oldFolder.get().equals(newObservedFolder)) {
            Platform.runLater(() ->
                    textField.setText(String.valueOf(newObservedFolder.getLimits().getFilesAmountLimit()))
            );
        }
    }
}
