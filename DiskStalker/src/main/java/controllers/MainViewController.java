package controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.FileData;
import graphics.GraphicsFactory;
import model.ObservedFolder;
import model.tree.TreeFileNode;
import persistence.ObservedFoldersSQL;

import java.io.File;
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
    private Button setSizeButton;
    @FXML
    private TextField directorySize; //TODO: bind to field, after change validate conditions, display warning

    private final List<ObservedFolder> folderList = new LinkedList<>();

    @FXML
    public void initialize() {
        createRoot();
        locationTreeView.getSelectionModel().setSelectionMode(
                SelectionMode.SINGLE);
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
        initializeSizeField();
        //TODO: repair buttons bindings
        setSizeButton.disableProperty().bind(Bindings.isEmpty(locationTreeView.getSelectionModel().getSelectedItems()));
        deleteButton.disableProperty().bind(Bindings.isEmpty(locationTreeView.getSelectionModel().getSelectedItems()));

        loadSavedSettings(); //TODO: test this
    }

    private void loadSavedSettings(){
        ObservedFoldersSQL
                .loadFolders()
                .forEach(this::addObservedFolder);
    }

    private void createRoot() {
        locationTreeView.setRoot(new TreeItem<>());
        locationTreeView.setShowRoot(false);
        locationTreeView.getRoot().setExpanded(true);
    }

    private void addToMainTree(TreeFileNode node) {
        locationTreeView.getRoot().getChildren().add(node);
    }

    private void addObservedFolder(ObservedFolder folder) {
        folder.getTree().subscribe(treeFileNode -> {
            addToMainTree(treeFileNode);
            folderList.add(folder);
        });
    }

    private void initializeButtons() {
        addButton.setOnAction(this::addButtonClicked);
        deleteButton.setOnAction(this::deleteButtonClicked);
        setSizeButton.setOnAction(this::setSizeButtonClicked);
    }

    private void initializeSizeField() {
        locationTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldTreeItem, newTreeItem) -> {
            if (oldTreeItem != null) {
                directorySize.textProperty().unbind();
            }
            directorySize.textProperty().bind(newTreeItem.getValue().getMaximumSizePropertyAsStringProperty());
        });
    }

    private void addButtonClicked(ActionEvent actionEvent) {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        directoryChooser.setTitle("Choose directory to watch");

        var selectedFolderOptional = Optional.ofNullable(directoryChooser.showDialog(new Stage()));
        selectedFolderOptional.ifPresent(selectedFolder -> {
            var rootChildren = locationTreeView.getRoot().getChildren().stream()
                    .filter(children -> children.getValue().getPath().equals(selectedFolder.toPath()))
                    .findAny();
            if (rootChildren.isPresent()) {
                var alert = createAlert("Warning", "You already observe this directory!");
                alert.showAndWait()
                        .filter(response -> response == ButtonType.OK);
            } else {
                var folder = new ObservedFolder(selectedFolder.toPath());
                addObservedFolder(folder);
            }
        });
    }

    private void deleteButtonClicked(ActionEvent actionEvent) {
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

    private void setSizeButtonClicked(ActionEvent actionEvent) {
        //TODO: set max dir size
    }

    private Alert createAlert(String headerText, String information) {
        var alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(headerText);
        alert.setContentText(information);
        return alert;
    }

    private void removeFolder(ObservedFolder folder, TreeItem<FileData> nodeToRemove) {
        var c = (TreeFileNode) nodeToRemove;
        if (locationTreeView.getRoot().getChildren().contains(c)) { //we are removing main folder
            folder.destroy();
            locationTreeView.getRoot().getChildren().remove(c);
            folderList.remove(folder);
        } else {
            c.deleteMe();
        }
    }

    public void onExit() {
        folderList.forEach(ObservedFolder::destroy);
    }
}
