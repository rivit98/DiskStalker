package org.agh.diskstalker.controllers.listeners;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;

public abstract class AbstractLimitButtonListener implements ChangeListener<TreeItem<NodeData>> {
    protected final TextField textField;
    protected final FolderList folderList;

    public AbstractLimitButtonListener(TextField textField, FolderList folderList) {
        this.textField = textField;
        this.folderList = folderList;
    }

    @Override
    public void changed(ObservableValue<? extends TreeItem<NodeData>> observable, TreeItem<NodeData> oldTreeItem, TreeItem<NodeData> newTreeItem) {
        folderList.getObservedFolderFromTreeItem(newTreeItem)
                .ifPresent(newObservedFolder -> {
                    handle(newObservedFolder, oldTreeItem);
                });
    }

    protected abstract void handle(ObservedFolder newObservedFolder, TreeItem<NodeData> oldTreeItem);
}
