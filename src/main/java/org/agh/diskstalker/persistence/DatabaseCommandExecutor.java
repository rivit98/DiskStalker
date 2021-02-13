package org.agh.diskstalker.persistence;

import org.agh.diskstalker.persistence.command.AbstractObservedFolderCommand;
import org.agh.diskstalker.persistence.command.CloseDbConnectionCommand;
import org.agh.diskstalker.persistence.command.CommandResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DatabaseCommandExecutor {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final IObservedFolderDao observedFolderDao;
    private final QueryExecutor queryExecutor;

    @Autowired
    public DatabaseCommandExecutor(IObservedFolderDao observedFolderDao, QueryExecutor queryExecutor) {
        this.observedFolderDao = observedFolderDao;
        this.queryExecutor = queryExecutor;
    }

    public CompletableFuture<CommandResult> executeCommand(AbstractObservedFolderCommand command) {
        //kinda ugly "injecting in runtime" method, but...
        command.setObservedFolderDao(observedFolderDao);
        command.setQueryExecutor(queryExecutor);

        return CompletableFuture.supplyAsync(command, executor);
    }

    public void stop(){
        executeCommand(new CloseDbConnectionCommand());
        executor.shutdown();
    }
}
