package org.agh.diskstalker.persistence.command;

import lombok.AllArgsConstructor;
import org.agh.diskstalker.model.interfaces.IObservedFolder;

@AllArgsConstructor
public class CreateObservedFolderCommand extends AbstractObservedFolderCommand{
    private final IObservedFolder IObservedFolder;

    @Override
    public CommandResult get() {
        observedFolderDao.create(IObservedFolder);
        return CommandResult.empty();
    }
}
