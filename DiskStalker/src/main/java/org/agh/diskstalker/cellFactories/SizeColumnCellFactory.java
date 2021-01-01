package org.agh.diskstalker.cellFactories;

import javafx.scene.control.TreeTableCell;
import org.agh.diskstalker.model.NodeData;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;

public class SizeColumnCellFactory extends TreeTableCell<NodeData, Number> {
    @Override
    protected void updateItem(Number value, boolean empty) {
        super.updateItem(value, empty);
        if (value == null || empty) {
            setText(null);
        } else {
            setText(FileUtils.byteCountToDisplaySize(value.longValue()));
        }
    }
}
