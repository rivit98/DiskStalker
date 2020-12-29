package org.agh.diskstalker.controllers;

import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.application.StringToIntFormatter;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.events.FolderEventType;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.agh.diskstalker.persistence.DatabaseCommand;
import org.agh.diskstalker.persistence.DatabaseCommandExecutor;
import org.agh.diskstalker.persistence.ObservedFolderDao;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@FxmlView("/views/MainView.fxml")
public class MainView {

    private final List<ObservedFolder> folderList = new LinkedList<>();
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

    private void initializeTableTreeView() {
        createRoot();
        //todo: refactor this
        var pathColumn = new TreeTableColumn<NodeData, Path>("Name");
        var sizeColumn = new TreeTableColumn<NodeData, Number>("Size");
        pathColumn.setPrefWidth(200); //todo: set proper width
        pathColumn.setCellValueFactory(node -> {
            var pathOptional = Optional.ofNullable(node.getValue())
                    .flatMap(v -> Optional.ofNullable(v.getValue()))
                    .map(NodeData::getPath);

            return pathOptional.map(SimpleObjectProperty::new).orElseGet(SimpleObjectProperty::new);
        });

        pathColumn.setCellFactory(ttc -> new TreeTableCell<>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    if (!empty && item.getFileName() == null) {
                        setText(item.toString());
                    } else {
                        setText(empty ? null : Objects.requireNonNull(item).getFileName().toString());
                    }
                    setGraphic(empty ? null : GraphicsFactory.getGraphic(item.toFile().isDirectory()));
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        });

        sizeColumn.setCellValueFactory(node -> {
            var sizePropertyOptional = Optional.ofNullable(node.getValue());
            return sizePropertyOptional
                    .map(nodeData -> nodeData.getValue().sizePropertyProperty())
                    .orElse(null);
        });

        sizeColumn.setCellFactory(ttc -> {
            TreeTableCell<NodeData, Number> cell = new TreeTableCell<>() {
                @Override
                protected void updateItem(Number value, boolean empty) {
                    super.updateItem(value, empty);
                    if (value != null)
                        setText(empty ? null : value.toString());
                    else
                        setText(null);
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
        initializeTableTreeView();
        initializeButtons();
        initializeSizeField();
        loadSavedSettings();
    }

    private void loadSavedSettings() {
        ObservedFolderDao.getAll()
                .forEach(observedFolder -> {
                    addObservedFolder(observedFolder);
                    try {
                        //TODO: loading many folders from db causes exceptions in rx (temporary sol)
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
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
        folder.getTree()
                .observeOn(JavaFxScheduler.platform())
                .subscribe(treeFileNode -> {
                    addToMainTree(treeFileNode);
                    folderList.add(folder);
                });

        folder.getEventStream()
                .buffer(2, TimeUnit.SECONDS)
                .observeOn(JavaFxScheduler.platform())
                .subscribe(eventList -> {
                    var uniqueEvents = new HashSet<FolderEventType>(eventList.size());
                    eventList.removeIf(e -> !uniqueEvents.add(e.getEventType())); // filter duplicated events
                    eventList.forEach(event -> {
                        switch (event.getEventType()) {
                            case ERROR -> { //TODO: error alert
                                Alerts.genericErrorAlert(folder.getPath(), event.getMessage());
                            }
                            case SIZE_CHANGED -> {
                                if (folder.isSizeLimitExceeded()) {
                                    Alerts.sizeExceededAlert(folder.getPath().toString(), folder.getMaximumSize());
                                }
                            }
                        }
                    });
                });
    }

    private void initializeButtons() {
        addButton.setOnAction(this::addButtonClicked);
        deleteButton.setOnAction(this::deleteButtonClicked);
        setSizeButton.setOnAction(this::setSizeButtonClicked);

        var selectionModel = locationTreeView.getSelectionModel();
        var selectedItems = selectionModel.getSelectedItems();

        setSizeButton.disableProperty().bind(Bindings.createBooleanBinding(() -> { //todo: refactor this
            if (!selectedItems.isEmpty()) {
                var selectedItem = selectionModel.getSelectedItem();
                if (selectedItem != null && selectedItem.getParent() != null && !maxSizeField.getText().equals("")) {
                    return selectedItem.getParent().getValue() != null;
                }
            }
            return true;
        }, selectionModel.selectedItemProperty(), maxSizeField.textProperty()));//isEmpty(locationTreeView.getSelectionModel().getSelectedItems()));

        deleteButton.disableProperty().bind(Bindings.isEmpty(selectedItems));
    }

    private void initializeSizeField() {
        maxSizeField.setTextFormatter(new StringToIntFormatter().getFormatter());

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

    private void addButtonClicked(ActionEvent actionEvent) {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        directoryChooser.setTitle("Choose directory to watch");

        var selectedFolderOptional = Optional.ofNullable(directoryChooser.showDialog(new Stage()));
        selectedFolderOptional.ifPresent(selectedFolder -> {
            var rootChildren = locationTreeView.getRoot().getChildren().stream()
                    .filter(children -> children.getValue().getPath().equals(selectedFolder.toPath()))
                    .findFirst();

            if (rootChildren.isPresent()) {
                Alerts.tryingToAddSameFolderToObservedAlert();
            } else {
                var folder = new ObservedFolder(selectedFolder.toPath());
                addObservedFolder(folder);
                new DatabaseCommandExecutor(folder, DatabaseCommand.SAVE).run();
            }
        });
    }

    private void deleteButtonClicked(ActionEvent actionEvent) {
        Optional.ofNullable(locationTreeView.getSelectionModel().getSelectedItem()).ifPresent(item -> {
            Platform.runLater(() -> maxSizeField.setText(""));
            var searchedPath = item.getValue().getPath();

            getObservedFolderFromTreePath(searchedPath).ifPresent(observedFolder -> { //should always be present
                removeFolder(observedFolder, item);
            });
        });
    }

    private void setSizeButtonClicked(ActionEvent actionEvent) {
        var maximumSize = Long.parseLong(maxSizeField.getText()) * FileUtils.ONE_MB;
        var selectedTreeItem = locationTreeView.getSelectionModel().getSelectedItem();
        var value = selectedTreeItem.getValue();

        getObservedFolderFromTreePath(value.getPath()).ifPresent(observedFolder -> {
            observedFolder.setMaximumSizeProperty(maximumSize);
            new DatabaseCommandExecutor(observedFolder, DatabaseCommand.UPDATE).run();
            Alerts.setMaxSizeAlert(value.getPath().toString(), maximumSize);
        });
    }

    private void removeFolder(ObservedFolder folder, TreeItem<NodeData> treeItem) {
        var nodeToRemove = (TreeFileNode) treeItem;
        if (locationTreeView.getRoot().getChildren().contains(nodeToRemove)) { //we are removing main folder
            new DatabaseCommandExecutor(folder, DatabaseCommand.DELETE).run();
            folder.destroy();
            locationTreeView.getRoot().getChildren().remove(nodeToRemove);
            folderList.remove(folder);
        } else {
            nodeToRemove.deleteMe();
        }
    }

    private Optional<ObservedFolder> getObservedFolderFromTreePath(Path searchedPath) {
        return folderList.stream()
                .filter(observedFolder -> observedFolder.containsNode(searchedPath))
                .findFirst();
    }

    private Optional<ObservedFolder> getObservedFolderFromSelection() {
        return Optional.ofNullable(locationTreeView.getSelectionModel().getSelectedItem())
                .flatMap(item -> getObservedFolderFromTreePath(item.getValue().getPath()));
    }

    private Optional<ObservedFolder> getObservedFolderFromTreeItem(TreeItem<NodeData> treeItem) {
        return Optional.ofNullable(treeItem)
                .flatMap(item -> getObservedFolderFromTreePath(item.getValue().getPath()));
    }

    public void onExit() {
        //TODO: debug, why app closes so long, probably DB connection needs to be closed
        folderList.forEach(ObservedFolder::destroy);
    }
}
