package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.controllers.alerts.Alerts;
import org.agh.diskstalker.model.ObservedFolder;

public class ObservedFolderFilesAmountChangedEvent extends AbstractObservedFolderEvent {
    public ObservedFolderFilesAmountChangedEvent(ObservedFolder folder) {
        super(folder);
    }

    @Override
    public void dispatch(MainController mainController) {
        var limits = folder.getLimits();
        if (limits.isFilesAmountExceeded()) {
            if(!limits.isFilesAmountFlagShown()) {
                Alerts.filesAmountExceededAlert(folder.getPath().toString(), limits.getFilesAmountLimit());
                limits.setFilesAmountFlagShown(true);
                mainController.refreshViews();
            }
        }
        else {
            limits.setFilesAmountFlagShown(false);
            mainController.refreshViews();
        }
    }
}
