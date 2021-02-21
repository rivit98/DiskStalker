package org.agh.diskstalker.controllers.sortPolicies;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;
import org.agh.diskstalker.model.tree.NodeData;

import java.util.Comparator;

public class MainControllerSortPolicy implements Callback<TreeTableView<NodeData>, Boolean> {

    private static final Comparator<TreeItem<NodeData>> comparator =
            Comparator.comparing(
                    (TreeItem<NodeData> treeItem) -> treeItem
                            .getValue()
                            .getPath()
                            .getFileName()
            );


    @Override
    public Boolean call(TreeTableView<NodeData> param) {
        param.getRoot().getChildren().sort(comparator);
        return true;
    }
}
