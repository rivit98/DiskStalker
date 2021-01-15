package org.agh.diskstalker.events.observedFolderEvents;

import lombok.AllArgsConstructor;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.controllers.alerts.Alerts;
import org.agh.diskstalker.model.ObservedFolder;

public class ObservedFolderErrorEvent extends AbstractObservedFolderEvent{
    private final String message;

    public ObservedFolderErrorEvent(ObservedFolder folder, String message){
        super(folder);
        this.message = message;
    }

    @Override
    public void dispatch(MainController mainController) {
        Alerts.genericErrorAlert(super.folder.getPath(), message);
    }
}
