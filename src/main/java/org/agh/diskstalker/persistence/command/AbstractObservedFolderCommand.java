package org.agh.diskstalker.persistence.command;

import lombok.Setter;
import org.agh.diskstalker.persistence.IObservedFolderDao;
import org.agh.diskstalker.persistence.QueryExecutor;

import java.util.function.Supplier;

@Setter
public abstract class AbstractObservedFolderCommand implements Supplier<CommandResult> {
    protected IObservedFolderDao observedFolderDao;
    protected QueryExecutor queryExecutor;
}
