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
        if (folder.isFilesAmountExceeded()) {
            if(!folder.getFilesAmountExceededProperty().getValue()) {
                Alerts.filesAmountExceededAlert(folder.getPath().toString(), folder.getMaximumFilesAmount());
                folder.setFilesAmountExceeded(true);
                mainController.refreshViews();
            }
        }
        else {
            folder.setFilesAmountExceeded(false);
            mainController.refreshViews();
        }
    }
}
