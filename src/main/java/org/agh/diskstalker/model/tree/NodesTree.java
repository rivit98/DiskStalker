package org.agh.diskstalker.model.tree;

import io.reactivex.rxjava3.subjects.SingleSubject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.TreeItem;
import lombok.Getter;

import java.nio.file.Path;
import java.util.Optional;


public class NodesTree {
    @Getter
    private final ObservableMap<Path, TreeFileNode> pathToTreeMap = FXCollections.observableHashMap();
    @Getter
    private final SingleSubject<TreeFileNode> rootSubject = SingleSubject.create();
    @Getter
    private TreeFileNode root;

    public void addNode(NodeData nodeData) {
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
    }

    public boolean containsNode(Path path) {
        return pathToTreeMap.containsKey(path);
    }

    public void removeMappedDirs(TreeItem<NodeData> node) {
        removeMappedDirsSingle(node);
        node.getChildren().forEach(this::removeMappedDirs);
    }

    private void removeMappedDirsSingle(TreeItem<NodeData> node) {
        Optional.ofNullable(node)
                .map(TreeItem::getValue)
                .map(NodeData::getPath)
                .ifPresent(pathToTreeMap::remove);
    }

    public long getSize() {
        return Optional.ofNullable(root)
                .map(rootNode -> rootNode.getValue().getAccumulatedSize())
                .orElse(0L);
    }

    public long getFilesAmount() {
        return pathToTreeMap.values().stream()
                .filter(node -> !node.getValue().isDirectory())
                .count();
    }

    public long getBiggestFileSize(){
        return pathToTreeMap.values().stream()
                .map(TreeItem::getValue)
                .filter(NodeData::isFile)
                .map(NodeData::getAccumulatedSize)
                .max(Long::compare)
                .orElse(0L);
    }
}

