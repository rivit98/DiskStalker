package org.agh.diskstalker.controllers.buttonHandlers;

import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.limits.LimitType;
import org.agh.diskstalker.persistence.command.UpdateObservedFolderCommand;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;

public class SetBiggestFileButtonHandler extends AbstractButtonSetLimitHandler {

    public SetBiggestFileButtonHandler(MainController mainController) {
        super(mainController, mainController.getBiggestFileField(), LimitType.BIGGEST_FILE);
    }

    @Override
    protected long getValue(long value) {
        return value * FileUtils.ONE_MB;
    }
}
