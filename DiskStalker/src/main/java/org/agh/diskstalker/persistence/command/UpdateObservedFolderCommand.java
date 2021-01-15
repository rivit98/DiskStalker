package org.agh.diskstalker.persistence.command;

import lombok.AllArgsConstructor;
import org.agh.diskstalker.model.ObservedFolder;

@AllArgsConstructor
public class UpdateObservedFolderCommand extends AbstractObservedFolderCommand {
    private final ObservedFolder observedFolder;

    @Override
    public CommandResult get() {
        observedFolderDao.update(observedFolder);
        return CommandResult.empty();
    }
}
