package org.agh.diskstalker.events.observedFolderEvents;

import lombok.AllArgsConstructor;
import org.agh.diskstalker.model.ObservedFolder;

@AllArgsConstructor
public abstract class AbstractObservedFolderEvent implements ObservedFolderEvent{
    protected final ObservedFolder folder;
}
