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

public class SetSizeButtonHandler implements EventHandler<ActionEvent> {
    private final DatabaseCommandExecutor commandExecutor;
    private final MainController mainController;

    public SetSizeButtonHandler(MainController mainController, DatabaseCommandExecutor commandExecutor) {
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
        var maximumSize = Long.parseLong(mainController.getMaxSizeField().getText()) * FileUtils.ONE_MB;

        mainController.getFolderList()
                .getObservedFolderFromTreePath(path)
                .ifPresent(observedFolder -> {
                    observedFolder.getLimits().setMaxTotalSize(maximumSize);
                    commandExecutor.executeCommand(new UpdateObservedFolderCommand(observedFolder));
                    Alerts.setMaxSizeAlert(path.toString(), maximumSize);
                });
    }
}
