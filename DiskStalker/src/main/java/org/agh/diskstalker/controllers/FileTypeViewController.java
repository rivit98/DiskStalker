package org.agh.diskstalker.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.model.statisctics.Type;
import org.springframework.stereotype.Component;

@Component
@FxmlView("/views/FileTypeView.fxml")
public class FileTypeViewController extends AbstractTabController {
    @FXML
    private TableView<Type> dataTableView;
    @FXML
    private TableColumn<Type, Integer> quantityColumn;
    @FXML
    private TableColumn<Type, String> typeColumn;


    protected void configureSelectionModelListener() {
        foldersTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                dataTableView.setItems(newValue.getFilesTypeStatistics().getTypeStatistics());
            } else {
                dataTableView.getItems().clear();
            }
        });
    }

    protected void prepareDataTableView() {
        quantityColumn.setCellValueFactory(val -> val.getValue().getQuantityProperty().asObject());
        typeColumn.setCellValueFactory(val -> val.getValue().getTypeProperty());
    }
}
