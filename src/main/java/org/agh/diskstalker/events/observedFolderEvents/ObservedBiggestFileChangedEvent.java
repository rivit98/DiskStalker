package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.limits.LimitType;

public class ObservedBiggestFileChangedEvent extends AbstractObservedFolderLimitEvent {
    public ObservedBiggestFileChangedEvent(ObservedFolder folder) {
        super(folder);
        limitType = LimitType.BIGGEST_FILE;
    }
}
