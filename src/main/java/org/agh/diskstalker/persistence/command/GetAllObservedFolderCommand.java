package org.agh.diskstalker.persistence.command;

public class GetAllObservedFolderCommand extends AbstractObservedFolderCommand {
    @Override
    public CommandResult get() {
        var res = new CommandResult();
        res.setFolderList(observedFolderDao.getAll());
        return res;
    }
}
