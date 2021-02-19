package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.persistence.command.UpdateObservedFolderCommand;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;

public class SetBiggestFileButtonHandler extends AbstractButtonSetLimitHandler {

    public SetBiggestFileButtonHandler(MainController mainController) {
        super(mainController);
    }

    @Override
    public void handle(ActionEvent event) {
        mainController.getSelectedItem()
                .map(treeItem -> treeItem.getValue().getPath())
                .ifPresent(this::setMaxSize);
    }

    private void setMaxSize(Path path){
        var maximumSize = Long.parseLong(mainController.getBiggestFileField().getText()) * FileUtils.ONE_MB;

        mainController.getFolderList()
                .getObservedFolderFromTreePath(path)
                .ifPresent(observedFolder -> {
                    mainController.getAlertsFactory().setBiggestFileAlert(path.toString(), maximumSize);
                    observedFolder.getLimits().setBiggestFileLimit(maximumSize);
                    mainController.getCommandExecutor().executeCommand(new UpdateObservedFolderCommand(observedFolder));
                });
    }
}
