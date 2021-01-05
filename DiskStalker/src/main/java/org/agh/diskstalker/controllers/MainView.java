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
import org.agh.diskstalker.cellFactories.PathColumnCellFactory;
import org.agh.diskstalker.cellFactories.SizeColumnCellFactory;
import org.agh.diskstalker.formatters.StringToIntFormatter;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.agh.diskstalker.persistence.DatabaseCommandExecutor;
import org.agh.diskstalker.persistence.command.*;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
@FxmlView("/views/MainView.fxml")
public class MainView {
    @FXML
    private TabPane tabPane;
    @FXML
    private TreeTableView<NodeData> locationTreeView;
    @FXML
    private Button addButton;
    @FXML
    private Button stopObserveButton;
    @FXML
    private Button setSizeButton;
    @FXML
    private TextField maxSizeField;
    @FXML
    private Button deleteFromDiskButton;
    @FXML
    private FileSizeView fileSizeViewController;
    @FXML
    private FileTypeView fileTypeViewController;
    @FXML
    private FileModificationDateView fileModificationDateViewController;

    private final DatabaseCommandExecutor commandExecutor = new DatabaseCommandExecutor();
    private FolderList folderList = new FolderList();

    @FXML
    public void initialize() {
        commandExecutor.executeCommand(new ConnectToDbCommand());
        initializeTableTreeView();
        initializeTabs();
        initializeButtons();
        initializeSizeField();
        loadSavedFolders();
        setStatisticsLoading();
    }

    private void setStatisticsLoading() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obsevable, oldTab, newTab) -> {
            if(newTab.getId().equals("fileTypeView")) {
                var thread = new Thread(() -> {
                    folderList.get().forEach(ObservedFolder::createTypeStatistics);
                });
                thread.start();
            } else if(newTab.getId().equals("fileSizeView")) {
                var thread = new Thread(() -> {
                    folderList.get().forEach(ObservedFolder::createSizeStatistics);
                });
                thread.start();
            } else if(newTab.getId().equals("fileModificationDateView")) {
                var thread = new Thread(() -> {
                    folderList.get().forEach(ObservedFolder::createDateModificationStatistics);
                });
                thread.start();
            }
        });
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

    private void initializeTabs() {
        fileSizeViewController.prepareTables(folderList);
        fileTypeViewController.prepareTables(folderList);
        fileModificationDateViewController.prepareTables(folderList);
    }

    private void prepareColumns() {
        //todo: refactor this
        var pathColumn = new TreeTableColumn<NodeData, Path>("Name");
        var sizeColumn = new TreeTableColumn<NodeData, Number>("Size");
        pathColumn.setPrefWidth(370); //todo: set proper width
        sizeColumn.setPrefWidth(192);

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
                    .map(nodeData -> nodeData.getValue().getSizeProperty())
                    .orElse(null);
        });

        locationTreeView.getColumns().addAll(List.of(pathColumn, sizeColumn));
    }

    private void initializeButtons() {
        addButton.setOnAction(this::addButtonClicked);
        stopObserveButton.setOnAction(this::stopObservingButtonClicked);
        setSizeButton.setOnAction(this::setSizeButtonClicked);
        deleteFromDiskButton.setOnAction(this::deleteFromDiskButtonClicked);

        var selectionModel = locationTreeView.getSelectionModel();
        var selectedItems = selectionModel.getSelectedItems();
        deleteFromDiskButton.disableProperty().bind(Bindings.isEmpty(selectedItems));

        setSizeButton.disableProperty().bind(Bindings.createBooleanBinding(() -> { //todo: refactor this
            if (!selectedItems.isEmpty()) {
                var selectedItem = selectionModel.getSelectedItem();
                if (isMainFolder(selectedItem) && !maxSizeField.getText().equals("")) {
                    return selectedItem.getParent().getValue() != null;
                }
            } 
            return true;
        }, selectionModel.selectedItemProperty(), maxSizeField.textProperty()));//isEmpty(locationTreeView.getSelectionModel().getSelectedItems()));

        stopObserveButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            if (!selectedItems.isEmpty()) { //FIXME: after deleting one element, other becomes selected in view but selectedItems is empty
                var selectedItem = selectionModel.getSelectedItem();
                if (isMainFolder(selectedItem)) {
                    return selectedItem.getParent().getValue() != null;
                }
            }
            return true;
        }, selectionModel.selectedItemProperty()));
    }

    private void initializeSizeField() {
        maxSizeField.setTextFormatter(new StringToIntFormatter());

        locationTreeView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldTreeItem, newTreeItem) -> {
                    var oldFolder = folderList.getObservedFolderFromTreeItem(oldTreeItem);
                    var newFolder = folderList.getObservedFolderFromTreeItem(newTreeItem);

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
        commandExecutor.executeCommand(new GetAllObservedFolderCommand())
                .thenAccept(folders -> {
                    folders.getFolderList().forEach(this::observeFolderEvents);
                });
    }

    public void addToMainTree(ObservedFolder folder, TreeFileNode node) {
        locationTreeView.getRoot().getChildren().add(node);
        folderList.get().add(folder);
    }

    private void observeFolderEvents(ObservedFolder folder) { //TODO: this might be problematic, we should subscribe folder before scanner starts
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

                commandExecutor.executeCommand(new SaveObservedFolderCommand(folder));
            }
        });
    }

    private void stopObservingButtonClicked(ActionEvent actionEvent) {
        Optional.ofNullable(
                locationTreeView.getSelectionModel().getSelectedItem()
        ).ifPresent(item -> {
            var searchedPath = item.getValue().getPath();
            folderList.getObservedFolderFromTreePath(searchedPath).ifPresent(observedFolder -> { //should always be present
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
                            folderList.getObservedFolderFromTreePath(searchedPath).ifPresent(observedFolder -> {
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

        folderList.getObservedFolderFromTreePath(value.getPath()).ifPresent(observedFolder -> {
            observedFolder.setMaximumSizeProperty(maximumSize);
            commandExecutor.executeCommand(new UpdateObservedFolderCommand(observedFolder));
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

    private boolean isMainFolder(TreeItem<NodeData> node) {
        return locationTreeView.getRoot().getChildren().contains(node);
    }

    private void removeMainFolder(ObservedFolder folder, TreeItem<NodeData> nodeToRemove) {
        folder.destroy();
        locationTreeView.getRoot().getChildren().remove(nodeToRemove);
        folderList.get().remove(folder);
        commandExecutor.executeCommand(new DeleteObservedFolderCommand(folder));
    }

    public TreeTableView<NodeData> getMainView() {
        return locationTreeView;
    }

    public FolderList getFolderList() {
        return folderList;
    }

    public void onExit() {
        //TODO: debug, why app closes so long, probably DB connection needs to be closed
        commandExecutor.stop();
        folderList.get().forEach(ObservedFolder::destroy);
    }
}
