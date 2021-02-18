package org.agh.diskstalker.model.interfaces;

import org.agh.diskstalker.events.observedFolderEvents.AbstractObservedFolderEvent;
import org.agh.diskstalker.model.FolderLimits;

import java.nio.file.Path;

public interface ILimitableFolder {
    void emitEvent(AbstractObservedFolderEvent event);

    long getSize();

    long getFilesAmount();

    long getBiggestFileSize();

    FolderLimits getLimits();

    void setLimits(FolderLimits limits);

    Path getPath();
}
