package model;

import javafx.scene.control.TreeItem;

import java.nio.file.Path;
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
    public void insertNode(TreeFileNode node) { //TODO: rewrite this
        var isDir = node.getValue().isDirectory();
        var targetName = node.getValue().getPath();
        int index = 0;
        var cachedList = getChildren();
        for (var ch : cachedList) {
            var tnode = (TreeFileNode) ch;
            var tnodeIsDir = tnode.getValue().isDirectory();

            if (!isDir && tnodeIsDir) { // we want to put file, so skip all dirs
                index++;
                continue;
            }

            if (isDir && !tnodeIsDir) { // no more dirs, so our is last
                break;
            }

            var tnodeName = tnode.getValue().getPath();
            if (targetName.compareTo(tnodeName) > 0) { //compare names to determine order
                index++;
                continue;
            }

            index++;
            break;
        }

        cachedList.add(index, node);
        getValue().modifySize(node.getValue().getSize());
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

                if (TreeFileNode.isChild(tnode.getValue().getPath(), node.getValue().getPath())) {
                    getValue().modifySize(node.getValue().getSize());
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

    public void deleteMe() {
        updateParentSize(this, -getValue().getSize());
        this.getParent().getChildren().remove(this);
    }

    private void updateParentSize(TreeItem<FileData> node, long deltaSize) {
        var parentNode = Optional.ofNullable(node.getParent());
        parentNode.ifPresent(parent -> {
            if (parent.getValue() != null) {
                parent.getValue().modifySize(deltaSize);
                updateParentSize(parent, deltaSize);
            }
        });
    }

    public void updateMe(){
        var fileData = getValue();
        var oldSize = fileData.getSize();
        updateParentSize(this, fileData.updateFileSize() - oldSize);
    }
}
