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
        prepareDataTableView();
        setSelectionModelListener();
    }

    protected abstract void setSelectionModelListener();

    protected void prepareTabController(FolderList folders) {
        TableColumn<ObservedFolder, ImageView> iconColumn = new TableColumn<>(EMPTY_STRING);
        TableColumn<ObservedFolder, String> nameColumn = new TableColumn<>(DIRECOTRY_COLUMN);
        iconColumn.setPrefWidth(23);
        nameColumn.setPrefWidth(253);

        foldersTableView.getColumns().addAll(List.of(iconColumn, nameColumn));

        foldersTableView.setItems(folders.get());

        iconColumn.setCellValueFactory(imageview -> new SimpleObjectProperty<>(GraphicsFactory.getGraphic(true)));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    }

    protected abstract void prepareDataTableView();
}
