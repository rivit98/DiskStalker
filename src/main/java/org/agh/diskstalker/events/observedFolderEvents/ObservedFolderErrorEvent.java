package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.folders.ObservedFolder;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;

public class ObservedFolderErrorEvent extends AbstractObservedFolderEvent{
    private final String message;

    public ObservedFolderErrorEvent(ILimitableObservableFolder folder, String message){
        super(folder);
        this.message = message;
    }

    @Override
    public void dispatch(MainController mainController) {
        mainController.getAlertsFactory().genericErrorAlert(super.folder.getPath(), message);
    }
}
