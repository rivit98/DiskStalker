package model.events;

import model.FileData;
import model.ObservedFolder;
import model.tree.TreeBuilder;
import model.tree.TreeFileNode;

import java.nio.file.Path;

public class EventProcessor implements IEventProcessor {
    private final ObservedFolder observedFolder;

    public EventProcessor(ObservedFolder observedFolder) {
        this.observedFolder = observedFolder;
    }

    //TODO: case when user removes root folder!

    @Override
    public void processEvent(EventObject eventObject) {
        Path resolvedPath = eventObject.getTargetDir();
        var eventType = eventObject.getEventType();
        System.out.println(eventType.name() + " | " + resolvedPath);

        switch (eventType) {
            case FILE_CREATED -> handleCreateEventFile(resolvedPath);
            case DIR_CREATED -> handleCreateEventDir(resolvedPath);

            case FILE_DELETED -> handleDeleteEventFile(resolvedPath);
            case DIR_DELETED -> handleDeleteEventDir(resolvedPath);

            case FILE_MODIFIED -> handleModifyEventFile(resolvedPath);
//            case DIR_MODIFIED -> handleModifyEventDir(resolvedPath);
        }
    }

    private void handleModifyEventFile(Path resolvedPath) {
        var modifiedNode = observedFolder.getPathToTreeMap().get(resolvedPath);
        modifiedNode.updateMe();
    }

    private void handleDeleteEventFile(Path resolvedPath) {
        handleDeleteEventCommon(resolvedPath);
    }

    private void handleDeleteEventDir(Path resolvedPath) {
        handleDeleteEventCommon(resolvedPath);
    }

    private void handleDeleteEventCommon(Path resolvedPath) {
        var affectedNode = observedFolder.getPathToTreeMap().remove(resolvedPath);
        observedFolder.removeMappedDirsRecursively(affectedNode);
        affectedNode.deleteMe();
    }

    private void handleCreateEventFile(Path resolvedPath) {
        handleCreateCommon(resolvedPath);
    }

    private void handleCreateEventDir(Path resolvedPath) {
        handleCreateCommon(resolvedPath);
    }

    private void handleCreateCommon(Path resolvedPath) {
        var fileData = new FileData(resolvedPath);
        var newTreeNode = new TreeFileNode(fileData);
        var parentPath = resolvedPath.getParent();
        var parentNode = observedFolder.getPathToTreeMap().get(parentPath);
        parentNode.insertNode(newTreeNode);
        observedFolder.getPathToTreeMap().put(fileData.getPath(), newTreeNode);
    }
}
