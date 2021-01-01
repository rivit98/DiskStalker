package org.agh.diskstalker.persistence;

import org.agh.diskstalker.model.ObservedFolder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//TODO: rewrite this to be async
//TODO: maybe use Executors with blockingqueue
public class DatabaseCommandExecutor {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Future<?> executeCommand(ObservedFolder folder, DatabaseCommandType type) {
        return executor.submit(() -> {
            switch (type) {
                case SAVE -> ObservedFolderDao.save(folder);
                case DELETE -> ObservedFolderDao.delete(folder);
                case UPDATE -> ObservedFolderDao.update(folder);
            }
        });
    }
}
