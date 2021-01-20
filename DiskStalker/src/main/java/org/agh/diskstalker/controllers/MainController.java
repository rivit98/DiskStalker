package org.agh.diskstalker.controllers;

import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.controllers.bindings.*;
import org.agh.diskstalker.controllers.buttonHandlers.*;
import org.agh.diskstalker.controllers.cellFactories.PathColumnCellFactory;
import org.agh.diskstalker.controllers.cellFactories.SizeTreeTableColumnCellFactory;
import org.agh.diskstalker.controllers.listeners.BiggestFileListener;
import org.agh.diskstalker.controllers.listeners.MaxFilesAmountListener;
import org.agh.diskstalker.controllers.listeners.MaxSizeButtonListener;
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
public class MainController {

    @FXML
    private TabPane tabPane;
    @FXML @Getter
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
    @FXML @Getter
    private TextField maxSizeField;
    @FXML
    private Button deleteFromDiskButton;
    @FXML @Getter
    private TextField maxFilesAmountField;
    @FXML
    private Button setMaxFilesAmountButton;
    @FXML @Getter
    public TextField biggestFileField;
    @FXML
    public Button setBiggestFileSizeButton;

    private final DatabaseCommandExecutor commandExecutor = new DatabaseCommandExecutor();

    @Getter
    private final FolderList folderList = new FolderList();

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
                folderList.get().forEach(folder -> new Thread(folder::createTypeStatistics).start());
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
    }

    private void initializeTabs() {
        filesTypeViewController.setModel(folderList);
        fileInfoViewController.setModel(folderList);
    }

    private void initializeButtons() {
        addButton.setOnAction(new AddButtonHandler(this, commandExecutor));
        stopObserveButton.setOnAction(new StopObserveButtonHandler(this));
        setMaxSizeButton.setOnAction(new SetSizeButtonHandler(this, commandExecutor));
        deleteFromDiskButton.setOnAction(new DeleteFromDiskButtonHandler(this));
        setMaxFilesAmountButton.setOnAction(new SetMaxFilesAmountButtonHandler(this, commandExecutor));
        setBiggestFileSizeButton.setOnAction(new SetBiggestFileButtonHandler(this, commandExecutor));

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

    public void refreshViews(){
        treeTableView.refresh();
        fileInfoViewController.refresh();
        filesTypeViewController.refresh();
    }

    public void onExit() {
        folderList.get().forEach(ObservedFolder::destroy);
        commandExecutor.stop();
    }
}
