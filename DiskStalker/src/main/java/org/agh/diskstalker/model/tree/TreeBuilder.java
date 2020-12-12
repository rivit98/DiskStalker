package org.agh.diskstalker.model.tree;

import io.reactivex.rxjava3.subjects.SingleSubject;
import javafx.scene.control.TreeItem;
import org.agh.diskstalker.model.FileData;

import java.nio.file.Path;
import java.util.HashMap;


public class TreeBuilder {
    private final HashMap<Path, TreeFileNode> pathToTreeMap = new HashMap<>();
    private final SingleSubject<TreeFileNode> rootSubject = SingleSubject.create();
    private TreeFileNode root;

    public void processFileData(FileData fileData) {
        var insertedNode = new TreeFileNode(fileData);
        pathToTreeMap.put(fileData.getPath(), insertedNode);

        if (root != null) {
            insertNewNode(insertedNode);
        } else {
            root = insertedNode;
            rootSubject.onSuccess(root);
        }
    }

    public void insertNewNode(TreeFileNode newNode) {
        var fileData = newNode.getValue();
        var parentPath = fileData.getPath().getParent();
        var parentNode = pathToTreeMap.get(parentPath);
        parentNode.insertNode(newNode);
        pathToTreeMap.put(fileData.getPath(), newNode);
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

    public void removeMappedDirsRecursively(TreeItem<FileData> node) {
        removeMappedDirs(node);
        node.getChildren().forEach(this::removeMappedDirsRecursively);
    }

    public void removeMappedDirs(TreeItem<FileData> node) {
        pathToTreeMap.remove(node.getValue().getPath());
    }
}

