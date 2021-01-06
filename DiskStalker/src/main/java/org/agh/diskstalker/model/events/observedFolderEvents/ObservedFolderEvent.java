package org.agh.diskstalker.model.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainViewController;

public interface ObservedFolderEvent {
    void dispatch(MainViewController mainViewController);
}
