package org.agh.diskstalker.events.observedFolderEvents;

import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.limits.LimitType;

@Slf4j
public class ObservedFolderSizeChangedEvent extends AbstractObservedFolderLimitEvent {
    public ObservedFolderSizeChangedEvent(ObservedFolder folder) {
        super(folder);
        limitType = LimitType.FILES_AMOUNT;
    }
}
