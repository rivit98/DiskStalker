package org.agh.diskstalker.model.events;

import javafx.beans.binding.Bindings;
import javafx.scene.control.TreeItem;
import org.agh.diskstalker.controllers.MainView;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.NodeData;
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
//        folder.isSizeExceededFlag().addListener((observable, oldValue, newValue) -> {
//            nodeDataTreeItem.setGraphic(null);
//            nodeDataTreeItem.setGraphic(GraphicsFactory.getGraphic(true, folder.isSizeLimitExceeded()));
//            System.out.println("set");
//            view.getMainView().refresh();
//        });
    }
}
