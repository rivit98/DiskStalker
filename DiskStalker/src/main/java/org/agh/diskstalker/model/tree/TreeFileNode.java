package org.agh.diskstalker.model.tree;

import javafx.scene.control.TreeItem;
import org.agh.diskstalker.model.NodeData;

import java.util.Objects;
import java.util.Optional;


public class TreeFileNode extends TreeItem<NodeData> {
    public TreeFileNode(NodeData nodeData) {
        super(nodeData);
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

    private void updateParentSize(TreeItem<NodeData> node, long deltaSize) {
        Optional.ofNullable(node.getParent())
                .ifPresent(parent -> {
                    Optional.ofNullable(parent.getValue())
                            .ifPresent(value -> {
                                value.setSize(deltaSize);
                                updateParentSize(parent, deltaSize);
                            });
                });
    }

    public boolean deleteMe() {
        updateParentSize(this, -getValue().getSize());
        return Optional.ofNullable(this.getParent())
                .map(TreeItem::getChildren)
                .map(childrenList -> childrenList.remove(this))
                .orElse(false);
    }

    public void updateMe() {
        var nodeData = getValue();
        var oldSize = nodeData.getSize();
        nodeData.updateModificationTime();
        updateParentSize(this, nodeData.updateFileSize() - oldSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var node = (TreeFileNode) o;
        return Objects.equals(getValue(), node.getValue());
    }
}
