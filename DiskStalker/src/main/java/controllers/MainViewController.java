package controllers;

import filesystemWatcher.FileData;
import filesystemWatcher.FileTreeScanner;
import filesystemWatcher.SimpleFileTreeItem;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import models.ObservableFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class MainViewController {

    @FXML
    private TreeView<File> locationTreeView;

    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TextField directorySize;

    @FXML
    public void initialize(){
        locationTreeView.setRoot(new TreeItem<>());
        locationTreeView.setShowRoot(false);
        locationTreeView.getRoot().setExpanded(true);
        initializeAddButton();
    }

    public void loadTreeItems(File dirToWatch) {
        try{
            var folder = new ObservableFolder(dirToWatch.toPath());

            locationTreeView.getRoot().getChildren().add(folder.getTree());
        }catch (IOException exception){
            exception.printStackTrace();
        }
    }

    private void initializeAddButton(){
        addButton.setOnAction(e -> {
            var directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File("."));
            directoryChooser.setTitle("Choose directory to watch");
            var selectedFolder = directoryChooser.showDialog(new Stage());
            loadTreeItems(selectedFolder);
        });
    }

    public void addButtonClicked(ActionEvent actionEvent) {
    }

    public void deleteButtonClicked(ActionEvent actionEvent) {
    }
}
