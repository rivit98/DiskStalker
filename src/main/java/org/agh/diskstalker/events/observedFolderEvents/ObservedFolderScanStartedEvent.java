package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.folders.FakeObservedFolder;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;

public class ObservedFolderScanStartedEvent extends AbstractObservedFolderEvent{
    public ObservedFolderScanStartedEvent(ILimitableObservableFolder folder) {
        super(folder);
    }

    @Override
    public void dispatch(MainController mainController) {
        addLoadingFolder(mainController);
    }

    private void addLoadingFolder(MainController mainController) {
        var folderList = mainController.getFolderList();
        var treeTableView = mainController.getTreeTableView();

        var fakeFolder = new FakeObservedFolder(folder);
        folderList.add(fakeFolder);
        treeTableView.getRoot().getChildren().add(fakeFolder.getFakeNode());
        treeTableView.sort();
    }
}
