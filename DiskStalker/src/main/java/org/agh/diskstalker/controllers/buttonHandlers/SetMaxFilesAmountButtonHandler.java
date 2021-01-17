package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.controllers.alerts.Alerts;
import org.agh.diskstalker.persistence.DatabaseCommandExecutor;
import org.agh.diskstalker.persistence.command.UpdateObservedFolderCommand;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.util.Optional;

public class SetMaxFilesAmountButtonHandler implements EventHandler<ActionEvent> {
    private final DatabaseCommandExecutor commandExecutor;
    private final MainController mainController;

    public SetMaxFilesAmountButtonHandler(MainController mainController, DatabaseCommandExecutor commandExecutor) {
        this.mainController = mainController;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public void handle(ActionEvent event) {
        Optional.ofNullable(
                mainController.getTreeTableView()
                        .getSelectionModel()
                        .getSelectedItem()
        )
                .map(treeItem -> treeItem.getValue().getPath())
                .ifPresent(this::setMaxFilesAmount);
    }

    private void setMaxFilesAmount(Path path){
        var maximumAmount = Long.parseLong(mainController.getMaxFilesAmountField().getText());

        mainController.getFolderList()
                .getObservedFolderFromTreePath(path)
                .ifPresent(observedFolder -> {
                    observedFolder.setMaximumFilesAmount(maximumAmount);
                    commandExecutor.executeCommand(new UpdateObservedFolderCommand(observedFolder));
                    Alerts.setMaxFilesAmountAlert(path.toString(), maximumAmount);
                });
    }
}
