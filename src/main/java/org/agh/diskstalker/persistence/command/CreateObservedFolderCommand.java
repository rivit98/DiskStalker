package org.agh.diskstalker.persistence.command;

import lombok.AllArgsConstructor;
import org.agh.diskstalker.model.ObservedFolder;

@AllArgsConstructor
public class CreateObservedFolderCommand extends AbstractObservedFolderCommand{
    private final ObservedFolder observedFolder;

    @Override
    public CommandResult get() {
        observedFolderDao.create(observedFolder);
        return CommandResult.empty();
    }
}
