package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.model.ObservedFolder;

public abstract class AbstractObservedFolderEvent implements ObservedFolderEvent{
    protected final ObservedFolder folder;

    protected AbstractObservedFolderEvent(ObservedFolder folder) {
        this.folder = folder;
    }
}
