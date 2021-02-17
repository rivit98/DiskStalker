package org.agh.diskstalker.controllers.cellFactories;

import javafx.scene.control.TreeTableCell;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;

import java.nio.file.Path;
import java.util.Optional;

public class PathColumnCellFactory extends TreeTableCell<NodeData, Path> {
    private final GraphicsFactory graphicsFactory;
    private final MainController mainController;

    public PathColumnCellFactory(MainController mainController) {
        this.mainController = mainController;
        this.graphicsFactory = mainController.getGraphicsFactory();
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
                .map(Path::toString)
                .ifPresentOrElse(
                        filename -> fileNamePresentHandler(filename, item),
                        () -> fileNameEmptyHandler(item)
                );
    }

    private void fileNamePresentHandler(String fileName, Path item) {
        setText(fileName);
        mainController.getFolderList()
                .getObservedFolderFromTreePath(item)
                .ifPresentOrElse(
                        folder -> setGraphics(folder, item),
                        this::setLoadingGraphics
                );
    }

    private void setGraphics(ObservedFolder folder, Path item){
        if(folder.isScanning()){
            setLoadingGraphics();
        }else{
            var node = folder.getNodeByPath(item);
            var image = graphicsFactory.getGraphic(node.getValue().isDirectory(), folder.getLimits().isAnyLimitExceeded());
            setGraphic(image);
        }
    }

    private void setLoadingGraphics(){
        setGraphic(graphicsFactory.getLoadingGraphics());
    }

    private void fileNameEmptyHandler(Path item) {
        setText(item.toString());
    }
}
