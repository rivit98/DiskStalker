package org.agh.diskstalker.model.events.observedFolderEvents;

import org.agh.diskstalker.controllers.Alerts;
import org.agh.diskstalker.controllers.MainViewController;
import org.agh.diskstalker.model.ObservedFolder;

public class ObservedFolderErrorEvent extends AbstractObservedFolderEvent{
    private final String message;

    public ObservedFolderErrorEvent(ObservedFolder folder, String message){
        super(folder);
        this.message = message;
    }

    @Override
    public void dispatch(MainViewController mainViewController) {
        Alerts.genericErrorAlert(super.folder.getPath(), message);
    }
}
