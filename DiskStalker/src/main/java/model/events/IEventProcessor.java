package model.events;

import java.nio.file.Path;

public interface IEventProcessor {
    void processEvent(EventObject eventObject);

    //FIXME: somewhere here (handleModifyEventFile, handleModifyEventDir) we got nullptrexception :(
    default void handleModifyEventFile(Path resolvedPath) {
        var modifiedNode = observedFolder.getPathToTreeMap().get(resolvedPath);
        modifiedNode.updateMe();
    }

    default void handleModifyEventDir(Path resolvedPath) {
        //FIXME, TEST this
        var modifiedNode = observedFolder.getPathToTreeMap().get(resolvedPath);
        modifiedNode.updateMe();
    }

    default void handleDeleteEventFile(Path resolvedPath) {
        var affectedNode = observedFolder.getPathToTreeMap().remove(resolvedPath); // this is the folder where something has changed
        var fileData = affectedNode.getValue();
        affectedNode.deleteMe();
    }

    default void handleDeleteEventDir(Path resolvedPath) {
        var affectedNode = observedFolder.getPathToTreeMap().remove(resolvedPath); // this is the folder where something has changed
        observedFolder.removeMappedDirsRecursively(affectedNode);
        affectedNode.deleteMe();
    }

    default void handleCreateEventFile(Path resolvedPath) {
        handleCreateCommon(resolvedPath);
    }

    default void handleCreateEventDir(Path resolvedPath) {
        handleCreateCommon(resolvedPath);
    }
}
