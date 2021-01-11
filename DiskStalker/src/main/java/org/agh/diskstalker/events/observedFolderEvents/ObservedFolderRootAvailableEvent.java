package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainViewController;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.tree.TreeFileNode;

public class ObservedFolderRootAvailableEvent extends AbstractObservedFolderEvent{
    private final TreeFileNode nodeDataTreeItem;

    public ObservedFolderRootAvailableEvent(ObservedFolder folder, TreeFileNode treeItem) {
        super(folder);
        nodeDataTreeItem = treeItem;
    }

    @Override
    public void dispatch(MainViewController mainViewController) {
        mainViewController.addToMainTree(folder, nodeDataTreeItem);
    }
}
