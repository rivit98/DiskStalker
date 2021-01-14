package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.controllers.alerts.Alerts;
import org.agh.diskstalker.model.ObservedFolder;

public class ObservedFolderSizeChangedEvent extends AbstractObservedFolderEvent {
    public ObservedFolderSizeChangedEvent(ObservedFolder folder) {
        super(folder);
    }

    @Override
    public void dispatch(MainController mainController) {
        if (folder.isSizeLimitExceeded()) {
            if(!folder.getSizeExceededProperty().getValue()) {
                Alerts.sizeExceededAlert(folder.getPath().toString(), folder.getMaximumSize());
                folder.setSizeExceeded(true);
                mainController.refreshViews();
            }
        }
        else {
            folder.setSizeExceeded(false);
            mainController.refreshViews();
        }
    }
}
