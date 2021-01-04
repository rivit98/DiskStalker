package org.agh.diskstalker.persistence.command;

import org.agh.diskstalker.model.ObservedFolder;

public class UpdateObservedFolderCommand extends AbstractObservedFolderCommand {
    private final ObservedFolder observedFolder;

    public UpdateObservedFolderCommand(ObservedFolder folder) {
        observedFolder = folder;
    }

    @Override
    public CommandResult get() {
        observedFolderDao.update(observedFolder);
        return CommandResult.empty();
    }
}
