package controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    //private Alerts alerts = new Alerts();

    private void initializeTree() {
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
                if(!empty && item.getFileName() == null) {
                    setText(item.toString());
                }
                else {
                    setText(empty ? null : item.getFileName().toString());
                }
                setGraphic(empty ? null : GraphicsFactory.getGraphic(item.toFile().isDirectory()));
            }
        });

        //todo: setCellFactory for sizeColumn (status bar?)
        sizeColumn.setCellValueFactory(node -> node.getValue().getValue().sizePropertyProperty());

        sizeColumn.setCellFactory(ttc -> {
            TreeTableCell<FileData, Number> cell = new TreeTableCell<>() {
                @Override
                protected void updateItem(Number value, boolean empty) {
                    var treeItem = getTreeTableRow().getTreeItem();
                    super.updateItem(value, empty);
                    setText(empty ? null : value.toString());
                    if(treeItem != null) {
                        var maximumSize = treeItem.getValue().getMaximumSize();
                        if (treeItem.getParent() != null && treeItem.getParent().getValue() == null
                                && value.longValue() > maximumSize) {
                            Alerts.sizeExceededAlert(treeItem.getValue().getPath().toString(), maximumSize / (1024 * 1024)); //todo: remove magic numbers
                        }
                    }
                }
            };
            cell.pseudoClassStateChanged(PseudoClass.getPseudoClass("centered"), true);

            return cell;
        });

        locationTreeView.getColumns().add(pathColumn);
        locationTreeView.getColumns().add(sizeColumn);
    }

    @FXML
    public void initialize() {
        initializeTree();
        initializeButtons();
        initializeSizeField();
        //TODO: repair buttons bindings
        setSizeButton.disableProperty().bind(Bindings.createBooleanBinding(() -> { //todo: refactor this
            if(!locationTreeView.getSelectionModel().getSelectedItems().isEmpty()) {
                var selectedItem = locationTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getParent() != null && !directorySize.getText().equals("")) {
                    return selectedItem.getParent().getValue() != null;
                }
            }
            return true;
        }, locationTreeView.getSelectionModel().selectedItemProperty(), directorySize.textProperty()));//isEmpty(locationTreeView.getSelectionModel().getSelectedItems()));
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

        directorySize.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                directorySize.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        locationTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldTreeItem, newTreeItem) -> {
            if (oldTreeItem != null) {
                directorySize.textProperty().unbind();
            }
            if(newTreeItem != null && newTreeItem.getParent() != null && newTreeItem.getParent().getValue() == null) {
                directorySize.textProperty().bindBidirectional(newTreeItem.getValue().getMaximumSizePropertyAsStringProperty()); //todo: is this good?
            }
//            else { //todo: consider if we want this
//                directorySize.textProperty().unbind();
//            }
        });
    }

    private void addButtonClicked(ActionEvent actionEvent) {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        directoryChooser.setTitle("Choose directory to watch");

        var selectedFolderOptional = Optional.ofNullable(directoryChooser.showDialog(new Stage()));
        selectedFolderOptional.ifPresent(selectedFolder -> {
            var rootChildrens = locationTreeView.getRoot().getChildren().stream()
                    .filter(children -> children.getValue().getPath().equals(selectedFolder.toPath()))
                    .findAny();
            if(rootChildrens.isPresent()){
                Alerts.tryingToAddSameFolderToObservedAlert();
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
        var maximumSize = Long.parseLong(directorySize.getText())*1024*1024; //todo: remove "magic numbers"
        var selectedItem = locationTreeView.getSelectionModel().getSelectedItem().getValue();
        selectedItem.setMaximumSizeProperty(maximumSize);
        Alerts.setMaxSizeAlert(selectedItem.getPath().toString(), maximumSize/(1024*1024));
        for(var c : locationTreeView.getRoot().getChildren()) {
            var fileData = c.getValue();
            if(fileData.getSize() > maximumSize) {
                Alerts.sizeExceededAlert(fileData.getPath().toString(), maximumSize/(1024*1024));
            }
        }
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