package model;

import java.nio.file.Path;


public class TreeBuilder {
    private final TreeFileNode root;

    public TreeBuilder(Path root) {
        this.root = new TreeFileNode(new FileData(root.toFile()));
    }

    public void addItem(FileData item) {
        // update 'fake root'
        if (item.getPath().equals(root.getValue().getPath())) {
            root.setValueEx(item);
            return;
        }
        var node = new TreeFileNode(item);
        root.addNode(node);
    }

    public TreeFileNode getRoot() {
        return root;
    }
}

