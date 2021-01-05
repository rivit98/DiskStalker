package org.agh.diskstalker.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

@Component
@FxmlView("/views/FileSizeView.fxml")
public class FileSizeView {
    @FXML
    private TableView<ObservedFolder> tableViewSizeNames;
    @FXML
    private TableView<Map.Entry<Path,TreeFileNode>> tableViewSize;

    @FXML
    public void initialize() {
        prepareTableViewSize();
        setSelectionModelListener();
    }

    private void setSelectionModelListener() {
        tableViewSizeNames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                var map = tableViewSizeNames.getSelectionModel().getSelectedItem().getTreeBuilder().getPathToTreeMap();
                ObservableList<Map.Entry<Path, TreeFileNode>> items = FXCollections.observableArrayList(map.entrySet());
                tableViewSize.setItems(items);
                tableViewSize.getItems().removeIf(val -> val.getValue().getValue().isDirectory());

            } else {
                tableViewSize.setItems(null);
            }
        });
    }

    protected void prepareTableViewSizeNames(FolderList folders) {
        TableColumn<ObservedFolder, ImageView> iconColumn = new TableColumn<>("");
        TableColumn<ObservedFolder, String> nameColumn = new TableColumn<>("Directory name");
        iconColumn.setPrefWidth(23);
        nameColumn.setPrefWidth(253);

        tableViewSizeNames.getColumns().addAll(iconColumn, nameColumn);

        tableViewSizeNames.setItems(folders.get());

        iconColumn.setCellValueFactory(imageview -> new SimpleObjectProperty<>(GraphicsFactory.getGraphic(true)));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    }

    private void prepareTableViewSize() {
        TableColumn<Map.Entry<Path, TreeFileNode>, String> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(val -> new SimpleStringProperty(FileUtils.byteCountToDisplaySize(val.getValue().getValue().getValue().getSize())));

        TableColumn<Map.Entry<Path, TreeFileNode>, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(val -> new SimpleStringProperty(val.getValue().getValue().getValue().getName()));

        sizeColumn.setPrefWidth(120);
        nameColumn.setPrefWidth(319);

        tableViewSize.getColumns().addAll(sizeColumn, nameColumn);
    }
}
