package org.agh.diskstalker.model.tree;

import io.reactivex.rxjava3.subjects.SingleSubject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import org.agh.diskstalker.model.NodeData;

import java.nio.file.Path;
import java.util.Optional;


public class TreeBuilder {
    @Getter
    private final ObservableMap<Path, TreeFileNode> pathToTreeMap = FXCollections.observableHashMap();
    @Getter
    private final SingleSubject<TreeFileNode> rootSubject = SingleSubject.create();
    @Getter
    private TreeFileNode root;

    public void processNodeData(NodeData nodeData) {
        var insertedNode = new TreeFileNode(nodeData);

        if (root != null) {
            insertNewNode(insertedNode);
        } else {
            pathToTreeMap.put(nodeData.getPath(), insertedNode);
            root = insertedNode;
            rootSubject.onSuccess(root);
        }
    }

    public void insertNewNode(TreeFileNode newNode) {
        var nodeData = newNode.getValue();
        var parentPath = nodeData.getPath().getParent();
        var parentNode = pathToTreeMap.get(parentPath);
        parentNode.insertNode(newNode);
        pathToTreeMap.put(nodeData.getPath(), newNode);
        nodeData.updateModificationTime();
    }

    public boolean containsNode(Path path) {
        return pathToTreeMap.containsKey(path);
    }

    public void removeMappedDirsRecursively(TreeItem<NodeData> node) {
        removeMappedDirs(node);
        node.getChildren().forEach(this::removeMappedDirsRecursively);
    }

    public void removeMappedDirs(TreeItem<NodeData> node) {
        Optional.ofNullable(node)
                .map(TreeItem::getValue)
                .map(NodeData::getPath)
                .ifPresent(pathToTreeMap::remove);
    }
}

