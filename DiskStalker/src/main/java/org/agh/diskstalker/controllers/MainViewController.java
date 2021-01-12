package org.agh.diskstalker.controllers;

import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.cellFactories.PathColumnCellFactory;
import org.agh.diskstalker.cellFactories.SizeColumnCellFactory;
import org.agh.diskstalker.controllers.buttonHandlers.AddButtonHandler;
import org.agh.diskstalker.controllers.buttonHandlers.DeleteFromDiskButtonHandler;
import org.agh.diskstalker.controllers.buttonHandlers.SetSizeButtonHandler;
import org.agh.diskstalker.controllers.buttonHandlers.StopObserveButtonHandler;
import org.agh.diskstalker.formatters.StringToIntFormatter;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.agh.diskstalker.persistence.DatabaseCommandExecutor;
import org.agh.diskstalker.persistence.command.ConnectToDbCommand;
import org.agh.diskstalker.persistence.command.DeleteObservedFolderCommand;
import org.agh.diskstalker.persistence.command.GetAllObservedFolderCommand;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;

@Component
@FxmlView("/views/MainView.fxml")
public class MainViewController {
    @FXML
    private TabPane tabPane;
    @FXML
    private TreeTableView<NodeData> treeTableView;
    @FXML
    private TreeTableColumn<NodeData, Path> pathColumn;
    @FXML
    private TreeTableColumn<NodeData, Number> sizeColumn;
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
    private AbstractTabController fileSizeViewController;
    @FXML
    private AbstractTabController fileTypeViewController;
    @FXML
    private AbstractTabController fileModificationDateViewController;

    private final DatabaseCommandExecutor commandExecutor = new DatabaseCommandExecutor();
    private final FolderList folderList = new FolderList();

    @FXML
    public void initialize() {
        commandExecutor.executeCommand(new ConnectToDbCommand());
        initializeTableTreeView();
        initializeTabs();
        initializeButtons();
        initializeSizeField();
        loadSavedFolders();
        initializeStatisticsLoading();
    }

    private void initializeStatisticsLoading() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab.getId().equals("fileTypeView")) {
                folderList.get().forEach(folder -> new Thread(folder::createTypeStatistics).start());
            } else if (newTab.getId().equals("fileModificationDateView")) {
                folderList.get().forEach(folder -> new Thread(folder::createDateModificationStatistics).start());
            }
        });
    }

    private void initializeTableTreeView() {
        createRoot();
        prepareColumns();
    }

    private void createRoot() {
        treeTableView.setRoot(new TreeItem<>());
        treeTableView.getRoot().setExpanded(true);
    }

    private void initializeTabs() {
        fileSizeViewController.setModel(folderList);
        fileTypeViewController.setModel(folderList);
        fileModificationDateViewController.setModel(folderList);
    }

    private void prepareColumns() {
        pathColumn.setCellFactory(ttc -> new PathColumnCellFactory(this));
        sizeColumn.setCellFactory(ttc -> new SizeColumnCellFactory());

        pathColumn.setCellValueFactory(node -> {
            var pathOptional = Optional.ofNullable(node.getValue())
                    .flatMap(v -> Optional.ofNullable(v.getValue()))
                    .map(NodeData::getPath);

            return pathOptional.map(SimpleObjectProperty::new).orElseGet(SimpleObjectProperty::new);
        });

        sizeColumn.setCellValueFactory(
                node -> Optional.ofNullable(node.getValue())
                        .map(nodeData -> nodeData.getValue().getSizeProperty())
                        .orElse(null)
        );
    }

    private void initializeButtons() {
        addButton.setOnAction(new AddButtonHandler(this, commandExecutor));
        stopObserveButton.setOnAction(new StopObserveButtonHandler(this));
        setSizeButton.setOnAction(new SetSizeButtonHandler(this, commandExecutor));
        deleteFromDiskButton.setOnAction(new DeleteFromDiskButtonHandler(this));

        initializeRulesForDisablingButtons();
    }

    private void initializeRulesForDisablingButtons() {
        var selectionModel = treeTableView.getSelectionModel();

        deleteFromDiskButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> selectionModel.getSelectedItem() == null,
                selectionModel.selectedItemProperty()
        ));

        setSizeButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> !isMainFolder(selectionModel.getSelectedItem()) || maxSizeField.getText().equals("")
                , selectionModel.selectedItemProperty(), maxSizeField.textProperty()
        ));

        stopObserveButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> !isMainFolder(selectionModel.getSelectedItem()),
                selectionModel.selectedItemProperty()
        ));
    }

    private void initializeSizeField() {
        maxSizeField.setTextFormatter(new StringToIntFormatter());

        treeTableView.getSelectionModel()
                .selectedItemProperty()
                .addListener(new MaxSizeButtonListener(maxSizeField, folderList));
    }

    private void loadSavedFolders() { //FIXME: restoring folders take long time
        commandExecutor.executeCommand(new GetAllObservedFolderCommand())
                .thenAccept(folders -> {
                    folders.getFolderList().forEach(this::observeFolderEvents);
                });
    }

    public void addToMainTree(ObservedFolder folder, TreeFileNode node) {
        treeTableView.getRoot().getChildren().add(node);
        folderList.get().add(folder);
    }

    public void observeFolderEvents(ObservedFolder folder) { //TODO: this might be problematic, we should subscribe folder before scanner starts
        folder.getEventStream()
                .observeOn(JavaFxScheduler.platform())
                .subscribe(event -> event.dispatch(this));
    }

    public boolean removeFolder(ObservedFolder folder, TreeItem<NodeData> treeItem) {
        if (isMainFolder(treeItem)) {
            return removeMainFolder(folder, treeItem);
        } else {
            return ((TreeFileNode) treeItem).deleteMe();
        }
    }

    public boolean removeTreeItem(TreeItem<NodeData> treeItem) {
        return folderList.getObservedFolderFromTreeItem(treeItem)
                .map(folder -> removeFolder(folder, treeItem))
                .orElse(false);
    }

    public boolean isMainFolder(TreeItem<NodeData> node) {
        return treeTableView.getRoot().getChildren().contains(node);
    }

    public boolean removeMainFolder(ObservedFolder folder, TreeItem<NodeData> nodeToRemove) {
        folder.destroy();
        treeTableView.getRoot().getChildren().remove(nodeToRemove);
        if (treeTableView.getRoot().getChildren().isEmpty()) {
            treeTableView.getSelectionModel().clearSelection();
        }
        commandExecutor.executeCommand(new DeleteObservedFolderCommand(folder));
        return folderList.get().remove(folder);
    }

    public TreeTableView<NodeData> getTreeTableView() {
        return treeTableView;
    }

    public FolderList getFolderList() {
        return folderList;
    }

    public TextField getMaxSizeField() {
        return maxSizeField;
    }

    public void onExit() {
        //TODO: debug, why app closes so long, probably DB connection needs to be closed
        commandExecutor.stop();
        folderList.get().forEach(ObservedFolder::destroy);
    }
}
