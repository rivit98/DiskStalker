package model.tree;

import javafx.scene.control.TreeItem;
import model.FileData;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;


public class TreeFileNode extends TreeItem<FileData> {
    public TreeFileNode(FileData fileData) {
        super(fileData);
    }

    @Override
    public boolean isLeaf() {
        return getValue().isFile();
    }

    // inserts node and keeps proper ordering
    public void insertNode(TreeFileNode node) {
        var value = node.getValue();
        var isDir = value.isDirectory();
        var targetName = value.getPath();
        var index = 0;
        var cachedList = getChildren();
        for (var childNode : cachedList) {
            var tnodeIsDir = childNode.getValue().isDirectory();

            if (!isDir && tnodeIsDir) { // we want to put file, so skip all dirs
                index++;
                continue;
            }

            if (isDir && !tnodeIsDir) { // no more dirs, so our is last
                break;
            }
            index++;

            var tnodeName = childNode.getValue().getPath();
            if (targetName.compareTo(tnodeName) > 0) { //compare names to determine order
                continue;
            }

            break;
        }

        cachedList.add(index, node);
        updateParentSize(node, value.getSize());
    }

    public void addNode(TreeFileNode node) {
        var relativizedPath = this.getValue().getPath().relativize(node.getValue().getPath()); // strip common part
        int relativePathDepth = relativizedPath.getNameCount();

        if (relativePathDepth == 1) { // we are adding new node to current node (folder or file)
            insertNode(node);
        } else {
            // loop over childs, find the proper one and enter
            for (var ch : getChildren()) {
                var tnode = (TreeFileNode) ch;
                if (tnode.getValue().isFile()) {
                    continue;
                }

                if (isChild(tnode.getValue().getPath(), node.getValue().getPath())) {
                    tnode.addNode(node);
                    return;
                }
            }

            throw new IllegalStateException("addNode failed! | " + node.getValue().getPath());
        }
    }

    private static boolean isChild(Path parent, Path child) {
        var absoluteParentPath = parent.normalize().toAbsolutePath();
        var absoluteChildPath = child.normalize().toAbsolutePath();

        return absoluteChildPath.startsWith(absoluteParentPath);
    }

    private void updateParentSize(TreeItem<FileData> node, long deltaSize) {
        Optional.ofNullable(node.getParent())
                .ifPresent(parent -> {
                    Optional.ofNullable(parent.getValue())
                            .ifPresent(value -> {
                                value.modifySize(deltaSize);
                                updateParentSize(parent, deltaSize);
                            });
                });
    }

    public void deleteMe() {
        updateParentSize(this, -getValue().getSize());
        this.getParent().getChildren().remove(this);
    }

    public void updateMe() {
        var fileData = getValue();
        var oldSize = fileData.getSize();
        updateParentSize(this, fileData.updateFileSize() - oldSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var node = (TreeFileNode) o;
        return Objects.equals(getValue(), node.getValue());
    }
}
