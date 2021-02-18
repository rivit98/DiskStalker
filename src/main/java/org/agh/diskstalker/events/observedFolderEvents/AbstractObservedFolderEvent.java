package org.agh.diskstalker.events.observedFolderEvents;

import lombok.AllArgsConstructor;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;

@AllArgsConstructor
public abstract class AbstractObservedFolderEvent implements ObservedFolderEvent{
    protected final ILimitableObservableFolder folder;
}
