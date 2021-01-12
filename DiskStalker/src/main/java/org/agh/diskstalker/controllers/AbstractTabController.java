package org.agh.diskstalker.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.ObservedFolder;

public abstract class AbstractTabController {
    @FXML
    protected TableView<ObservedFolder> foldersTableView;
    @FXML
    protected TableColumn<ObservedFolder, String> nameColumn;

    @FXML
    public void initialize() {
        prepareColumn();
        prepareDataTableView();
        configureSelectionModelListener();
    }

    private void prepareColumn(){
        nameColumn.setCellFactory(cell -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if(empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(name);
                    setGraphic(GraphicsFactory.getGraphic(true, false));
                }
            }
        });
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    }

    protected void setModel(FolderList folders) {
        foldersTableView.setItems(folders.get());
    }

    protected abstract void configureSelectionModelListener();

    protected abstract void prepareDataTableView();
}
