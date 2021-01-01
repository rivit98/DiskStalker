package org.agh.diskstalker.model.events;

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
            Alerts.sizeExceededAlert(folder.getPath().toString(), folder.getMaximumSize());
        }
    }
}
