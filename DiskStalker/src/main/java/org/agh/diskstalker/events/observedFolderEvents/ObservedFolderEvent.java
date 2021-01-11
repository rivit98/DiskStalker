package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainViewController;

public interface ObservedFolderEvent {
    void dispatch(MainViewController mainViewController);
}
