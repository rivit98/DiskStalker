package org.agh.diskstalker.cellFactories;

import javafx.scene.control.ListCell;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.ObservedFolder;

public class FolderColumnCellFactory extends ListCell<ObservedFolder> {
    @Override
    protected void updateItem(ObservedFolder folder, boolean empty) {
        super.updateItem(folder, empty);
        if(empty) {
            setText(null);
            setGraphic(null);
            return;
        }

        fileNamePresentHandler(folder);
    }

    private void fileNamePresentHandler(ObservedFolder folder) {
        setText(folder.getPath().getFileName().toString()); //FIXME: use getName
        setGraphic(GraphicsFactory.getGraphic(true, folder.isSizeLimitExceeded()));
    }
}
