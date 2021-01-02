package org.agh.diskstalker.model.events;

import org.agh.diskstalker.controllers.MainView;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.tree.TreeFileNode;

public class ObservedFolderRootAvailableEvent extends AbstractObservedFolderEvent{
    private final TreeFileNode nodeDataTreeItem;

    public ObservedFolderRootAvailableEvent(ObservedFolder folder, TreeFileNode treeItem) {
        super(folder);
        nodeDataTreeItem = treeItem;
    }

    @Override
    public void dispatch(MainView view) {
        view.addToMainTree(folder, nodeDataTreeItem);
    }
}
