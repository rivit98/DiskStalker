package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.limits.LimitType;
import org.agh.diskstalker.persistence.command.UpdateObservedFolderCommand;

import java.nio.file.Path;

public abstract class AbstractButtonSetLimitHandler extends AbstractButtonHandler{
    private final TextField textField;
    private final LimitType limitType;

    public AbstractButtonSetLimitHandler(MainController mainController, TextField textField, LimitType limitType) {
        super(mainController);
        this.textField = textField;
        this.limitType = limitType;
    }

    @Override
    public void handle(ActionEvent event) {
        mainController.getSelectedItem()
                .map(treeItem -> treeItem.getValue().getPath())
                .ifPresent(path -> set(
                        path,
                        getValue(Long.parseLong(textField.getText()))
                ));
    }

    protected long getValue(long value){
        return value;
    }

    private void set(Path path, long value){
        mainController.getFolderList()
                .getObservedFolderFromTreePath(path)
                .ifPresent(observedFolder -> {
                    alertsFactory.setLimit(path.toString(), limitType, value);
                    observedFolder.getLimits().setLimit(limitType, value);
                    mainController.getCommandExecutor().executeCommand(new UpdateObservedFolderCommand(observedFolder));
                });
    }
}
