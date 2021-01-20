package org.agh.diskstalker.persistence;

import org.agh.diskstalker.persistence.command.CloseDbConnectionCommand;
import org.agh.diskstalker.persistence.command.CommandResult;
import org.agh.diskstalker.persistence.command.IObservedFolderCommand;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseCommandExecutor {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CompletableFuture<CommandResult> executeCommand(IObservedFolderCommand command) {
        return CompletableFuture.supplyAsync(command, executor);
    }

    public void stop(){
        executeCommand(new CloseDbConnectionCommand());
        executor.shutdown();
    }
}
