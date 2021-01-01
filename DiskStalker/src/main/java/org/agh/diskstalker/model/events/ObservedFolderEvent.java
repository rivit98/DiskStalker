package org.agh.diskstalker.model.events;

import org.agh.diskstalker.controllers.MainView;

public interface ObservedFolderEvent {
    void dispatch(MainView view);
}
