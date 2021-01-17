package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.agh.diskstalker.controllers.MainController;

import java.util.Optional;

public class StopObserveButtonHandler implements EventHandler<ActionEvent> {
    private final MainController mainController;

    public StopObserveButtonHandler(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void handle(ActionEvent event) {
        Optional.ofNullable(
                mainController.getTreeTableView().getSelectionModel().getSelectedItem()
        ).ifPresent(item -> {
            if(mainController.removeTreeItem(item)){
                mainController.getMaxSizeField().clear();
                mainController.getMaxFilesAmountField().clear();
                mainController.getBiggestFileField().clear();
            }
        });
    }
}
