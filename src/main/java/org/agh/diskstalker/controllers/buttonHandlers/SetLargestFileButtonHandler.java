package org.agh.diskstalker.controllers.buttonHandlers;

import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.limits.LimitType;
import org.apache.commons.io.FileUtils;

public class SetLargestFileButtonHandler extends AbstractButtonSetLimitHandler {

    public SetLargestFileButtonHandler(MainController mainController) {
        super(mainController, mainController.getLargestFileField(), LimitType.LARGEST_FILE);
    }

    @Override
    protected long getValue(long value) {
        return value * FileUtils.ONE_MB;
    }
}
