package org.agh.diskstalker.events.observedFolderEvents;

import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.FakeObservedFolder;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.interfaces.IObservedFolder;
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
        replaceLoadingFolderWithRealOne(mainController, folder, nodeDataTreeItem);
    }

    // replace fake folder with real one
    private void replaceLoadingFolderWithRealOne(MainController mainController, IObservedFolder folder, TreeFileNode realRoot) {
        var folderList = mainController.getFolderList();
        var treeTableView = mainController.getTreeTableView();

        var searchedPath = folder.getPath();
        var fakeFolder = (FakeObservedFolder) folderList.stream()
                .filter(f -> f.getPath().equals(searchedPath))
                .findFirst()
                .orElseThrow();

        var indexToReplace = folderList.indexOf(fakeFolder);
        folderList.set(indexToReplace, fakeFolder.getRealObservedFolder());
        treeTableView.getRoot().getChildren().remove(fakeFolder.getFakeNode());
        treeTableView.getRoot().getChildren().add(realRoot);
        treeTableView.sort();
        mainController.refreshViews();
    }
}
