package org.agh.diskstalker.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/views/FileModificationDateView.fxml")
public class FileModificationDateView {
    @FXML
    public TableView<ObservedFolder> tableViewModificationDateNames;
    @FXML
    private TableView<NodeData> tableViewModificationDate;

    @FXML
    public void initialize() {
        setSelectionModelListener();
    }

    private void setSelectionModelListener() {
        tableViewModificationDateNames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                tableViewModificationDate.setItems(newValue.getStatistics().getModificationDates());
            } else {
                tableViewModificationDate.setItems(null);
            }
        });
    }

    protected void prepareTables(FolderList folders) {
        TableColumn<ObservedFolder, ImageView> iconColumn = new TableColumn<>("");
        TableColumn<ObservedFolder, String> nameColumn = new TableColumn<>("Directory name");
        iconColumn.setPrefWidth(23);
        nameColumn.setPrefWidth(253);

        tableViewModificationDateNames.getColumns().add(iconColumn);
        tableViewModificationDateNames.getColumns().add(nameColumn);

        tableViewModificationDateNames.setItems(folders.get());

        iconColumn.setCellValueFactory(imageview -> new SimpleObjectProperty<>(GraphicsFactory.getGraphic(true)));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<NodeData, String> modificationDateColumn = new TableColumn<>("Last modification date");
        TableColumn<NodeData, String> fileColumn = new TableColumn<>("File");
        modificationDateColumn.setPrefWidth(239);
        fileColumn.setPrefWidth(200);

        tableViewModificationDate.getColumns().add(modificationDateColumn);
        tableViewModificationDate.getColumns().add(fileColumn);


        modificationDateColumn.setCellValueFactory(new PropertyValueFactory<>("modification"));
        fileColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    }
}