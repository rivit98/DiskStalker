package controllers;

import filesystemWatcher.RecursiveTreeBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

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
        var treeItem = new TreeItem<File>();
        locationTreeView.setRoot(treeItem);
        locationTreeView.setShowRoot(false);
        locationTreeView.getRoot().setExpanded(true);
        initializeAddButton();
    }

    private void initializeAddButton(){
        addButton.setOnAction(e -> {
            var directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File("."));
            directoryChooser.setTitle("Choose directory to watch");
            var selectedFolder = directoryChooser.showDialog(new Stage());
            locationTreeView.getRoot().getChildren().add(new RecursiveTreeBuilder(selectedFolder));
        });
    }

    public void addButtonClicked(ActionEvent actionEvent) {
    }

    public void deleteButtonClicked(ActionEvent actionEvent) {
    }
}
