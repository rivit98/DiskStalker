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
        var limits = folder.getLimits();
        if (limits.isTotalSizeExceeded()) {
            if(!limits.isTotalSizeFlagShown()) {
                Alerts.sizeExceededAlert(folder.getPath().toString(), limits.getTotalSizeLimit());
                limits.setTotalSizeFlagShown(true);
                mainController.refreshViews();
            }
        }
        else {
            limits.setTotalSizeFlagShown(false);
            mainController.refreshViews();
        }
    }
}
