package models;

import javafx.scene.control.TreeItem;

public class TreeBuilder<T> {
    private final TreeItem<T> filesTree;

    public TreeBuilder(T root) {
        filesTree = new TreeItem<>(root);
    }

    public void addItem(T item) {
        //TODO: implement logic for adding items to proper children
        filesTree.getChildren().add(new TreeItem<>(item));
    }

    public TreeItem<T> getFilesTree() {
        return filesTree;
    }
}
