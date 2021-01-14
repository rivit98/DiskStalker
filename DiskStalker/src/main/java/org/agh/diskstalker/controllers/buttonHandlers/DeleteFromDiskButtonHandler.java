package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonType;
import org.agh.diskstalker.controllers.alerts.Alerts;
import org.agh.diskstalker.controllers.MainViewController;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class DeleteFromDiskButtonHandler implements EventHandler<ActionEvent> {
    private final MainViewController mainViewController;

    public DeleteFromDiskButtonHandler(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

    @Override
    public void handle(ActionEvent event) {
        Optional.ofNullable(
                mainViewController.getTreeTableView().getSelectionModel().getSelectedItem()
        ).ifPresent(item -> {
            var nodeData = item.getValue();
            var searchedPath = nodeData.getPath();
            if (!askIfDelete(searchedPath)) {
                return;
            }

            try {
                var searchedFile = searchedPath.toFile();
                if (nodeData.isDirectory()) {
                    FileUtils.deleteDirectory(searchedFile);
                } else {
                    searchedFile.delete();
                }

                mainViewController.removeTreeItem(item);
            } catch (IOException | IllegalArgumentException e) {
                Alerts.genericErrorAlert(searchedPath, "Cannot delete file");
                e.printStackTrace(); //TODO: logger
            }
        });
    }

    private boolean askIfDelete(Path toDeletePath) {
        var buttonType = Alerts.yesNoDeleteAlert(toDeletePath);
        return buttonType.equals(ButtonType.YES);
    }
}
