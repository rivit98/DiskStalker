package org.agh.diskstalker.persistence.command;

import lombok.AllArgsConstructor;
import org.agh.diskstalker.model.interfaces.IObservedFolder;

@AllArgsConstructor
public class DeleteObservedFolderCommand extends AbstractObservedFolderCommand {
    private final IObservedFolder IObservedFolder;

    @Override
    public CommandResult get() {
        observedFolderDao.delete(IObservedFolder);
        return CommandResult.empty();
    }
}
