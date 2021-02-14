package org.agh.diskstalker.controllers;

import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import lombok.Getter;
import org.agh.diskstalker.controllers.cellFactories.FolderColumnCellFactory;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.ObservedFolder;

import java.util.Comparator;

public abstract class AbstractTabController {
    @FXML
    protected ListView<ObservedFolder> foldersTableView;

    protected SortedList<ObservedFolder> sortedList;

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

    protected void setModel(FolderList folderList) {
        sortedList = new SortedList<>(folderList, Comparator.comparing(ObservedFolder::getName));
        foldersTableView.setItems(sortedList);
    }

    public void refresh(){
        foldersTableView.refresh();
    }

    protected abstract void configureSelectionModelListener();

    protected abstract void prepareDataTableView();
}
