package org.agh.diskstalker.model.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainView;

public interface ObservedFolderEvent {
    void dispatch(MainView view);
}
