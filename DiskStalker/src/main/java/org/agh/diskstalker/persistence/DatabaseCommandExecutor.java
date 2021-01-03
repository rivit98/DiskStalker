package org.agh.diskstalker.persistence;

import org.agh.diskstalker.persistence.command.CommandResult;
import org.agh.diskstalker.persistence.command.IObservedFolderCommand;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseCommandExecutor {
    private final ExecutorService executor = Executors.newSingleThreadExecutor(); //tODO: shutdown it on appclose

    public CompletableFuture<CommandResult> executeCommand(IObservedFolderCommand command) {
        return CompletableFuture.supplyAsync(command, executor);
    }

    public void stop(){
        executor.shutdown();
    }
}
