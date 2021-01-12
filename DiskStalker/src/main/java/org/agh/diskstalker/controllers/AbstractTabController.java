package org.agh.diskstalker.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.ObservedFolder;

public abstract class AbstractTabController {
    @FXML
    protected TableView<ObservedFolder> foldersTableView;
    @FXML
    protected TableColumn<ObservedFolder, ImageView> iconColumn;
    @FXML
    protected TableColumn<ObservedFolder, String> nameColumn;

    @FXML
    public void initialize() {
        prepareColumns();
        prepareDataTableView();
        configureSelectionModelListener();
    }

    private void prepareColumns(){
        iconColumn.setCellValueFactory(imageview -> new SimpleObjectProperty<>(GraphicsFactory.getGraphic(true)));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    }

    protected void setModel(FolderList folders) {
        foldersTableView.setItems(folders.get());
    }

    protected abstract void configureSelectionModelListener();

    protected abstract void prepareDataTableView();
}
