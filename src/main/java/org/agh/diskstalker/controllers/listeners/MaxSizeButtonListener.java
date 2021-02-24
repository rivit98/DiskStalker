package org.agh.diskstalker.controllers.listeners;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import org.agh.diskstalker.model.folders.FolderList;
import org.agh.diskstalker.model.interfaces.ILimitableFolder;
import org.agh.diskstalker.model.limits.LimitType;
import org.agh.diskstalker.model.tree.NodeData;
import org.apache.commons.io.FileUtils;

public class MaxSizeButtonListener extends AbstractLimitButtonListener {
    public MaxSizeButtonListener(TextField textField, FolderList folderList) {
        super(textField, folderList);
    }

    protected void handle(ILimitableFolder newFolder, TreeItem<NodeData> oldTreeItem) {
        var oldFolder = folderList.getObservedFolderFromTreeItem(oldTreeItem);
        if (oldFolder.isEmpty() || !oldFolder.get().equals(newFolder)) {
            Platform.runLater(() ->
                    textField.setText(String.valueOf(newFolder.getLimits().get(LimitType.TOTAL_SIZE) / FileUtils.ONE_MB))
            );
        }
    }
}
