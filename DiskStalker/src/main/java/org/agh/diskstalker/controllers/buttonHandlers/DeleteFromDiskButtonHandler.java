package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonType;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.controllers.alerts.Alerts;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class DeleteFromDiskButtonHandler implements EventHandler<ActionEvent> {
    private final MainController mainController;

    public DeleteFromDiskButtonHandler(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void handle(ActionEvent event) {
        Optional.ofNullable(
                mainController.getTreeTableView().getSelectionModel().getSelectedItem()
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

                mainController.removeTreeItem(item);
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
