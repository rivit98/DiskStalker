package org.agh.diskstalker.events.observedFolderEvents;

import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.folders.FakeObservedFolder;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;

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


        folderList.set(folderList.indexOf(fakeFolder), folder);
        var indexToReplace = treeTableView.getRoot().getChildren().indexOf(fakeFolder.getFakeNode());
        treeTableView.getRoot().getChildren().set(indexToReplace, folder.getNodesTree().getRoot());

        folder.getLimits().checkLimits();
        treeTableView.sort();
        mainController.refreshViews();
    }
}
