package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.model.folders.ObservedFolder;
import org.agh.diskstalker.model.limits.LimitType;

public class ObservedLargestFileChangedEvent extends AbstractObservedFolderLimitEvent {
    public ObservedLargestFileChangedEvent(ObservedFolder folder) {
        super(folder, LimitType.LARGEST_FILE);
    }
}
