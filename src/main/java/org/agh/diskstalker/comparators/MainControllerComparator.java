package org.agh.diskstalker.comparators;

import javafx.scene.control.TreeItem;
import org.agh.diskstalker.model.NodeData;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class MainControllerComparator {

    private MainControllerComparator() {

    }

    public static Comparator<TreeItem<NodeData>> getComparator() {
        return Comparator
                .comparing((TreeItem<NodeData> treeItem) -> treeItem
                        .getValue()
                        .getPath()
                        .getFileName());
    }
}
