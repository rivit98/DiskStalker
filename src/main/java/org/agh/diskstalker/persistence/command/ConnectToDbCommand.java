package org.agh.diskstalker.persistence.command;

public class ConnectToDbCommand extends AbstractObservedFolderCommand{
    @Override
    public CommandResult get() {
        queryExecutor.getConnectionProvider().init();
        queryExecutor.createTables();
        return CommandResult.empty();
    }
}
