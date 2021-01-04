package org.agh.diskstalker.persistence.command;

import org.agh.diskstalker.persistence.ConnectionProvider;

public class ConnectToDbCommand extends AbstractObservedFolderCommand{
    @Override
    public CommandResult get() {
        ConnectionProvider.init("jdbc:sqlite:observed_folders.db");
        return new CommandResult();
    }
}
