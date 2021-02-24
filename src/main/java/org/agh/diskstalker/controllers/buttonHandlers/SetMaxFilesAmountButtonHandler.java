package org.agh.diskstalker.controllers.buttonHandlers;

import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.limits.LimitType;

public class SetMaxFilesAmountButtonHandler extends AbstractButtonSetLimitHandler {

    public SetMaxFilesAmountButtonHandler(MainController mainController) {
        super(mainController, mainController.getMaxFilesAmountField(), LimitType.FILES_AMOUNT);
    }
}
