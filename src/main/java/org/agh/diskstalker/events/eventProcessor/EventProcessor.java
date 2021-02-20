package org.agh.diskstalker.events.eventProcessor;

import lombok.AllArgsConstructor;
import org.agh.diskstalker.events.filesystemEvents.FilesystemEvent;
import org.agh.diskstalker.model.tree.NodeData;
import org.agh.diskstalker.model.tree.NodesTree;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;

@AllArgsConstructor
public class EventProcessor implements IEventProcessor {
    private final NodesTree nodesTree;

    @Override
    public void processEvent(FilesystemEvent filesystemEvent) {
        var resolvedPath = filesystemEvent.getTargetDir();
        var eventType = filesystemEvent.getType();

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
        var modifiedNode = nodesTree.getPathToTreeMap().get(resolvedPath);
        modifiedNode.updateMe();
    }

    private void handleDeleteEventFile(Path resolvedPath) {
        handleDeleteEventCommon(resolvedPath);
    }

    private void handleDeleteEventDir(Path resolvedPath) {
        handleDeleteEventCommon(resolvedPath);
    }

    private void handleDeleteEventCommon(Path resolvedPath) {
        var affectedNode = nodesTree.getPathToTreeMap().get(resolvedPath);
        nodesTree.removeMappedDirs(affectedNode);
        affectedNode.deleteMe();
    }

    private void handleCreateEventFile(Path resolvedPath) {
        handleCreateCommon(resolvedPath);
    }

    private void handleCreateEventDir(Path resolvedPath) {
        handleCreateCommon(resolvedPath);
    }

    private void handleCreateCommon(Path resolvedPath) {
        var nodeData = new NodeData(resolvedPath);
        var newTreeNode = new TreeFileNode(nodeData);
        nodesTree.insertNewNode(newTreeNode);
    }
}
