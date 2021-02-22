package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;
import org.agh.diskstalker.model.limits.LimitType;

public abstract class AbstractObservedFolderLimitEvent extends AbstractObservedFolderEvent {
    protected LimitType limitType;

    public AbstractObservedFolderLimitEvent(ILimitableObservableFolder folder) {
        super(folder);
    }

    @Override
    public void dispatch(MainController mainController) {
        var limits = folder.getLimits();
        if (limits.isLimitExceeded(limitType)) {
            if (!limits.wasShown(limitType)) {
                mainController.getAlertsFactory().biggestFileExceededAlert(
                        folder.getPath().toString(),
                        limits.get(limitType)
                );
                limits.setShown(limitType);
                mainController.refreshViews();
            }
        } else {
            limits.clearShown(limitType);
            mainController.refreshViews();
        }
    }
}
