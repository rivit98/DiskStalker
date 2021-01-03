package org.agh.diskstalker.model.events.observedFolderEvents;

import org.agh.diskstalker.controllers.Alerts;
import org.agh.diskstalker.controllers.MainView;
import org.agh.diskstalker.model.ObservedFolder;

public class ObservedFolderSizeChangedEvent extends AbstractObservedFolderEvent {
    public ObservedFolderSizeChangedEvent(ObservedFolder folder) {
        super(folder);
    }

    @Override
    public void dispatch(MainView view) {
        if (folder.isSizeLimitExceeded()) {
            if(!folder.getSizeExceededProperty().getValue()) {
                Alerts.sizeExceededAlert(folder.getPath().toString(), folder.getMaximumSize());
                folder.setSizeExceeded(true);
                view.getMainView().refresh(); //TODO: change this
            }
        }
        else {
            folder.setSizeExceeded(false);
            view.getMainView().refresh(); //TODO: change this
        }
    }
}
