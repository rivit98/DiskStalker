package org.agh.diskstalker.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

@Component
@FxmlView("/views/FileModificationDateView.fxml")
public class FileModificationDateViewController extends AbstractTabController {
    @FXML
    private TableView<Map.Entry<Path, TreeFileNode>> dataTableView;

    protected void setSelectionModelListener() {
        foldersTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                var map = foldersTableView.getSelectionModel().getSelectedItem().getTreeBuilder().getPathToTreeMap();
                ObservableList<Map.Entry<Path, TreeFileNode>> items = FXCollections.observableArrayList(map.entrySet());
                dataTableView.setItems(items);
                dataTableView.getItems().removeIf(val -> val.getValue().getValue().isDirectory());
            } else {
                dataTableView.setItems(null);
            }
        });
    }

    protected void prepareDataTableView() {
        TableColumn<Map.Entry<Path, TreeFileNode>, String> dateColumn = new TableColumn<>("Modification date");
        dateColumn.setCellValueFactory(val -> new SimpleStringProperty(val.getValue().getValue().getValue().getModificationDate()));

        TableColumn<Map.Entry<Path, TreeFileNode>, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(val -> new SimpleStringProperty(val.getValue().getValue().getValue().getName()));

        dateColumn.setPrefWidth(170);
        nameColumn.setPrefWidth(269);

        dataTableView.getColumns().addAll(dateColumn, nameColumn);
    }
}