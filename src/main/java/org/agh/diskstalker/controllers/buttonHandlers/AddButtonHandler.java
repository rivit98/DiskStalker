package org.agh.diskstalker.controllers.buttonHandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.controllers.alerts.Alerts;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.persistence.command.CreateObservedFolderCommand;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public class AddButtonHandler implements EventHandler<ActionEvent> {
    private final MainController mainController;

    public AddButtonHandler(MainController mainController) {
        this.mainController = mainController;
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
            mainController.observeFolderEvents(folder);

            mainController.getCommandExecutor().executeCommand(new CreateObservedFolderCommand(folder));
        }
    }

    private boolean checkIfFolderAlreadyExists(Path path){
        return Optional.ofNullable(mainController.getTreeTableView().getRoot())
                .map(TreeItem::getChildren)
                .map(Collection::stream)
                .map(stream -> stream.anyMatch(children -> children.getValue().getPath().equals(path)))
                .orElse(false);
    }
}
