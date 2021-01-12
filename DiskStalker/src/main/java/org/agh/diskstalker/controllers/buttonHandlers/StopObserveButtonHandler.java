package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.agh.diskstalker.controllers.MainViewController;

import java.util.Optional;

public class StopObserveButtonHandler implements EventHandler<ActionEvent> {
    private final MainViewController mainViewController;

    public StopObserveButtonHandler(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

    @Override
    public void handle(ActionEvent event) {
        Optional.ofNullable(
                mainViewController.getTreeTableView().getSelectionModel().getSelectedItem()
        ).ifPresent(item -> {
            if(mainViewController.removeTreeItem(item)){
                mainViewController.getMaxSizeField().clear();
            }
        });
    }
}
