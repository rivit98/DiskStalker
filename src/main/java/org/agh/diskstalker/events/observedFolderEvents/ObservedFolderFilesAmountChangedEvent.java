package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.limits.LimitType;

public class ObservedFolderFilesAmountChangedEvent extends AbstractObservedFolderLimitEvent {
    public ObservedFolderFilesAmountChangedEvent(ObservedFolder folder) {
        super(folder);
        limitType = LimitType.FILES_AMOUNT;
    }
}
