package org.agh.diskstalker.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import lombok.Getter;
import org.agh.diskstalker.controllers.cellFactories.FolderColumnCellFactory;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.ObservedFolder;

public abstract class AbstractTabController {
    @FXML
    protected ListView<ObservedFolder> foldersTableView;

    @Getter
    protected FolderList folderList;

    private final GraphicsFactory graphicsFactory;

    protected AbstractTabController(GraphicsFactory graphicsFactory) {
        this.graphicsFactory = graphicsFactory;
    }

    @FXML
    public void initialize() {
        prepareColumn();
        prepareDataTableView();
        configureSelectionModelListener();
    }

    private void prepareColumn(){
        foldersTableView.setCellFactory(cell -> new FolderColumnCellFactory(graphicsFactory));
    }

    protected void setModel(FolderList folders) {
        folderList = folders;
        foldersTableView.setItems(folderList);
    }

    public void refresh(){
        foldersTableView.refresh();
    }

    protected abstract void configureSelectionModelListener();

    protected abstract void prepareDataTableView();
}
