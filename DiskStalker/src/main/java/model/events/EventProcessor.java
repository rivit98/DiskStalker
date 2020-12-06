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

    private void handleCreateCommon(Path resolvedPath) {
        var newNode = treeBuilder.addItem(new FileData(resolvedPath));
        var fileData = newNode.getValue();
        observedFolder.getPathToTreeMap().put(fileData.getPath(), newNode);
    }
}
