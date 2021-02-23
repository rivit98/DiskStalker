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
import org.agh.diskstalker.model.folders.FolderList;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;
import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.agh.diskstalker.model.tree.NodeData;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.agh.diskstalker.persistence.DatabaseCommandExecutor;
import org.agh.diskstalker.persistence.command.ConnectToDbCommand;
import org.agh.diskstalker.persistence.command.DeleteObservedFolderCommand;
import org.agh.diskstalker.persistence.command.GetAllObservedFolderCommand;
import org.agh.diskstalker.statistics.TypeRecognizer;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Component
@FxmlView("/views/MainView.fxml")
public class MainController {

    @FXML @Getter private TreeTableView<NodeData> treeTableView;
    @FXML private TreeTableColumn<NodeData, Path> pathColumn;
    @FXML private TreeTableColumn<NodeData, Number> sizeColumn;
    @FXML private AbstractTabController filesTypeViewController;
    @FXML private AbstractTabController fileInfoViewController;
    @FXML private Button addButton;
    @FXML private Button stopObserveButton;
    @FXML private Button setMaxSizeButton;
    @FXML @Getter private TextField maxSizeField;
    @FXML private Button deleteFromDiskButton;
    @FXML @Getter private TextField maxFilesAmountField;
    @FXML private Button setMaxFilesAmountButton;
    @FXML @Getter public TextField biggestFileField;
    @FXML public Button setBiggestFileSizeButton;

    @Getter private final DatabaseCommandExecutor commandExecutor;
    @Getter private final FolderList folderList;
    @Getter private final GraphicsFactory graphicsFactory;
    @Getter private final AlertsFactory alertsFactory;
    @Getter private final TypeRecognizer typeRecognizer;

    public MainController(DatabaseCommandExecutor commandExecutor,
                          FolderList folderList,
                          GraphicsFactory graphicsFactory,
                          AlertsFactory alertsFactory,
                          TypeRecognizer typeRecognizer) {
        this.commandExecutor = commandExecutor;
        this.folderList = folderList;
        this.graphicsFactory = graphicsFactory;
        this.alertsFactory = alertsFactory;
        this.typeRecognizer = typeRecognizer;
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
    }

    private void createRoot() {
        treeTableView.setRoot(new TreeItem<>());
        treeTableView.getRoot().setExpanded(true);
    }

    private void prepareColumns() {
        pathColumn.setCellFactory(ttc -> new PathColumnCellFactory(this));
        pathColumn.setCellValueFactory(node -> new SimpleObjectProperty<>(node.getValue().getValue().getPath()));

        sizeColumn.setCellFactory(ttc -> new SizeTreeTableColumnCellFactory());
        sizeColumn.setCellValueFactory(node -> node.getValue().getValue().getAccumulatedSizeProperty());

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
                    var folderList = folders.getFolderList();
                    folderList.forEach(this::observeFolderEvents);
                    folderList.forEach(IObservedFolder::scan);
                }));
    }

    public void observeFolderEvents(IObservedFolder folder) {
        folder.setTypeRecognizer(typeRecognizer);
        folder.getEventStream()
                .observeOn(JavaFxScheduler.platform())
                .subscribe(
                        event -> event.dispatch(this),
                        t -> log.warn(t.getMessage())
                );
    }

    public boolean removeFolder(ILimitableObservableFolder folder, TreeItem<NodeData> treeItem) {
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
                && !folderList.getObservedFolderFromTreeItem(node).map(IObservedFolder::isScanning).orElse(false);
    }

    public boolean removeMainFolder(ILimitableObservableFolder folder, TreeItem<NodeData> nodeToRemove) {
        folder.destroy();

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
        folderList.forEach(IObservedFolder::destroy);
        typeRecognizer.stop();
        commandExecutor.stop();
    }
}
