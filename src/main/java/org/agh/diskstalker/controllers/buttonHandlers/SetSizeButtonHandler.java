package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.limits.LimitType;
import org.agh.diskstalker.persistence.command.UpdateObservedFolderCommand;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;

public class SetSizeButtonHandler extends AbstractButtonSetLimitHandler {
    public SetSizeButtonHandler(MainController mainController) {
        super(mainController, mainController.getMaxSizeField(), LimitType.TOTAL_SIZE);
    }

    @Override
    protected long getValue(long value) {
        return value * FileUtils.ONE_MB;
    }
}
