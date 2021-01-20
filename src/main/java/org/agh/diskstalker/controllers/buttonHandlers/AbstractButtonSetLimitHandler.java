package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.agh.diskstalker.controllers.MainController;

public abstract class AbstractButtonSetLimitHandler implements EventHandler<ActionEvent> {
    protected final MainController mainController;

    public AbstractButtonSetLimitHandler(MainController mainController) {
        this.mainController = mainController;
    }


}
