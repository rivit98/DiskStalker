package org.agh.diskstalker.cellFactories;

import javafx.scene.control.TreeTableCell;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.NodeData;

import java.nio.file.Path;
import java.util.Optional;

public class PathColumnCellFactory extends TreeTableCell<NodeData, Path> {
    @Override
    protected void updateItem(Path item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
            setGraphic(null);
        } else {
            Optional.ofNullable(item.getFileName())
                    .ifPresentOrElse(
                            fname -> setText(fname.toString()),
                            () -> setText(item.toString())
                    );

            setGraphic(GraphicsFactory.getGraphic(item.toFile().isDirectory()));
        }
    }
}
