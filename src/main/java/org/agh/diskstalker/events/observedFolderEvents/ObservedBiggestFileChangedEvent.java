package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.model.folders.ObservedFolder;
import org.agh.diskstalker.model.limits.LimitType;

public class ObservedBiggestFileChangedEvent extends AbstractObservedFolderLimitEvent {
    public ObservedBiggestFileChangedEvent(ObservedFolder folder) {
        super(folder, LimitType.BIGGEST_FILE);
    }
}
