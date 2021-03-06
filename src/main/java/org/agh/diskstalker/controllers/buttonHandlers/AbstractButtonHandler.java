package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.agh.diskstalker.alerts.AlertsFactory;
import org.agh.diskstalker.controllers.MainController;

public abstract class AbstractButtonHandler implements EventHandler<ActionEvent> {
    protected final MainController mainController;
    protected final AlertsFactory alertsFactory;

    public AbstractButtonHandler(MainController mainController) {
        this.mainController = mainController;
        this.alertsFactory = mainController.getAlertsFactory();
    }
}
