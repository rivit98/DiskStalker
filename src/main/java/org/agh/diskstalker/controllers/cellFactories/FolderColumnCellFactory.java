package org.agh.diskstalker.controllers.cellFactories;

import javafx.scene.control.ListCell;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;

public class FolderColumnCellFactory extends ListCell<ILimitableObservableFolder> {
    private final GraphicsFactory graphicsFactory;

    public FolderColumnCellFactory(GraphicsFactory graphicsFactory) {
        this.graphicsFactory = graphicsFactory;
    }

    @Override
    protected void updateItem(ILimitableObservableFolder folder, boolean empty) {
        super.updateItem(folder, empty);
        if(empty) {
            setText(null);
            setGraphic(null);
            return;
        }

        setText(folder.getPath().getFileName().toString());
        if(folder.isScanning()){
            setGraphic(graphicsFactory.getLoadingGraphics());
        }else{
            setGraphic(graphicsFactory.getGraphic(true, folder.getLimits().isAnyLimitExceeded()));
        }
    }
}
