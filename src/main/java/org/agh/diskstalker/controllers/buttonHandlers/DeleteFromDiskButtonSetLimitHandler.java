package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import org.agh.diskstalker.controllers.MainController;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class DeleteFromDiskButtonSetLimitHandler extends AbstractButtonSetLimitHandler {

    public DeleteFromDiskButtonSetLimitHandler(MainController mainController) {
        super(mainController);
    }

    @Override
    public void handle(ActionEvent event) {
        mainController.getSelectedItem().ifPresent(item -> {
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
                mainController.getAlertsFactory().genericErrorAlert(searchedPath, "Cannot delete file");
                log.error("Cannot delete file" + searchedPath, e);
            }
        });
    }

    private boolean askIfDelete(Path toDeletePath) {
        var buttonType = mainController.getAlertsFactory().yesNoDeleteAlert(toDeletePath);
        return buttonType.equals(ButtonType.YES);
    }
}
