package org.agh.diskstalker.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.controllers.listeners.FilesTypeSelectedItemChangeListener;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.stats.StatsEntry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@FxmlView("/views/FilesTypeView.fxml")
public class FilesTypeController extends AbstractTabController {
    @FXML @Getter private TableView<StatsEntry> dataTableView;
    @FXML private TableColumn<StatsEntry, String> typeColumn;
    @FXML private TableColumn<StatsEntry, Number> quantityColumn;

    public FilesTypeController(GraphicsFactory graphicsFactory) {
        super(graphicsFactory);
    }

    protected void configureSelectionModelListener() {
        var selectionModel = foldersTableView.getSelectionModel();
        selectionModel.selectedItemProperty().addListener(new FilesTypeSelectedItemChangeListener(this));
    }

    public void setSortOrder() {
        dataTableView.getSortOrder().addAll(List.of(quantityColumn, typeColumn));
    }

    protected void prepareDataTableView() {
        typeColumn.setCellValueFactory(node -> node.getValue().getTypeProperty());

        quantityColumn.setSortType(TableColumn.SortType.DESCENDING);
        quantityColumn.setCellValueFactory(node -> node.getValue().getLongProperty());
    }
}
