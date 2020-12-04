package controllers;


import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.FileData;
import model.GraphicsFactory;
import model.ObservedFolder;
import model.TreeFileNode;

import java.io.File;
import java.io.IOException;
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
    private TextField directorySize; //TODO: bind to field, after change validate conditions, display warning

    private final List<ObservedFolder> folderList = new LinkedList<>();

    @FXML
    public void initialize() {
        createRoot();
        //todo: refactor this
        TreeTableColumn<FileData, File> pathColumn = new TreeTableColumn<>("Name");
        TreeTableColumn<FileData, Number> sizeColumn = new TreeTableColumn<>("Size");
        pathColumn.setPrefWidth(200); //todo: set proper width
        pathColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("file"));//node -> {
        //    //node.getValue().setGraphic(GraphicsFactory.getGraphic(node.getValue().getValue().isDirectory()));
        //    return new SimpleStringProperty(node.getValue().getValue().getFile().getName());
        //});

        pathColumn.setCellFactory(ttc -> new TreeTableCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
                setGraphic(empty ? null : GraphicsFactory.getGraphic(item.isDirectory()));
            }
        });

        //todo: setCellFactory for sizeColumn (status bar?)
        sizeColumn.setCellValueFactory(node ->
                node.getValue().getValue().sizePropertyProperty());

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
    }

    public void createRoot() {
        locationTreeView.setRoot(new TreeItem<>());
        locationTreeView.setShowRoot(false);
        locationTreeView.getRoot().setExpanded(true);
    }

    public void addToMainTree(TreeFileNode node) {
        locationTreeView.getRoot().getChildren().add(node);
    }

    public void loadTreeItems(Path pathToWatch) {
        try {
            var folder = new ObservedFolder(pathToWatch);
            folder.getTree().subscribe(treeFileNode -> {
                addToMainTree(treeFileNode);
                folderList.add(folder);
            });
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void initializeButtons() {
        addButton.setOnAction(this::addButtonClicked);
        deleteButton.setOnAction(this::deleteButtonClicked);
//        loadTreeItems(new File("./testDirs").toPath());
    }

    public void addButtonClicked(ActionEvent actionEvent) {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        directoryChooser.setTitle("Choose directory to watch");
        var selectedFolderOptional = Optional.ofNullable(directoryChooser.showDialog(new Stage()));
        selectedFolderOptional.ifPresent(selectedFolder -> {
            loadTreeItems(selectedFolder.toPath());
        });
    }

    public void deleteButtonClicked(ActionEvent actionEvent) {
        var selectedTreeItem = Optional.ofNullable(locationTreeView.getSelectionModel().getSelectedItem());
        //TODO: fix nullptr exception
        selectedTreeItem.ifPresent(item -> {
            var folder =
                    folderList.stream()
                            .filter(observedFolder -> observedFolder.checkIfNodeIsChild(item.getValue().getPath()))
                            .findAny();

            //FIXME: 
//            folder.ifPresent(folderWithNode -> folderWithNode.deleteNodes(item));
            item.getParent().getChildren().remove(item);
        });
    }

    public void onExit() {
        folderList.forEach(ObservedFolder::destroy);
    }
}
