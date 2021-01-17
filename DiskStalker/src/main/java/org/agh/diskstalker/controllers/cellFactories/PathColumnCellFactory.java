package org.agh.diskstalker.controllers.cellFactories;

import javafx.scene.control.TreeTableCell;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.NodeData;

import java.nio.file.Path;
import java.util.Optional;

public class PathColumnCellFactory extends TreeTableCell<NodeData, Path> {
    private final GraphicsFactory graphicsFactory = new GraphicsFactory();
    private final MainController mainController;

    public PathColumnCellFactory(MainController mainController) {
        this.mainController = mainController;
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
                        fileName -> fileNamePresentHandler(fileName, item),
                        () -> fileNameEmptyHandler(item)
                );
    }

    private void fileNamePresentHandler(Path fileName, Path item) {
        setText(fileName.toString());
        mainController.getFolderList()
                .getObservedFolderFromTreePath(item)
                .ifPresent(folder -> {
                    setGraphic(graphicsFactory.getGraphic(folder.getRoot().getValue().isDirectory(), folder.getLimits().isAnyLimitExceeded()));
                });
    }

    private void fileNameEmptyHandler(Path item) {
        setText(item.toString());
    }
}
