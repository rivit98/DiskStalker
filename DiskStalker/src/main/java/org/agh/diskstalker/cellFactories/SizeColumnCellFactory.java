package org.agh.diskstalker.cellFactories;

import javafx.css.PseudoClass;
import javafx.scene.control.TreeTableCell;
import org.agh.diskstalker.model.NodeData;
import org.apache.commons.io.FileUtils;

public class SizeColumnCellFactory extends TreeTableCell<NodeData, Number> {
    public SizeColumnCellFactory() {
        pseudoClassStateChanged(PseudoClass.getPseudoClass("centered"), true);
    }

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
