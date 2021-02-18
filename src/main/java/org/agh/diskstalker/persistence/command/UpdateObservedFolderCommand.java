package org.agh.diskstalker.persistence.command;

import lombok.AllArgsConstructor;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;

@AllArgsConstructor
public class UpdateObservedFolderCommand extends AbstractObservedFolderCommand {
    private final ILimitableObservableFolder observedFolder;

    @Override
    public CommandResult get() {
        observedFolderDao.update(observedFolder);
        return CommandResult.empty();
    }
}
