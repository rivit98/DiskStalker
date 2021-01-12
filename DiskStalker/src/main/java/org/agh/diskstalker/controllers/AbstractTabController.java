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

import java.util.List;

public abstract class AbstractTabController {
    private final String EMPTY_STRING = "";
    private final String DIRECOTRY_COLUMN = "Directory name";

    @FXML
    protected TableView<ObservedFolder> foldersTableView;

    @FXML
    public void initialize() {
        prepareColumns();
        prepareDataTableView();
        setSelectionModelListener();
    }

    private void prepareColumns(){
        var iconColumn = new TableColumn<ObservedFolder, ImageView>(EMPTY_STRING);
        var nameColumn = new TableColumn<ObservedFolder, String>(DIRECOTRY_COLUMN);

        iconColumn.setPrefWidth(23);
        nameColumn.setPrefWidth(253);

        iconColumn.setCellValueFactory(imageview -> new SimpleObjectProperty<>(GraphicsFactory.getGraphic(true)));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        foldersTableView.getColumns().addAll(List.of(iconColumn, nameColumn));
    }

    protected void setModel(FolderList folders) {
        foldersTableView.setItems(folders.get());
    }

    protected abstract void setSelectionModelListener();

    protected abstract void prepareDataTableView();
}
