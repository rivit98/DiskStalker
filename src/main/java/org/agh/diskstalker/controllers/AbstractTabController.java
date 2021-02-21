package org.agh.diskstalker.controllers;

import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.agh.diskstalker.controllers.cellFactories.FolderColumnCellFactory;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.interfaces.ILimitableObservableFolder;
import org.agh.diskstalker.model.interfaces.IObservedFolder;

import java.util.Comparator;

public abstract class AbstractTabController {
    @FXML
    protected ListView<ILimitableObservableFolder> foldersTableView;

    protected FolderList folderList;
    protected SortedList<ILimitableObservableFolder> sortedList;

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
        this.folderList = folderList;
        sortedList = new SortedList<>(folderList, Comparator.comparing(IObservedFolder::getName));
        foldersTableView.setItems(sortedList);
    }

    public void refresh(){
        foldersTableView.refresh();
        refreshSelection();
    }

    public void refreshSelection(){
        var selectionModel = foldersTableView.getSelectionModel();
        var item = selectionModel.getSelectedItem();
        if(item != null){ //TODO: figure out better solution
            selectionModel.clearSelection();
            selectionModel.select(item);
        }
    }

    protected abstract void configureSelectionModelListener();

    protected abstract void prepareDataTableView();

    protected abstract void setSortOrder();
}
