package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.ObservedFolder;

public class ObservedFolderScanStartedEvent extends AbstractObservedFolderEvent{
    public ObservedFolderScanStartedEvent(ObservedFolder folder) {
        super(folder);
    }

    @Override
    public void dispatch(MainController mainController) {
        mainController.addLoadingFolder(folder);
    }
}
