package model;

import java.nio.file.Path;


public class TreeBuilder {
    private final TreeFileNode root;

    public TreeBuilder(Path root) {
        this.root = new TreeFileNode(new FileData(root.toFile()));
    }

    public TreeFileNode addItem(FileData item) {
        // update 'fake root'
        if (item.getPath().equals(root.getValue().getPath())) { //TODO: think about reducing this check
            root.setValueEx(item);
            return root;
        }
        var node = new TreeFileNode(item);
        root.addNode(node);
        return node;
    }

    public TreeFileNode getRoot() {
        return root;
    }
}

