package org.agh.diskstalker.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.FolderList;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.statisctics.Type;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/views/FileTypeView.fxml")
public class FileTypeView {
    @FXML
    private TableView<ObservedFolder> tableViewTypeNames;
    @FXML
    private TableView<Type> tableViewType;

    @FXML
    public void initialize() {
        setSelectionModelListener();
    }

    private void setSelectionModelListener() {
        tableViewTypeNames.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                tableViewType.setItems(newValue.getStatistics().getTypes());
            } else {
                tableViewType.setItems(null);
            }
        });
    }

    protected void prepareTables(FolderList folders) {
        TableColumn<ObservedFolder, ImageView> iconColumn = new TableColumn<>("");
        TableColumn<ObservedFolder, String> nameColumn = new TableColumn<>("Directory name");
        iconColumn.setPrefWidth(23);
        nameColumn.setPrefWidth(253);

        tableViewTypeNames.getColumns().add(iconColumn);
        tableViewTypeNames.getColumns().add(nameColumn);

        tableViewTypeNames.setItems(folders.get());

        iconColumn.setCellValueFactory(imageview -> new SimpleObjectProperty<>(GraphicsFactory.getGraphic(true)));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Type, Integer> quantityColumn = new TableColumn<>("Numer of files");
        TableColumn<Type, String> typeColumn = new TableColumn<>("File type");
        quantityColumn.setPrefWidth(150);
        typeColumn.setPrefWidth(289);

        tableViewType.getColumns().add(quantityColumn);
        tableViewType.getColumns().add(typeColumn);


        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
    }
}
