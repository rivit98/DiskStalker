package org.agh.diskstalker.persistence.command;

import org.agh.diskstalker.model.ObservedFolder;

public class DeleteObservedFolderCommand extends AbstractObservedFolderCommand {
    private final ObservedFolder observedFolder;

    public DeleteObservedFolderCommand(ObservedFolder folder) {
        observedFolder = folder;
    }

    @Override
    public CommandResult get() {
        observedFolderDao.delete(observedFolder);
        return CommandResult.empty();
    }
}
