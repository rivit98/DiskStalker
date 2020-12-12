package org.agh.diskstalker.controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.application.StringToIntFormatter;
import org.agh.diskstalker.graphics.Alerts;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.FileData;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.agh.diskstalker.persistence.DatabaseCommand;
import org.agh.diskstalker.persistence.DatabaseCommandExecutor;
import org.agh.diskstalker.persistence.ObservedFolderDao;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@FxmlView("/views/MainView.fxml")
public class MainView {

    private final List<ObservedFolder> folderList = new LinkedList<>();
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

    private final ChangeListener listener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            var newVal = (Number) newValue;
            Platform.runLater(() -> directorySize.setText(String.valueOf((newVal.longValue() / (1024 * 1024)))));
        }
    };

    private void initializeTree() {
        createRoot();
        //todo: refactor this
        var pathColumn = new TreeTableColumn<FileData, Path>("Name");
        var sizeColumn = new TreeTableColumn<FileData, Number>("Size");
        pathColumn.setPrefWidth(200); //todo: set proper width
        pathColumn.setCellValueFactory(node -> {
            var pathOptional = Optional.ofNullable(node.getValue())
                    .flatMap(v -> Optional.ofNullable(v.getValue()))
                    .map(FileData::getPath);

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

        //todo: setCellFactory for sizeColumn (status bar?)
        sizeColumn.setCellValueFactory(node -> {
            var sizePropertyOptional = Optional.ofNullable(node.getValue());
            return sizePropertyOptional.<ObservableValue<Number>>map(fileDataTreeItem -> fileDataTreeItem.getValue().sizePropertyProperty()).orElse(null);
        });

        sizeColumn.setCellFactory(ttc -> {
            TreeTableCell<FileData, Number> cell = new TreeTableCell<>() {
                @Override
                protected void updateItem(Number value, boolean empty) {
                    var treeItem = getTreeTableRow().getTreeItem();
                    super.updateItem(value, empty);
                    if (value != null)
                        setText(empty ? null : value.toString());
                    else
                        setText(null);
//                    if (treeItem != null) {
//                        var maximumSize = treeItem.getValue().getMaximumSize();
//                        if (value != null && treeItem.getParent() != null && treeItem.getParent().getValue() == null
//                                && value.longValue() > maximumSize) {
//                            Alerts.sizeExceededAlert(treeItem.getValue().getPath().toString(), maximumSize / (1024 * 1024)); //todo: remove magic numbers
//                        }
//                    }
                    //TODO: we need some kind of notifying about exceeding size,
                    // notifications should be emitted from observed folder via some kind of subject
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
            if (!locationTreeView.getSelectionModel().getSelectedItems().isEmpty()) {
                var selectedItem = locationTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getParent() != null && !directorySize.getText().equals("")) {
                    return selectedItem.getParent().getValue() != null;
                }
            }
            return true;
        }, locationTreeView.getSelectionModel().selectedItemProperty(), directorySize.textProperty()));//isEmpty(locationTreeView.getSelectionModel().getSelectedItems()));
        deleteButton.disableProperty().bind(Bindings.isEmpty(locationTreeView.getSelectionModel().getSelectedItems()));

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
        directorySize.setTextFormatter(new StringToIntFormatter().getFormatter());

        locationTreeView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldTreeItem, newTreeItem) -> {
                    var oldFolder = getObservedFolderFromTreeItem(oldTreeItem);
                    var newFolder = getObservedFolderFromTreeItem(newTreeItem);

                    oldFolder.ifPresentOrElse(oldObservedFolder -> {
                        newFolder.ifPresent(newObservedFolder -> {
                            if (!oldObservedFolder.equals(newObservedFolder)) {
                                directorySize.setText(String.valueOf(newObservedFolder.getMaximumSize() / FileUtils.ONE_MB));
                                // this won't work because:
                                // 1) unbind removes all listeners
                                // 2) bind prevents inputting value
//                            directorySize.textProperty().unbind();
//                            directorySize.textProperty().bind(newObservedFolder.getMaximumSizeProperty().asString());
                            }

                        });
                    }, () -> newFolder.ifPresent(newObservedFolder -> {
                        directorySize.setText(String.valueOf(newObservedFolder.getMaximumSize() / FileUtils.ONE_MB));
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
            Platform.runLater(() -> directorySize.setText(""));
            var searchedPath = item.getValue().getPath();

            getObservedFolderFromTreePath(searchedPath).ifPresent(observedFolder -> { //should always be present
                removeFolder(observedFolder, item);
            });
        });
    }

    private void setSizeButtonClicked(ActionEvent actionEvent) {
        var maximumSize = Long.parseLong(directorySize.getText()) * FileUtils.ONE_MB;
        var selectedTreeItem = locationTreeView.getSelectionModel().getSelectedItem();
        var value = selectedTreeItem.getValue();

        getObservedFolderFromTreePath(value.getPath()).ifPresent(observedFolder -> {
            observedFolder.setMaximumSizeProperty(maximumSize);
            new DatabaseCommandExecutor(observedFolder, DatabaseCommand.UPDATE).run();
            Alerts.setMaxSizeAlert(value.getPath().toString(), maximumSize / FileUtils.ONE_MB);
            if (observedFolder.isSizeLimitExceeded()) {
                Alerts.sizeExceededAlert(observedFolder.toString(), maximumSize / FileUtils.ONE_MB);
            }
        });
    }

    private void removeFolder(ObservedFolder folder, TreeItem<FileData> treeItem) {
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

    private Optional<ObservedFolder> getObservedFolderFromTreeItem(TreeItem<FileData> treeItem) {
        return Optional.ofNullable(treeItem)
                .flatMap(item -> getObservedFolderFromTreePath(item.getValue().getPath()));
    }

    public void onExit() {
        //TODO: debug, why app closes so long
        folderList.forEach(ObservedFolder::destroy);
    }
}
