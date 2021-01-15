package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainController;

public interface ObservedFolderEvent {
    void dispatch(MainController mainController);
}
