package org.agh.diskstalker.controllers;

import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.application.StringToIntFormatter;
import org.agh.diskstalker.cellFactories.PathColumnCellFactory;
import org.agh.diskstalker.cellFactories.SizeColumnCellFactory;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.agh.diskstalker.persistence.DatabaseCommandExecutor;
import org.agh.diskstalker.persistence.DatabaseCommandType;
import org.agh.diskstalker.persistence.ObservedFolderDao;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Component
@FxmlView("/views/MainView.fxml")
public class MainView {
    @FXML
    private TreeTableView<NodeData> locationTreeView;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button setSizeButton;
    @FXML
    private TextField maxSizeField;
    @FXML
    private Button deleteFromDiskButton;

    private final List<ObservedFolder> folderList = new LinkedList<>();
    private final DatabaseCommandExecutor commandExecutor = new DatabaseCommandExecutor();

    @FXML
    public void initialize() {
        initializeTableTreeView();
        initializeButtons();
        initializeSizeField();
        loadSavedFolders();
    }

    private void initializeTableTreeView() {
        createRoot();
        prepareColumns();
    }

    private void createRoot() {
        locationTreeView.setRoot(new TreeItem<>());
        locationTreeView.setShowRoot(false);
        locationTreeView.getRoot().setExpanded(true);
    }

    private void prepareColumns() {
        //todo: refactor this
        var pathColumn = new TreeTableColumn<NodeData, Path>("Name");
        var sizeColumn = new TreeTableColumn<NodeData, Number>("Size");
        pathColumn.setPrefWidth(242); //todo: set proper width
        sizeColumn.setPrefWidth(123);

        pathColumn.setCellFactory(ttc -> new PathColumnCellFactory(this));
        sizeColumn.setCellFactory(ttc -> new SizeColumnCellFactory());

        pathColumn.setCellValueFactory(node -> {
            var pathOptional = Optional.ofNullable(node.getValue())
                    .flatMap(v -> Optional.ofNullable(v.getValue()))
                    .map(NodeData::getPath);

            return pathOptional.map(SimpleObjectProperty::new).orElseGet(SimpleObjectProperty::new);
        });

        sizeColumn.setCellValueFactory(node -> {
            var sizePropertyOptional = Optional.ofNullable(node.getValue());
            return sizePropertyOptional
                    .map(nodeData -> nodeData.getValue().sizePropertyProperty())
                    .orElse(null);
        });

        locationTreeView.getColumns().addAll(List.of(pathColumn, sizeColumn));
    }

    private void initializeButtons() {
        addButton.setOnAction(this::addButtonClicked);
        deleteButton.setOnAction(this::deleteButtonClicked);
        setSizeButton.setOnAction(this::setSizeButtonClicked);
        deleteFromDiskButton.setOnAction(this::deleteFromDiskButtonClicked);

        var selectionModel = locationTreeView.getSelectionModel();
        var selectedItems = selectionModel.getSelectedItems();

        deleteFromDiskButton.disableProperty().bind(Bindings.isEmpty(selectedItems));

        setSizeButton.disableProperty().bind(Bindings.createBooleanBinding(() -> { //todo: refactor this
            if (!selectedItems.isEmpty()) {
                var selectedItem = selectionModel.getSelectedItem();
                if (checkIfRoot(selectedItem) && !maxSizeField.getText().equals("")) {
                    return selectedItem.getParent().getValue() != null;
                }
            }
            return true;
        }, selectionModel.selectedItemProperty(), maxSizeField.textProperty()));//isEmpty(locationTreeView.getSelectionModel().getSelectedItems()));

        deleteButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            if (!selectedItems.isEmpty()) {
                var selectedItem = selectionModel.getSelectedItem();
                if (checkIfRoot(selectedItem)) {
                    return selectedItem.getParent().getValue() != null;
                }
            }
            return true;
        }, selectionModel.selectedItemProperty()));//Bindings.isEmpty(selectedItems));
    }

    private void initializeSizeField() {
        maxSizeField.setTextFormatter(new StringToIntFormatter());

        locationTreeView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldTreeItem, newTreeItem) -> {
                    var oldFolder = getObservedFolderFromTreeItem(oldTreeItem);
                    var newFolder = getObservedFolderFromTreeItem(newTreeItem);

                    oldFolder.ifPresentOrElse(oldObservedFolder -> {
                        newFolder.ifPresent(newObservedFolder -> {
                            if (!oldObservedFolder.equals(newObservedFolder)) {
                                maxSizeField.setText(String.valueOf(newObservedFolder.getMaximumSize() / FileUtils.ONE_MB));
                                // this won't work because:
                                // 1) unbind removes all listeners
                                // 2) bind prevents inputting value
//                            directorySize.textProperty().unbind();
//                            directorySize.textProperty().bind(newObservedFolder.getMaximumSizeProperty().asString());
                            }
                        });
                    }, () -> newFolder.ifPresent(newObservedFolder -> {
                        maxSizeField.setText(String.valueOf(newObservedFolder.getMaximumSize() / FileUtils.ONE_MB));
                    }));
                });
    }

    private void loadSavedFolders() { //FIXME: restoring folders take long time
        ObservedFolderDao.getAll().forEach(this::observeFolderEvents);
    }

    public void addToMainTree(ObservedFolder folder, TreeFileNode node) {
        locationTreeView.getRoot().getChildren().add(node);
        folderList.add(folder);
    }

    private void observeFolderEvents(ObservedFolder folder) {
        folder.getEventStream()
                .observeOn(JavaFxScheduler.platform())
                .subscribe(event -> event.dispatch(this));
    }

    private void addButtonClicked(ActionEvent actionEvent) {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        directoryChooser.setTitle("Choose directory to watch");

        var selectedFolderOptional = Optional.ofNullable(directoryChooser.showDialog(new Stage()));
        selectedFolderOptional.ifPresent(selectedFolder -> {
            var samePathExists = locationTreeView.getRoot().getChildren().stream()
                    .anyMatch(children -> children.getValue().getPath().equals(selectedFolder.toPath()));

            if (samePathExists) {
                Alerts.tryingToAddSameFolderToObservedAlert();
            } else {
                var folder = new ObservedFolder(selectedFolder.toPath());
                observeFolderEvents(folder);

                commandExecutor.executeCommand(folder, DatabaseCommandType.SAVE);
            }
        });
    }

    private void deleteButtonClicked(ActionEvent actionEvent) {
        Optional.ofNullable(
                locationTreeView.getSelectionModel().getSelectedItem()
        ).ifPresent(item -> {
            var searchedPath = item.getValue().getPath();
            getObservedFolderFromTreePath(searchedPath).ifPresent(observedFolder -> { //should always be present
                removeFolder(observedFolder, item);
                maxSizeField.clear();
            });
        });
    }

    private void deleteFromDiskButtonClicked(ActionEvent actionEvent) {
        Optional.ofNullable(
                locationTreeView.getSelectionModel().getSelectedItem()
        ).ifPresent(item -> {
            var nodeData = item.getValue();
            var searchedPath = nodeData.getPath();
            var res = Alerts.yesNoDeleteAlert(searchedPath);
            if (res.equals(ButtonType.YES)) {
                try {
                    var searchedFile = searchedPath.toFile();
                    if (nodeData.isDirectory()) {
                        FileUtils.deleteDirectory(searchedFile);
                        if (isMainFolder(item)) {
                            getObservedFolderFromTreePath(searchedPath).ifPresent(observedFolder -> {
                                removeMainFolder(observedFolder, item);
                            });
                        }
                    } else {
                        if (!searchedFile.delete()) {
                            throw new IOException();
                        }
                    }
                } catch (IOException | IllegalArgumentException e) {
                    Alerts.genericErrorAlert(searchedPath, "Cannot delete file");
                    e.printStackTrace();
                }
            }
        });
    }

    private void setSizeButtonClicked(ActionEvent actionEvent) {
        var maximumSize = Long.parseLong(maxSizeField.getText()) * FileUtils.ONE_MB;
        var selectedTreeItem = locationTreeView.getSelectionModel().getSelectedItem();
        var value = selectedTreeItem.getValue();

        getObservedFolderFromTreePath(value.getPath()).ifPresent(observedFolder -> {
            observedFolder.setMaximumSizeProperty(maximumSize);
            commandExecutor.executeCommand(observedFolder, DatabaseCommandType.UPDATE);
            Alerts.setMaxSizeAlert(value.getPath().toString(), maximumSize);
        });
    }

    private void removeFolder(ObservedFolder folder, TreeItem<NodeData> treeItem) {
        if (isMainFolder(treeItem)) {
            removeMainFolder(folder, treeItem);
        } else {
            ((TreeFileNode) treeItem).deleteMe();
        }
    }

    private boolean isMainFolder(TreeItem<NodeData> node){
        return locationTreeView.getRoot().getChildren().contains(node);
    }

    private void removeMainFolder(ObservedFolder folder, TreeItem<NodeData> nodeToRemove){
        folder.destroy();
        locationTreeView.getRoot().getChildren().remove(nodeToRemove);
        folderList.remove(folder);
        commandExecutor.executeCommand(folder, DatabaseCommandType.DELETE);
    }

    public Optional<ObservedFolder> getObservedFolderFromTreePath(Path searchedPath) {
        return folderList.stream()
                .filter(observedFolder -> observedFolder.containsNode(searchedPath))
                .findFirst();
    }

    public Optional<ObservedFolder> getObservedFolderFromSelection() {
        return Optional.ofNullable(locationTreeView.getSelectionModel().getSelectedItem())
                .flatMap(item -> getObservedFolderFromTreePath(item.getValue().getPath()));
    }

    public Optional<ObservedFolder> getObservedFolderFromTreeItem(TreeItem<NodeData> treeItem) {
        return Optional.ofNullable(treeItem)
                .flatMap(item -> getObservedFolderFromTreePath(item.getValue().getPath()));
    }

    public TreeTableView<NodeData> getMainView() {
        return locationTreeView;
    }

    private boolean checkIfRoot(TreeItem<NodeData> item) {
        return item != null && item.getParent() != null;
    }

    public void onExit() {
        //TODO: debug, why app closes so long, probably DB connection needs to be closed
        folderList.forEach(ObservedFolder::destroy);
    }
}
