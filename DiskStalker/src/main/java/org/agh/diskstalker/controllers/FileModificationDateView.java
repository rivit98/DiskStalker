package org.agh.diskstalker.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

@Component
@FxmlView("/views/FileModificationDateView.fxml")
public class FileModificationDateView {
    @FXML
    private TableView<ObservedFolder> tableViewModificationDateNames;
    @FXML
    private TableView<Map.Entry<Path, TreeFileNode>> tableViewModificationDate;

    @FXML
    public void initialize() {
        prepareTableViewModificationDate();
        setSelectionModelListener();
    }

    private void setSelectionModelListener() {
        tableViewModificationDateNames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                var map = tableViewModificationDateNames.getSelectionModel().getSelectedItem().getTreeBuilder().getPathToTreeMap();
                ObservableList<Map.Entry<Path, TreeFileNode>> items = FXCollections.observableArrayList(map.entrySet());
                tableViewModificationDate.setItems(items);
                tableViewModificationDate.getItems().removeIf(val -> val.getValue().getValue().isDirectory());

            } else {
                tableViewModificationDate.setItems(null);
            }
        });
    }

    protected void prepareTableViewModificationDateNames(FolderList folders) {
        TableColumn<ObservedFolder, ImageView> iconColumn = new TableColumn<>("");
        TableColumn<ObservedFolder, String> nameColumn = new TableColumn<>("Directory name");
        iconColumn.setPrefWidth(23);
        nameColumn.setPrefWidth(253);

        tableViewModificationDateNames.getColumns().addAll(iconColumn, nameColumn);

        tableViewModificationDateNames.setItems(folders.get());

        iconColumn.setCellValueFactory(imageview -> new SimpleObjectProperty<>(GraphicsFactory.getGraphic(true)));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    }

    private void prepareTableViewModificationDate() {
        TableColumn<Map.Entry<Path, TreeFileNode>, String> dateColumn = new TableColumn<>("Modification date");
        dateColumn.setCellValueFactory(val -> new SimpleStringProperty(val.getValue().getValue().getValue().getModificationDate()));

        TableColumn<Map.Entry<Path, TreeFileNode>, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(val -> new SimpleStringProperty(val.getValue().getValue().getValue().getName()));

        dateColumn.setPrefWidth(170);
        nameColumn.setPrefWidth(269);

        tableViewModificationDate.getColumns().addAll(dateColumn, nameColumn);
    }
}