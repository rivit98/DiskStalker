package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.persistence.command.UpdateObservedFolderCommand;

import java.nio.file.Path;

public class SetMaxFilesAmountButtonHandler extends AbstractButtonSetLimitHandler {

    public SetMaxFilesAmountButtonHandler(MainController mainController) {
        super(mainController);
    }

    @Override
    public void handle(ActionEvent event) {
        mainController.getSelectedItem()
                .map(treeItem -> treeItem.getValue().getPath())
                .ifPresent(this::setMaxFilesAmount);
    }

    private void setMaxFilesAmount(Path path){
        var maximumAmount = Long.parseLong(mainController.getMaxFilesAmountField().getText());

        mainController.getFolderList()
                .getObservedFolderFromTreePath(path)
                .ifPresent(observedFolder -> {
                    mainController.getAlertsFactory().setMaxFilesAmountAlert(path.toString(), maximumAmount);
                    observedFolder.getLimits().setMaxFilesAmount(maximumAmount);
                    mainController.getCommandExecutor().executeCommand(new UpdateObservedFolderCommand(observedFolder));
                });
    }
}
