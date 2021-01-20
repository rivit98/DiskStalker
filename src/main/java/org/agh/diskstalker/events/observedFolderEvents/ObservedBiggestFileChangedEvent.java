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
            if(!limits.isBiggestFileExceededFlag()) {
                Alerts.biggestFileExceededAlert(folder.getPath().toString(), limits.getBiggestFileLimit());
                limits.setBiggestFileExceededFlag(true);
                mainController.refreshViews();
            }
        }
        else {
            limits.setBiggestFileExceededFlag(false);
            mainController.refreshViews();
        }
    }
}
