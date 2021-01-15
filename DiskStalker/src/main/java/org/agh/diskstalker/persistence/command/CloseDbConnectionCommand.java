package org.agh.diskstalker.persistence.command;

import org.agh.diskstalker.persistence.ConnectionProvider;

import java.sql.SQLException;

public class CloseDbConnectionCommand extends AbstractObservedFolderCommand{
    @Override
    public CommandResult get() {
        try {
            ConnectionProvider.close();
        } catch (SQLException exception) {
            exception.printStackTrace(); //TODO: logger
        }
        return CommandResult.empty();
    }
}
