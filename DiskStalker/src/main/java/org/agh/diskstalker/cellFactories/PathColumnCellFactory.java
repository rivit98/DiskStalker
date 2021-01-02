package org.agh.diskstalker.cellFactories;

import javafx.scene.control.TreeTableCell;
import org.agh.diskstalker.controllers.MainView;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.NodeData;

import java.nio.file.Path;
import java.util.Optional;

public class PathColumnCellFactory extends TreeTableCell<NodeData, Path> {
    private final MainView mainView;

    public PathColumnCellFactory(MainView mainView) {
        this.mainView = mainView;
    }

    @Override
    protected void updateItem(Path item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
            setGraphic(null);
            return;
        }

        Optional.ofNullable(item.getFileName())
                .ifPresentOrElse(
                        fileName -> {
                            setText(fileName.toString());
                            var observedFolder = mainView.getObservedFolderFromTreePath(item);
                            observedFolder.ifPresent(folder -> {
                                setGraphic(GraphicsFactory.getGraphic(item.toFile().isDirectory(), folder.isSizeLimitExceeded()));
                            });
                        },
                        () -> setText(item.toString())
                );
    }
}
