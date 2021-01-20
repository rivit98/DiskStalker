package org.agh.diskstalker.persistence.command;

import lombok.AllArgsConstructor;
import org.agh.diskstalker.model.ObservedFolder;

@AllArgsConstructor
public class DeleteObservedFolderCommand extends AbstractObservedFolderCommand {
    private final ObservedFolder observedFolder;

    @Override
    public CommandResult get() {
        observedFolderDao.delete(observedFolder);
        return CommandResult.empty();
    }
}
