package org.agh.diskstalker.persistence.command;

import lombok.AllArgsConstructor;
import org.agh.diskstalker.model.ObservedFolder;

@AllArgsConstructor
public class SaveObservedFolderCommand extends AbstractObservedFolderCommand{
    private final ObservedFolder observedFolder;

    @Override
    public CommandResult get() {
        observedFolderDao.save(observedFolder);
        return CommandResult.empty();
    }
}
