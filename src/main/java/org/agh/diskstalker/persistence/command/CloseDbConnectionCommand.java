package org.agh.diskstalker.persistence.command;

import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.persistence.ConnectionProvider;

import java.sql.SQLException;

@Slf4j
public class CloseDbConnectionCommand extends AbstractObservedFolderCommand{
    @Override
    public CommandResult get() {
        try {
            ConnectionProvider.close();
        } catch (SQLException exception) {
            log.error("Could not close db connection", exception);
        }
        return CommandResult.empty();
    }
}
