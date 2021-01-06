package org.agh.diskstalker.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.model.statisctics.Type;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/views/FileTypeView.fxml")
public class FileTypeViewController extends AbstractTabController {
    @FXML
    private TableView<Type> dataTableView;

    protected void setSelectionModelListener() {
        foldersTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                dataTableView.setItems(newValue.getFilesTypeStatistics().getTypeStatistics());
            } else {
                dataTableView.setItems(null);
            }
        });
    }

    protected void prepareDataTableView() {
        TableColumn<Type, Integer> quantityColumn = new TableColumn<>("Numer of files");
        TableColumn<Type, String> typeColumn = new TableColumn<>("File type");
        quantityColumn.setPrefWidth(150);
        typeColumn.setPrefWidth(289);

        dataTableView.getColumns().addAll(quantityColumn, typeColumn);

        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
    }
}
