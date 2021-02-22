package org.agh.diskstalker.events.eventProcessor;

import org.agh.diskstalker.events.filesystemEvents.FilesystemEvent;
import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.agh.diskstalker.model.tree.NodeData;
import org.agh.diskstalker.model.tree.NodesTree;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.agh.diskstalker.statistics.messages.AddRecognizeTypeMessage;
import org.agh.diskstalker.statistics.messages.RemoveRecognizeTypeMessage;
import org.agh.diskstalker.statistics.messages.UpdateRecognizeTypeMessage;

import java.nio.file.Path;

public class EventProcessor implements IEventProcessor {
    private final IObservedFolder folder;
    private final NodesTree nodesTree;

    public EventProcessor(IObservedFolder folder) {
        this.folder = folder;
        this.nodesTree = folder.getNodesTree();
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
        var modifiedNode = nodesTree.getPathToTreeMap().get(resolvedPath);
        if(modifiedNode == null){
            return;
        }

        modifiedNode.updateMe();
        folder.getTypeRecognizer().recognize(new UpdateRecognizeTypeMessage(folder, modifiedNode.getValue()));
    }

    private void handleDeleteEventFile(Path resolvedPath) {
        handleDeleteEventCommon(resolvedPath);
    }

    private void handleDeleteEventDir(Path resolvedPath) {
        handleDeleteEventCommon(resolvedPath);
    }

    private void handleDeleteEventCommon(Path resolvedPath) {
        var affectedNode = nodesTree.getPathToTreeMap().get(resolvedPath);
        if(affectedNode == null){
            return;
        }

        nodesTree.removeMappedDirs(affectedNode);
        affectedNode.deleteMe();
        new RemoveRecognizeTypeMessage(folder, affectedNode.getValue()).doAction();
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
        folder.getTypeRecognizer().recognize(new AddRecognizeTypeMessage(folder, newTreeNode.getValue()));
    }
}
