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
            if(!limits.isFilesAmountExceededFlag()) {
                Alerts.filesAmountExceededAlert(folder.getPath().toString(), limits.getFilesAmountLimit());
                limits.setFilesAmountExceededFlag(true);
                mainController.refreshViews();
            }
        }
        else {
            limits.setFilesAmountExceededFlag(false);
            mainController.refreshViews();
        }
    }
}
