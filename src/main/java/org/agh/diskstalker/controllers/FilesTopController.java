package org.agh.diskstalker.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.controllers.cellFactories.DateColumnCellFactory;
import org.agh.diskstalker.controllers.cellFactories.SizeTableColumnCellFactory;
import org.agh.diskstalker.controllers.listeners.FilesTopSelectedItemChangeListener;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.tree.NodeData;
import org.springframework.stereotype.Component;

import java.nio.file.attribute.FileTime;
import java.util.List;

@Component
@FxmlView("/views/FileInfoView.fxml")
public class FilesTopController extends AbstractTabController {
    @FXML @Getter private TableView<NodeData> dataTableView;
    @FXML private TableColumn<NodeData, FileTime> dateColumn;
    @FXML private TableColumn<NodeData, Number> sizeColumn;
    @FXML private TableColumn<NodeData, String> fileNameColumn;

    protected FilesTopController(GraphicsFactory graphicsFactory) {
        super(graphicsFactory);
    }

    protected void configureSelectionModelListener() {
        var selectionModel = foldersTableView.getSelectionModel();
        selectionModel.selectedItemProperty().addListener(new FilesTopSelectedItemChangeListener(this));
    }

    public void setSortOrder() {
        dataTableView.getSortOrder().addAll(List.of(sizeColumn, fileNameColumn, dateColumn));
    }

    protected void prepareDataTableView() {
        dateColumn.setCellFactory(ttc -> new DateColumnCellFactory());
        dateColumn.setCellValueFactory(node -> node.getValue().getModificationDateProperty());

        sizeColumn.setCellFactory(val -> new SizeTableColumnCellFactory());
        sizeColumn.setSortType(TableColumn.SortType.DESCENDING);
        sizeColumn.setCellValueFactory(node -> node.getValue().getAccumulatedSizeProperty());

        fileNameColumn.setCellValueFactory(node -> node.getValue().getFilenameProperty());

        setSortOrder();
    }
}
