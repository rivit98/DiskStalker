package org.agh.diskstalker.events.eventProcessor;

import org.agh.diskstalker.events.filesystemEvents.FilesystemEvent;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.statisctics.FilesTypeStatistics;
import org.agh.diskstalker.model.tree.TreeBuilder;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;

public class EventProcessor implements IEventProcessor {
    private final TreeBuilder treeBuilder;
    private final FilesTypeStatistics filesTypeStatistics;

    public EventProcessor(TreeBuilder treeBuilder, FilesTypeStatistics filesTypeStatistics) {
        this.treeBuilder = treeBuilder;
        this.filesTypeStatistics = filesTypeStatistics;
    }

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
        var modifiedNode = treeBuilder.getPathToTreeMap().get(resolvedPath);
        modifiedNode.updateMe();
    }

    private void handleDeleteEventFile(Path resolvedPath) {
        handleDeleteEventCommon(resolvedPath);
    }

    private void handleDeleteEventDir(Path resolvedPath) {
        handleDeleteEventCommon(resolvedPath);
    }

    private void handleDeleteEventCommon(Path resolvedPath) {
        var affectedNode = treeBuilder.getPathToTreeMap().remove(resolvedPath);
        treeBuilder.removeMappedDirsRecursively(affectedNode);
        if(affectedNode.getValue().isFile()) {
            filesTypeStatistics.decrementTypeCounter(affectedNode.getValue());
        }
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
        treeBuilder.insertNewNode(newTreeNode);
        if(nodeData.isFile()) {
            filesTypeStatistics.addNewNodeType(nodeData);
        }
    }
}
