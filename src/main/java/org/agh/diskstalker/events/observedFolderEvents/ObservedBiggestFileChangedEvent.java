package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.controllers.alerts.Alerts;
import org.agh.diskstalker.model.ObservedFolder;

public class ObservedBiggestFileChangedEvent extends AbstractObservedFolderEvent {
    public ObservedBiggestFileChangedEvent(ObservedFolder folder) {
        super(folder);
    }

    @Override
    public void dispatch(MainController mainController) {
        var limits = folder.getLimits();
        if (limits.isBiggestFileLimitExceeded()) {
            if(!limits.isBiggestFileFlagShown()) {
                Alerts.biggestFileExceededAlert(folder.getPath().toString(), limits.getBiggestFileLimit());
                limits.setBiggestFileFlagShown(true);
                mainController.refreshViews();
            }
        }
        else {
            limits.setBiggestFileFlagShown(false);
            mainController.refreshViews();
        }
    }
}
