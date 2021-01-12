package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.agh.diskstalker.controllers.Alerts;
import org.agh.diskstalker.controllers.MainViewController;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.persistence.DatabaseCommandExecutor;
import org.agh.diskstalker.persistence.command.SaveObservedFolderCommand;

import java.io.File;
import java.util.Optional;

public class AddButtonHandler implements EventHandler<ActionEvent> {

    private final DatabaseCommandExecutor commandExecutor;
    private final MainViewController mainViewController;

    public AddButtonHandler(MainViewController mainViewController, DatabaseCommandExecutor commandExecutor) {
        this.mainViewController = mainViewController;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public void handle(ActionEvent event) {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        directoryChooser.setTitle("Choose directory to watch");

        Optional.ofNullable(directoryChooser.showDialog(new Stage()))
                .map(File::toPath)
                .ifPresent(path -> {
                    var samePathExists = mainViewController.getTreeTableView().getRoot().getChildren().stream()
                            .anyMatch(children -> children.getValue().getPath().equals(path));

                    if (samePathExists) {
                        Alerts.tryingToAddSameFolderToObservedAlert();
                    } else {
                        var folder = new ObservedFolder(path);
                        mainViewController.observeFolderEvents(folder);

                        commandExecutor.executeCommand(new SaveObservedFolderCommand(folder));
                    }
                });
    }
}
