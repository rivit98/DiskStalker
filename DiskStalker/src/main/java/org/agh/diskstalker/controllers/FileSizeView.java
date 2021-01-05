package org.agh.diskstalker.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@FxmlView("/views/FileSizeView.fxml")
public class FileSizeView {
    @FXML
    private TableView<ObservedFolder> tableViewSizeNames;
    @FXML
    private TableView<NodeData> tableViewSize;

    @FXML
    public void initialize() {
        setSelectionModelListener();
    }

    private void setSelectionModelListener() {
        tableViewSizeNames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                tableViewSize.setItems(newValue.getStatistics().getSizes());
            } else {
                tableViewSize.setItems(null);
            }
        });
    }

    protected void prepareTables(FolderList folders) {
        TableColumn<ObservedFolder, ImageView> iconColumn = new TableColumn<>("");
        TableColumn<ObservedFolder, String> nameColumn = new TableColumn<>("Directory name");
        iconColumn.setPrefWidth(23);
        nameColumn.setPrefWidth(253);

        tableViewSizeNames.getColumns().add(iconColumn);
        tableViewSizeNames.getColumns().add(nameColumn);

        tableViewSizeNames.setItems(folders.get());

        iconColumn.setCellValueFactory(imageview -> new SimpleObjectProperty<>(GraphicsFactory.getGraphic(true)));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<NodeData, Long> sizeColumn = new TableColumn<>("File size(B)");
        TableColumn<NodeData, String> fileColumn = new TableColumn<>("File");
        sizeColumn.setPrefWidth(150);
        fileColumn.setPrefWidth(289);

        tableViewSize.getColumns().add(sizeColumn);
        tableViewSize.getColumns().add(fileColumn);


        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        fileColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    }
}
