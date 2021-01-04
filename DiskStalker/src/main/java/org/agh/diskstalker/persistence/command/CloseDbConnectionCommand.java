package org.agh.diskstalker.persistence.command;

import org.agh.diskstalker.persistence.ConnectionProvider;

import java.sql.SQLException;

public class CloseDbConnectionCommand extends AbstractObservedFolderCommand{
    @Override
    public CommandResult get() {
        try {
            ConnectionProvider.close();
            System.out.println("close");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return CommandResult.empty();
    }
}
