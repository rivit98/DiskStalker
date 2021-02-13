package org.agh.diskstalker.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.statisctics.Type;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@FxmlView("/views/FilesTypeView.fxml")
public class FilesTypeController extends AbstractTabController {
    @FXML
    private TableView<Type> dataTableView;
    @FXML
    private TableColumn<Type, Integer> quantityColumn;
    @FXML
    private TableColumn<Type, String> typeColumn;

    public FilesTypeController(GraphicsFactory graphicsFactory) {
        super(graphicsFactory);
    }

    protected void configureSelectionModelListener() {
        foldersTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                dataTableView.setItems(newValue.getFilesTypeStatistics().getTypeStatistics());
                dataTableView.getSortOrder().addAll(Collections.singletonList(quantityColumn));
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
