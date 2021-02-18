package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;

public class ObservedFolderScanStartedEvent extends AbstractObservedFolderEvent{
    public ObservedFolderScanStartedEvent(ILimitableObservableFolder folder) {
        super(folder);
    }

    @Override
    public void dispatch(MainController mainController) {
        mainController.addLoadingFolder(folder);
    }
}
