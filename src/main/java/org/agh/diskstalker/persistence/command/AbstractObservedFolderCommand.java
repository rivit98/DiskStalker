package org.agh.diskstalker.persistence.command;

import org.agh.diskstalker.persistence.IObservedFolderDao;
import org.agh.diskstalker.persistence.ObservedFolderDao;

public abstract class AbstractObservedFolderCommand implements IObservedFolderCommand{
    protected final IObservedFolderDao observedFolderDao = new ObservedFolderDao();
}
