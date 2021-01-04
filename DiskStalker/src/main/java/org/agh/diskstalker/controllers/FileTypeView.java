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
import org.springframework.stereotype.Component;

@Component
@FxmlView("/views/FileTypeView.fxml")
public class FileTypeView {
    @FXML
    private TableView<ObservedFolder> tableViewType;
    @FXML
    private Button showTypes;

    @FXML
    public void initialize() {
    }

    protected void prepareTable(FolderList folders) {
        TableColumn<ObservedFolder, ImageView> iconColumn = new TableColumn<>("");
        TableColumn<ObservedFolder, String> nameColumn = new TableColumn<>("Directory name");
        iconColumn.setPrefWidth(23);
        nameColumn.setPrefWidth(342);

        tableViewType.getColumns().add(iconColumn);
        tableViewType.getColumns().add(nameColumn);

        tableViewType.setItems(folders.get());

        iconColumn.setCellValueFactory(imageview -> new SimpleObjectProperty<>(GraphicsFactory.getGraphic(true)));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

    }
}
