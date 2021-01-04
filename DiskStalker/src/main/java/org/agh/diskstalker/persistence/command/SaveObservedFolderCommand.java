package org.agh.diskstalker.persistence.command;

import org.agh.diskstalker.model.ObservedFolder;

public class SaveObservedFolderCommand extends AbstractObservedFolderCommand{
    private final ObservedFolder observedFolder;

    public SaveObservedFolderCommand(ObservedFolder folder) {
        observedFolder = folder;
    }

    @Override
    public CommandResult get() {
        observedFolderDao.save(observedFolder);
        return CommandResult.empty();
    }
}
