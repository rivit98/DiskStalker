package org.agh.diskstalker.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
@FxmlView("/views/FileSizeView.fxml")
public class FileSizeViewController extends AbstractTabController {
    @FXML
    private TableView<Map.Entry<Path,TreeFileNode>> dataTableView;

    protected void setSelectionModelListener() {
        foldersTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                var map = foldersTableView.getSelectionModel().getSelectedItem().getTreeBuilder().getPathToTreeMap();
                ObservableList<Map.Entry<Path, TreeFileNode>> items = FXCollections.observableArrayList(map.entrySet());
                dataTableView.setItems(items);
                dataTableView.getItems().removeIf(val -> val.getValue().getValue().isDirectory());

            } else {
                dataTableView.getItems().clear();
            }
        });
    }

    protected void prepareDataTableView() {
        TableColumn<Map.Entry<Path, TreeFileNode>, String> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(val -> new SimpleStringProperty(FileUtils.byteCountToDisplaySize(val.getValue().getValue().getValue().getSize())));

        TableColumn<Map.Entry<Path, TreeFileNode>, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(val -> new SimpleStringProperty(val.getValue().getValue().getValue().getName()));

        sizeColumn.setPrefWidth(120);
        nameColumn.setPrefWidth(319);

        dataTableView.getColumns().addAll(List.of(sizeColumn, nameColumn));
    }
}
