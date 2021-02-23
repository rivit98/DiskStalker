package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.limits.LimitType;
import org.agh.diskstalker.persistence.command.UpdateObservedFolderCommand;

import java.nio.file.Path;

public class SetMaxFilesAmountButtonHandler extends AbstractButtonSetLimitHandler {

    public SetMaxFilesAmountButtonHandler(MainController mainController) {
        super(mainController, mainController.getMaxFilesAmountField(), LimitType.FILES_AMOUNT);
    }
}
