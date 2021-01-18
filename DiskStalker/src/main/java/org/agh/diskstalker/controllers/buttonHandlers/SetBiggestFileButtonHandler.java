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

public class SetBiggestFileButtonHandler implements EventHandler<ActionEvent> {
    private final DatabaseCommandExecutor commandExecutor;
    private final MainController mainController;

    public SetBiggestFileButtonHandler(MainController mainController, DatabaseCommandExecutor commandExecutor) {
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
                .ifPresent(this::setMaxSize);
    }

    private void setMaxSize(Path path){
        var maximumSize = Long.parseLong(mainController.getBiggestFileField().getText()) * FileUtils.ONE_MB;

        mainController.getFolderList()
                .getObservedFolderFromTreePath(path)
                .ifPresent(observedFolder -> {
                    observedFolder.getLimits().setBiggestFileLimit(maximumSize);
                    commandExecutor.executeCommand(new UpdateObservedFolderCommand(observedFolder));
                    Alerts.setBiggestFileAlert(path.toString(), maximumSize);
                });
    }
}
