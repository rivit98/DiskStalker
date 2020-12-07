package model.events;

import model.FileData;
import model.ObservedFolder;
import model.tree.TreeBuilder;

import java.nio.file.Path;

public class EventProcessor implements IEventProcessor {
    private final ObservedFolder observedFolder;
    private final TreeBuilder treeBuilder;

    public EventProcessor(ObservedFolder observedFolder, TreeBuilder treeBuilder) {
        this.observedFolder = observedFolder;
        this.treeBuilder = treeBuilder;
    }

    //TODO: case when user removes root folder!
    //TODO: when updating branch, update parents size!
    //TODO: better idea - use nodemap for inserting - requries updating size in reverse order (bottom-up)

    @Override
    public void processEvent(EventObject eventObject) {
        Path resolvedPath = eventObject.getTargetDir();
        var eventType = eventObject.getEventType();
        System.out.println(eventType.name() + " | context: " + resolvedPath);

        switch (eventType) {
            case FILE_CREATED -> handleCreateEventFile(resolvedPath);
            case DIR_CREATED -> handleCreateEventDir(resolvedPath);

            case FILE_DELETED -> handleDeleteEventFile(resolvedPath);
            case DIR_DELETED -> handleDeleteEventDir(resolvedPath);

            case FILE_MODIFIED -> handleModifyEventFile(resolvedPath);
            case DIR_MODIFIED -> handleModifyEventDir(resolvedPath);
        }
    }

    //FIXME: somewhere here (handleModifyEventFile, handleModifyEventDir) we got nullptrexception :(
    void handleModifyEventFile(Path resolvedPath) {
        var modifiedNode = observedFolder.getPathToTreeMap().get(resolvedPath);
        modifiedNode.updateMe();
    }

    void handleModifyEventDir(Path resolvedPath) {
        //FIXME, TEST this
        var modifiedNode = observedFolder.getPathToTreeMap().get(resolvedPath);
        modifiedNode.updateMe();
    }

    void handleDeleteEventFile(Path resolvedPath) {
        var affectedNode = observedFolder.getPathToTreeMap().remove(resolvedPath); // this is the folder where something has changed
        var fileData = affectedNode.getValue();
        affectedNode.deleteMe();
    }

    void handleDeleteEventDir(Path resolvedPath) {
        var affectedNode = observedFolder.getPathToTreeMap().remove(resolvedPath); // this is the folder where something has changed
        observedFolder.removeMappedDirsRecursively(affectedNode);
        affectedNode.deleteMe();
    }

    void handleCreateEventFile(Path resolvedPath) {
        handleCreateCommon(resolvedPath);
    }

    void handleCreateEventDir(Path resolvedPath) {
        handleCreateCommon(resolvedPath);
    }

    private void handleCreateCommon(Path resolvedPath) {
        var newNode = treeBuilder.addItem(new FileData(resolvedPath));
        var fileData = newNode.getValue();
        observedFolder.getPathToTreeMap().put(fileData.getPath(), newNode);
    }
}
