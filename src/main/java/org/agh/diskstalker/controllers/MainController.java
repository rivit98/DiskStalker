package org.agh.diskstalker.controllers;

import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.controllers.alerts.AlertsFactory;
import org.agh.diskstalker.controllers.bindings.*;
import org.agh.diskstalker.controllers.buttonHandlers.*;
import org.agh.diskstalker.controllers.cellFactories.PathColumnCellFactory;
import org.agh.diskstalker.controllers.cellFactories.SizeTreeTableColumnCellFactory;
import org.agh.diskstalker.controllers.listeners.BiggestFileListener;
import org.agh.diskstalker.controllers.listeners.MaxFilesAmountListener;
import org.agh.diskstalker.controllers.listeners.MaxSizeButtonListener;
import org.agh.diskstalker.controllers.sortPolicies.MainControllerSortPolicy;
import org.agh.diskstalker.formatters.StringToIntFormatter;
import org.agh.diskstalker.graphics.GraphicsFactory;
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
import java.util.HashMap;
import java.util.Optional;

@Getter
@Slf4j
@Component
@FxmlView("/views/MainView.fxml")
public class MainController {

    @FXML
    private TabPane tabPane;
    @FXML
    private TreeTableView<NodeData> treeTableView;
    @FXML
    private TreeTableColumn<NodeData, Path> pathColumn;
    @FXML
    private TreeTableColumn<NodeData, Number> sizeColumn;
    @FXML
    private AbstractTabController filesTypeViewController;
    @FXML
    private AbstractTabController fileInfoViewController;
    @FXML
    private Button addButton;
    @FXML
    private Button stopObserveButton;
    @FXML
    private Button setMaxSizeButton;
    @FXML
    private TextField maxSizeField;
    @FXML
    private Button deleteFromDiskButton;
    @FXML
    private TextField maxFilesAmountField;
    @FXML
    private Button setMaxFilesAmountButton;
    @FXML
    public TextField biggestFileField;
    @FXML
    public Button setBiggestFileSizeButton;

    private final HashMap<Path, TreeFileNode> loadingFolderList = new HashMap<>();
    private final DatabaseCommandExecutor commandExecutor;
    private final FolderList folderList;
    private final GraphicsFactory graphicsFactory;
    private final AlertsFactory alertsFactory;

    public MainController(DatabaseCommandExecutor commandExecutor,
                          FolderList folderList,
                          GraphicsFactory graphicsFactory,
                          AlertsFactory alertsFactory) {
        this.commandExecutor = commandExecutor;
        this.folderList = folderList;
        this.graphicsFactory = graphicsFactory;
        this.alertsFactory = alertsFactory;
    }

    @FXML
    public void initialize() {
        this.commandExecutor.executeCommand(new ConnectToDbCommand());
        createRoot();
        prepareColumns();
        initializeTabs();
        initializeButtons();
        initializeFields();
        loadSavedFolders();
        initializeStatisticsLoading();
    }

    private void initializeStatisticsLoading() {
        //TODO: refactor whole type recognizing system
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab.getId().equals("filesTypeView")) {
                folderList.forEach(folder -> new Thread(folder::createTypeStatistics).start());
            }
        });
    }

    private void createRoot() {
        treeTableView.setRoot(new TreeItem<>());
        treeTableView.getRoot().setExpanded(true);
    }

    private void prepareColumns() {
        pathColumn.setCellFactory(ttc -> new PathColumnCellFactory(this));
        sizeColumn.setCellFactory(ttc -> new SizeTreeTableColumnCellFactory());

        pathColumn.setCellValueFactory(
                node -> Optional.ofNullable(node.getValue())
                        .flatMap(v -> Optional.ofNullable(v.getValue()))
                        .map(NodeData::getPath)
                        .map(SimpleObjectProperty::new)
                        .orElseGet(SimpleObjectProperty::new)
        );

        sizeColumn.setCellValueFactory(
                node -> Optional.ofNullable(node.getValue())
                        .map(TreeItem::getValue)
                        .map(NodeData::getAccumulatedSizeProperty)
                        .orElse(null)
        );

        treeTableView.sortPolicyProperty().set(new MainControllerSortPolicy());
    }

    private void initializeTabs() {
        filesTypeViewController.setModel(folderList);
        fileInfoViewController.setModel(folderList);
    }

    private void initializeButtons() {
        addButton.setOnAction(new AddButtonHandler(this));
        stopObserveButton.setOnAction(new StopObserveButtonHandler(this));
        deleteFromDiskButton.setOnAction(new DeleteFromDiskButtonHandler(this)); //if folder is big then removing time is really long
        setMaxSizeButton.setOnAction(new SetSizeButtonHandler(this));
        setMaxFilesAmountButton.setOnAction(new SetMaxFilesAmountButtonHandler(this));
        setBiggestFileSizeButton.setOnAction(new SetBiggestFileButtonHandler(this));

        var selectionModel = treeTableView.getSelectionModel();
        deleteFromDiskButton.disableProperty().bind(new DeleteFromDiskButtonBinding(selectionModel));
        stopObserveButton.disableProperty().bind(new StopObserveButtonBinding(this, selectionModel));
        setMaxSizeButton.disableProperty().bind(new SetMaxSizeButtonBinding(this, selectionModel));
        setMaxFilesAmountButton.disableProperty().bind(new SetMaxFilesAmountButtonBinding(this, selectionModel));
        setBiggestFileSizeButton.disableProperty().bind(new SetBiggestFileButtonBinding(this, selectionModel));
    }

    private void initializeFields() {
        maxSizeField.setTextFormatter(new StringToIntFormatter());
        maxFilesAmountField.setTextFormatter(new StringToIntFormatter());
        biggestFileField.setTextFormatter(new StringToIntFormatter());

        var selectedItemProperty = treeTableView.getSelectionModel().selectedItemProperty();
        selectedItemProperty.addListener(new MaxSizeButtonListener(maxSizeField, folderList));
        selectedItemProperty.addListener(new MaxFilesAmountListener(maxFilesAmountField, folderList));
        selectedItemProperty.addListener(new BiggestFileListener(biggestFileField, folderList));
    }

    private void loadSavedFolders() {
        commandExecutor.executeCommand(new GetAllObservedFolderCommand())
                .thenAccept(folders -> Platform.runLater(() -> {
                    folders.getFolderList().forEach(this::observeFolderEvents);
                    folders.getFolderList().forEach(ObservedFolder::scanDirectory);
                }));
    }

    // replace fake folder with real one
    public void replaceLoadingFolderWithRealOne(ObservedFolder folder, TreeFileNode node) {
        var fakeNode = loadingFolderList.remove(folder.getPath());
        treeTableView.getRoot().getChildren().remove(fakeNode);
        treeTableView.getRoot().getChildren().add(node);
        treeTableView.sort();
        refreshViews();
    }

    public void addLoadingFolder(ObservedFolder folder) {
        var node = new TreeFileNode(new NodeData(folder.getPath()));
        loadingFolderList.put(folder.getPath(), node);
        folderList.add(folder);
        treeTableView.getRoot().getChildren().add(node);
        treeTableView.sort();
    }

    public void observeFolderEvents(ObservedFolder folder) {
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

    public boolean canSetLimitOnNode(TreeItem<NodeData> node) {
        return isMainFolder(node)
                && !folderList.getObservedFolderFromTreeItem(node).map(ObservedFolder::isScanning).orElse(false);
    }

    public boolean removeMainFolder(ObservedFolder folder, TreeItem<NodeData> nodeToRemove) {
        folder.destroy();

        loadingFolderList.remove(folder.getPath());
        treeTableView.getRoot().getChildren().remove(nodeToRemove);
        if (treeTableView.getRoot().getChildren().isEmpty()) {
            treeTableView.getSelectionModel().clearSelection();
        }
        commandExecutor.executeCommand(new DeleteObservedFolderCommand(folder));
        return folderList.remove(folder);
    }

    public void refreshViews() {
        treeTableView.refresh();
        fileInfoViewController.refresh();
        filesTypeViewController.refresh();
    }

    public Optional<TreeItem<NodeData>> getSelectedItem() {
        return Optional.ofNullable(treeTableView.getSelectionModel().getSelectedItem());
    }

    public void onExit() {
        log.info("MainController onExit");
        folderList.forEach(ObservedFolder::destroy);
        commandExecutor.stop();
    }

}
