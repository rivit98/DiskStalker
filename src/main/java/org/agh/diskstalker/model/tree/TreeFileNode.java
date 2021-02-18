package org.agh.diskstalker.model.tree;

import javafx.scene.control.TreeItem;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;


public class TreeFileNode extends TreeItem<NodeData> {
    public TreeFileNode(NodeData nodeData) {
        super(nodeData);
    }

    public void insertNode(TreeFileNode node) {
        var currentValue = node.getValue();
        var cachedList = getChildren();
        var index = cachedList.stream().takeWhile(childNode -> currentValue.compareTo(childNode.getValue()) > 0).count();

        cachedList.add((int) index, node);
        updateParentSize(node, currentValue.getAccumulatedSize());
    }

    private void updateParentSize(TreeItem<NodeData> node, long deltaSize) {
        Optional.ofNullable(node.getParent())
                .ifPresent(parent -> updateParentSizeWorker(parent, deltaSize));
    }

    private void updateParentSizeWorker(TreeItem<NodeData> parent, long deltaSize){
        Optional.ofNullable(parent.getValue())
                .ifPresent(value -> {
                    value.modifyAccumulatedSize(deltaSize);
                    parent.getChildren().sort(Comparator.comparing(TreeItem::getValue));
                    updateParentSize(parent, deltaSize);
                });
    }

    public boolean deleteMe() {
        updateParentSize(this, -getValue().getAccumulatedSize());
        return Optional.ofNullable(this.getParent())
                .map(TreeItem::getChildren)
                .map(childrenList -> childrenList.remove(this))
                .orElse(false);
    }

    public void updateMe() {
        var nodeData = getValue();
        var oldSize = nodeData.getAccumulatedSize();
        nodeData.updateFileData();
        updateParentSize(this, nodeData.getAccumulatedSize() - oldSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var node = (TreeFileNode) o;
        return Objects.equals(getValue(), node.getValue());
    }
}
