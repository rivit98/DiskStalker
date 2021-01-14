package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.agh.diskstalker.controllers.alerts.Alerts;
import org.agh.diskstalker.controllers.MainViewController;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.persistence.DatabaseCommandExecutor;
import org.agh.diskstalker.persistence.command.SaveObservedFolderCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public class AddButtonHandler implements EventHandler<ActionEvent> {
    private final MainViewController mainViewController;
    private final DatabaseCommandExecutor commandExecutor;

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
                .ifPresent(this::addPath);
    }

    private void addPath(Path path) {
        if (checkIfFolderAlreadyExists(path)) {
            Alerts.tryingToAddSameFolderToObservedAlert();
        } else {
            var folder = new ObservedFolder(path);
            mainViewController.observeFolderEvents(folder);

            commandExecutor.executeCommand(new SaveObservedFolderCommand(folder));
        }
    }

    private boolean checkIfFolderAlreadyExists(Path path){
        return Optional.ofNullable(mainViewController.getTreeTableView().getRoot())
                .map(TreeItem::getChildren)
                .map(Collection::stream)
                .map(stream -> stream.anyMatch(children -> children.getValue().getPath().equals(path)))
                .orElse(false);
    }
}
