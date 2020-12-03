package controllers;


import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

import model.FileData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.ObservedFolder;
import model.GraphicsFactory;
import model.TreeFileNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainViewController {

    @FXML
    private TreeTableView<FileData> locationTreeView;

    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TextField directorySize; //TODO: bind to field, after change validate conditions, display warning

    private final List<ObservedFolder> folderList = new LinkedList<>();

    @FXML
    public void initialize() {
        createRoot();
        TreeTableColumn<FileData, File> pathColumn = new TreeTableColumn<>("Name");
        TreeTableColumn<FileData, Long> sizeColumn = new TreeTableColumn<>("Size");
        pathColumn.setPrefWidth(200); //todo: set proper width
        pathColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("file"));//node -> {
        //    //node.getValue().setGraphic(GraphicsFactory.getGraphic(node.getValue().getValue().isDirectory()));
        //    return new SimpleStringProperty(node.getValue().getValue().getFile().getName());
        //});

        pathColumn.setCellFactory(ttc -> new TreeTableCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
                setGraphic(empty ? null : GraphicsFactory.getGraphic(item.isDirectory()));
            }
        });

        //todo: setCellFactory for sizeColumn (status bar?)
        sizeColumn.setCellValueFactory(node -> {
            TreeFileNode n = (TreeFileNode) node.getValue();
            return new ReadOnlyObjectWrapper<>(n.getSize());
        });

        locationTreeView.getColumns().add(pathColumn);
        locationTreeView.getColumns().add(sizeColumn);
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
            var folder = new ObservedFolder(pathToWatch);
            addToMainTree(folder.getTree());
            folderList.add(folder);
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

    public void onExit(){
        folderList.forEach(ObservedFolder::destroy);
    }
}
