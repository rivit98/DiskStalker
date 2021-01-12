package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.controllers.Alerts;
import org.agh.diskstalker.controllers.MainViewController;
import org.agh.diskstalker.model.ObservedFolder;

public class ObservedFolderSizeChangedEvent extends AbstractObservedFolderEvent {
    public ObservedFolderSizeChangedEvent(ObservedFolder folder) {
        super(folder);
    }

    @Override
    public void dispatch(MainViewController mainViewController) {
        if (folder.isSizeLimitExceeded()) {
            if(!folder.getSizeExceededProperty().getValue()) {
                Alerts.sizeExceededAlert(folder.getPath().toString(), folder.getMaximumSize());
                folder.setSizeExceeded(true);
                mainViewController.getTreeTableView().refresh(); //TODO: change this
            }
        }
        else {
            folder.setSizeExceeded(false);
            mainViewController.getTreeTableView().refresh(); //TODO: change this
        }
    }
}
