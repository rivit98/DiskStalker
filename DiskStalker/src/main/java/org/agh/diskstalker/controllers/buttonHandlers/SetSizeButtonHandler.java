package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.agh.diskstalker.controllers.Alerts;
import org.agh.diskstalker.controllers.MainViewController;
import org.agh.diskstalker.persistence.DatabaseCommandExecutor;
import org.agh.diskstalker.persistence.command.UpdateObservedFolderCommand;
import org.apache.commons.io.FileUtils;

import java.util.Optional;

public class SetSizeButtonHandler implements EventHandler<ActionEvent> {

    private final DatabaseCommandExecutor commandExecutor = new DatabaseCommandExecutor();
    private final MainViewController mainViewController;

    public SetSizeButtonHandler(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

    @Override
    public void handle(ActionEvent event) {
        var maximumSize = Long.parseLong(mainViewController.getMaxSizeField().getText()) * FileUtils.ONE_MB;
        Optional.ofNullable(
                mainViewController.getTreeTableView()
                        .getSelectionModel()
                        .getSelectedItem()
        ).map(treeItem -> treeItem.getValue().getPath()).ifPresent(path -> {
            mainViewController.getFolderList()
                    .getObservedFolderFromTreePath(path)
                    .ifPresent(observedFolder -> {
                        observedFolder.setMaximumSize(maximumSize);
                        commandExecutor.executeCommand(new UpdateObservedFolderCommand(observedFolder));
                        Alerts.setMaxSizeAlert(path.toString(), maximumSize);
                    });
        });

    }
}
