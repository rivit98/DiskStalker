package org.agh.diskstalker.events.observedFolderEvents;

import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.tree.TreeFileNode;

@Slf4j
public class ObservedFolderScanFinishedEvent extends AbstractObservedFolderEvent{
    private final TreeFileNode nodeDataTreeItem;

    public ObservedFolderScanFinishedEvent(ObservedFolder folder, TreeFileNode treeItem) {
        super(folder);
        nodeDataTreeItem = treeItem;
    }

    @Override
    public void dispatch(MainController mainController) {
        mainController.replaceLoadingFolderWithRealOne(folder, nodeDataTreeItem);
    }
}
