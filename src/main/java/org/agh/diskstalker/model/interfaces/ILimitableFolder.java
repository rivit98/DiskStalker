package org.agh.diskstalker.model.interfaces;

import org.agh.diskstalker.events.observedFolderEvents.AbstractObservedFolderEvent;
import org.agh.diskstalker.events.observedFolderEvents.ObservedFolderEvent;
import org.agh.diskstalker.model.limits.FolderLimits;

import java.nio.file.Path;

public interface ILimitableFolder {
    void emitEvent(ObservedFolderEvent event);

    long getSize();

    long getFilesAmount();

    long getBiggestFileSize();

    FolderLimits getLimits();

    void setLimits(FolderLimits limits);

    Path getPath();
}
