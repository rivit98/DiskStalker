package controllers;


import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.FileData;
import model.GraphicsFactory;
import model.ObservedFolder;
import model.TreeFileNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
        //todo: refactor this
        TreeTableColumn<FileData, Path> pathColumn = new TreeTableColumn<>("Name");
        TreeTableColumn<FileData, Number> sizeColumn = new TreeTableColumn<>("Size");
        pathColumn.setPrefWidth(200); //todo: set proper width
        pathColumn.setCellValueFactory(node -> new SimpleObjectProperty<>(node.getValue().getValue().getPath()));

        pathColumn.setCellFactory(ttc -> new TreeTableCell<>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getFileName().toString());
                setGraphic(empty ? null : GraphicsFactory.getGraphic(item.toFile().isDirectory()));
            }
        });

        //todo: setCellFactory for sizeColumn (status bar?)
        sizeColumn.setCellValueFactory(node -> node.getValue().getValue().sizePropertyProperty());

        sizeColumn.setCellFactory(ttc -> {
            TreeTableCell<FileData, Number> cell = new TreeTableCell<>() {
                @Override
                protected void updateItem(Number value, boolean empty) {
                    super.updateItem(value, empty);
                    setText(empty ? null : value.toString());
                }
            };
            cell.pseudoClassStateChanged(PseudoClass.getPseudoClass("centered"), true);

            return cell;
        });

        locationTreeView.getColumns().add(pathColumn);
        locationTreeView.getColumns().add(sizeColumn);
        initializeButtons();
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
            folder.getTree().subscribe(treeFileNode -> {
                addToMainTree(treeFileNode);
                folderList.add(folder);
            });
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void initializeButtons() {
        addButton.setOnAction(this::addButtonClicked);
        deleteButton.setOnAction(this::deleteButtonClicked);
    }

    public void addButtonClicked(ActionEvent actionEvent) {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        directoryChooser.setTitle("Choose directory to watch");
        var selectedFolderOptional = Optional.ofNullable(directoryChooser.showDialog(new Stage()));
        selectedFolderOptional.ifPresent(selectedFolder -> {
            loadTreeItems(selectedFolder.toPath());
        });
    }

    public void deleteButtonClicked(ActionEvent actionEvent) {
        var selectedTreeItem = Optional.ofNullable(locationTreeView.getSelectionModel().getSelectedItem());
        selectedTreeItem.ifPresent(item -> {
            var searchedPath = item.getValue().getPath();
            var rootFolder =
                    folderList.stream()
                            .filter(observedFolder -> observedFolder.containsNode(searchedPath))
                            .findFirst();

            rootFolder.ifPresent(observedFolder -> { //should always be present
                removeFolder(observedFolder, item);
            });
        });
    }

    public void removeFolder(ObservedFolder folder, TreeItem<FileData> nodeToRemove){
        var c = (TreeFileNode) nodeToRemove;
        if(locationTreeView.getRoot().getChildren().contains(c)){ //we are removing main folder
            folder.destroy();
            locationTreeView.getRoot().getChildren().remove(c);
            folderList.remove(folder);
        }else{
            c.deleteMe();
        }
    }

    public void onExit() {
        folderList.forEach(ObservedFolder::destroy);
    }
}
