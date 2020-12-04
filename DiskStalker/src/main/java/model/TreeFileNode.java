package model;

import javafx.scene.control.TreeItem;

import java.nio.file.Path;


public class TreeFileNode extends TreeItem<FileData> {
    public TreeFileNode(FileData fileData) {
        super(fileData);
    }

    public void setValueEx(FileData value) {
        super.setValue(value);
    }

    // inserts node and keeps proper ordering
    public void insertNode(TreeFileNode node) { //TODO: rewrite this
        var isDir = node.getValue().isDirectory();
        var targetName = node.getValue().getFile().getName();
        int index = 0;
        var cachedList = getChildren();
        for (var ch : cachedList) { //TODO: is it better than sorting?
            var tnode = (TreeFileNode) ch; //TODO: is it possible to do this without cast?
            var tnodeIsDir = tnode.getValue().isDirectory();

            if (!isDir && tnodeIsDir) { // we want to put file, so skip all dirs
                index++;
                continue;
            }

            if (isDir && !tnodeIsDir) { // no more dirs, so our is last
                break;
            }

            var tnodeName = tnode.getValue().getFile().getName();
            if (targetName.compareTo(tnodeName) > 0) { //compare names to determine order
                index++;
                continue;
            }

            index++;
            break;
        }

//        getChildren().add(index, node);
        cachedList.add(index, node);
        getValue().setSize(node.getValue().size());
    }

    public void addNode(TreeFileNode node) {
        var relativizedPath = this.getValue().getPath().relativize(node.getValue().getPath()); // strip common part
        int relativePathDepth = relativizedPath.getNameCount();

        if (relativePathDepth == 1) { // we are adding new node to current node (folder or file)
            insertNode(node);
        } else {
            // loop over childs, find the proper one and enter
            for (var ch : getChildren()) {
                var tnode = (TreeFileNode) ch; //TODO: is it possible to do this without cast?
                if (tnode.getValue().isFile()) {
                    continue;
                }

                if (TreeFileNode.isChild(tnode.getValue().getPath(), node.getValue().getPath())) {
                    getValue().modifySize(node.getValue().size());
                    tnode.addNode(node);
                    return;
                }
            }

            throw new IllegalStateException("Cos sie popsulo i nie dodalo mnie :(");
        }
    }

    private static boolean isChild(Path parent, Path child) {
        var absoluteParentPath = parent.normalize().toAbsolutePath();
        var absoluteChildPath = child.normalize().toAbsolutePath();

        return absoluteChildPath.startsWith(absoluteParentPath);
    }
}
