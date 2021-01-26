package org.agh.diskstalker.controllers.sortPolicies;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;
import org.agh.diskstalker.model.NodeData;

import java.util.Comparator;

public class MainControllerSortPolicy implements Callback<TreeTableView<NodeData>, Boolean> {

    private Comparator<TreeItem<NodeData>> getComparator() {
        return Comparator
                .comparing((TreeItem<NodeData> treeItem) -> treeItem
                        .getValue()
                        .getPath()
                        .getFileName());
    }

    @Override
    public Boolean call(TreeTableView<NodeData> param) {
        var comparator = this.getComparator();
        param.getRoot().getChildren().sort(comparator);
        return true;
    }
}
