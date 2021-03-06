package org.agh.diskstalker.controllers.buttonHandlers;

import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.limits.LimitType;
import org.apache.commons.io.FileUtils;

public class SetSizeButtonHandler extends AbstractButtonSetLimitHandler {
    public SetSizeButtonHandler(MainController mainController) {
        super(mainController, mainController.getMaxSizeField(), LimitType.TOTAL_SIZE);
    }

    @Override
    protected long getValue(long value) {
        return value * FileUtils.ONE_MB;
    }
}
