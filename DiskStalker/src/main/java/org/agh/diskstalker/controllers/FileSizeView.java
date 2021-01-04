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
@FxmlView("/views/FileSizeView.fxml")
public class FileSizeView {
    @FXML
    private TableView<ObservedFolder> tableViewSize;
    @FXML
    private Button showBiggestFiles;

    @FXML
    public void initialize() {
    }

    protected void prepareTable(FolderList folders) {
        TableColumn<ObservedFolder, ImageView> iconColumn = new TableColumn<>("");
        TableColumn<ObservedFolder, String> nameColumn = new TableColumn<>("Directory name");
        iconColumn.setPrefWidth(23);
        nameColumn.setPrefWidth(342);

        tableViewSize.getColumns().add(iconColumn);
        tableViewSize.getColumns().add(nameColumn);

        tableViewSize.setItems(folders.get());

        iconColumn.setCellValueFactory(imageview -> new SimpleObjectProperty<>(GraphicsFactory.getGraphic(true)));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    }
}
