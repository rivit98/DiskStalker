package org.agh.diskstalker.events.observedFolderEvents;

import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.folders.FakeObservedFolder;
import org.agh.diskstalker.model.folders.ObservedFolder;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;
import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.util.Objects;

@Slf4j
public class ObservedFolderScanFinishedEvent extends AbstractObservedFolderEvent{

    public ObservedFolderScanFinishedEvent(ILimitableObservableFolder folder) {
        super(folder);
    }

    @Override
    public void dispatch(MainController mainController) {
        replaceLoadingFolderWithRealOne(mainController);
    }

    // replace fake folder with real one
    private void replaceLoadingFolderWithRealOne(MainController mainController) {
        var folderList = mainController.getFolderList();
        var treeTableView = mainController.getTreeTableView();

        var searchedPath = folder.getPath();
        var fakeFolder = (FakeObservedFolder) folderList.stream()
                .filter(f -> f.getPath().equals(searchedPath))
                .findFirst()
                .orElseThrow();

        var indexToReplace = folderList.indexOf(fakeFolder);
        folderList.set(indexToReplace, folder);
        treeTableView.getRoot().getChildren().remove(fakeFolder.getFakeNode());
        treeTableView.getRoot().getChildren().add(folder.getNodesTree().getRoot());

        folder.getLimits().checkLimits();
        treeTableView.sort();
        mainController.refreshViews();
    }
}
