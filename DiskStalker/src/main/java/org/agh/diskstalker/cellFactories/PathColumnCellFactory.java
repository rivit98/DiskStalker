package org.agh.diskstalker.cellFactories;

import javafx.scene.control.TreeTableCell;
import org.agh.diskstalker.controllers.MainViewController;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.NodeData;

import java.nio.file.Path;
import java.util.Optional;

public class PathColumnCellFactory extends TreeTableCell<NodeData, Path> {
    private final MainViewController mainViewController;

    public PathColumnCellFactory(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
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
        mainViewController.getFolderList()
                .getObservedFolderFromTreePath(item)
                .ifPresent(folder -> {
                    setGraphic(GraphicsFactory.getGraphic(item.toFile().isDirectory(), folder.isSizeLimitExceeded()));
                });
    }

    private void fileNameEmptyHandler(Path item) {
        setText(item.toString());
    }
}
