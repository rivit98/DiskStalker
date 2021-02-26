package org.agh.diskstalker.events.observedFolderEvents;

import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.interfaces.ILimitableFolder;
import org.agh.diskstalker.model.limits.LimitType;

public abstract class AbstractObservedFolderLimitEvent implements ObservedFolderEvent {
    protected final LimitType limitType;
    protected final ILimitableFolder folder;

    public AbstractObservedFolderLimitEvent(ILimitableFolder folder, LimitType limitType) {
        this.folder = folder;
        this.limitType = limitType;
    }

    @Override
    public void dispatch(MainController mainController) {
        var limits = folder.getLimits();
        var wasShown = limits.wasShown(limitType);
        if (limits.isLimitExceeded(limitType)) {
            if (!wasShown) {
                mainController.refreshViews();
                mainController.getAlertsFactory().limitExceededAlert(
                        folder.getPath().toString(),
                        limitType,
                        limits.get(limitType)
                );
                limits.setShown(limitType);
            }
        } else if (wasShown) {
            limits.clearShown(limitType);
            mainController.refreshViews();
        }
    }
}
