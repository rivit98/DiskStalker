package controllers;

import model.FileData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.ObservableFolder;
import model.TreeFileNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class MainViewController {

    @FXML
    private TreeView<FileData> locationTreeView;

    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TextField directorySize;

    @FXML
    public void initialize() {
        createRoot();
        initializeAddButton();
    }

    public void createRoot() {
        locationTreeView.setRoot(new TreeItem<>());
        locationTreeView.setShowRoot(false);
        locationTreeView.getRoot().setExpanded(true);
    }

    public void addToMainTree(TreeFileNode node) {
        locationTreeView.getRoot().getChildren().add(node);
    }

    public void loadTreeItems(Path pathToWatch) {
        try {
            var folder = new ObservableFolder(pathToWatch);
            addToMainTree(folder.getTree());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void initializeAddButton() {
        addButton.setOnAction(this::addButtonClicked);
//        loadTreeItems(new File("./testDirs").toPath());
    }

    public void addButtonClicked(ActionEvent actionEvent) {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        directoryChooser.setTitle("Choose directory to watch");
        var selectedFolder = directoryChooser.showDialog(new Stage());
        //TODO: check for null here
        loadTreeItems(selectedFolder.toPath());
    }
}
