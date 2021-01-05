package org.agh.diskstalker.model.tree;

import io.reactivex.rxjava3.subjects.SingleSubject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.statisctics.Type;
import org.agh.diskstalker.model.statisctics.TypeDetector;

import java.nio.file.Path;
import java.util.HashMap;


public class TreeBuilder {
    private final HashMap<Path, TreeFileNode> pathToTreeMap = new HashMap<>();
    private final SingleSubject<TreeFileNode> rootSubject = SingleSubject.create();
    private TreeFileNode root;
    private ObservableList<Type> typeStatistics;

    public void processNodeData(NodeData nodeData) {
        var insertedNode = new TreeFileNode(nodeData);
        pathToTreeMap.put(nodeData.getPath(), insertedNode);

        if (root != null) {
            insertNewNode(insertedNode);
        } else {
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

    public SingleSubject<TreeFileNode> getRoot() {
        return rootSubject;
    }

    public boolean containsNode(Path path) {
        return pathToTreeMap.containsKey(path);
    }

    public HashMap<Path, TreeFileNode> getPathToTreeMap() {
        return pathToTreeMap;
    }

    public void removeMappedDirsRecursively(TreeItem<NodeData> node) {
        removeMappedDirs(node);
        node.getChildren().forEach(this::removeMappedDirsRecursively);
    }

    public void removeMappedDirs(TreeItem<NodeData> node) {
        pathToTreeMap.remove(node.getValue().getPath());
    }

    public ObservableList<Type> getTypeStatistics() {
        return typeStatistics;
    }

    public void setTypeStatistics() {
        typeStatistics = FXCollections.observableArrayList();
        var typeDetector = new TypeDetector();
        pathToTreeMap.forEach((path, node) -> {
            if(node.getValue().isFile()) {
                var type = typeDetector.detectType(path, typeStatistics);
                node.getValue().setType(type);
            }
        });
    }

    public void addNewNodeType(NodeData node) {
        var typeDetector = new TypeDetector();
        var type = typeDetector.detectType(node.getPath(), typeStatistics);
        node.setType(type);
    }

    public void decrementTypeCounter(NodeData node) {
        var foundedType = typeStatistics.stream()
                .filter(type -> type.getType().equals(node.getType()))
                .findFirst();

        foundedType.ifPresent(type -> {
            type.decrement();
            if(type.getQuantity() == 0) {
                typeStatistics.remove(type);
            }
        });
    }
}

