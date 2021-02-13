package org.agh.diskstalker.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.Getter;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.controllers.cellFactories.DateColumnCellFactory;
import org.agh.diskstalker.controllers.cellFactories.SizeTableColumnCellFactory;
import org.agh.diskstalker.controllers.listeners.SelectedItemChangeListener;
import org.agh.diskstalker.graphics.GraphicsFactory;
import org.agh.diskstalker.model.NodeData;
import org.springframework.stereotype.Component;

import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Optional;

@Component
@FxmlView("/views/FileInfoView.fxml")
public class FileInfoController extends AbstractTabController {
    @FXML @Getter
    private TableView<NodeData> dataTableView;
    @FXML
    private TableColumn<NodeData, FileTime> dateColumn;
    @FXML
    private TableColumn<NodeData, Number> sizeColumn;
    @FXML
    private TableColumn<NodeData, String> fileNameColumn;

    protected FileInfoController(GraphicsFactory graphicsFactory) {
        super(graphicsFactory);
    }

    protected void configureSelectionModelListener() {
        var selectionModel = foldersTableView.getSelectionModel();
        selectionModel.selectedItemProperty().addListener(new SelectedItemChangeListener(this));
    }

    public void setSortOrder(){
        dataTableView.getSortOrder().addAll(List.of(dateColumn, sizeColumn, fileNameColumn));
    }

    protected void prepareDataTableView() {
        dateColumn.setCellFactory(ttc -> new DateColumnCellFactory());
        dateColumn.setCellValueFactory(node -> Optional.ofNullable(node.getValue())
                .map(NodeData::getModificationDateProperty)
                .orElseGet(SimpleObjectProperty::new)
        );

        sizeColumn.setCellFactory(val -> new SizeTableColumnCellFactory());
        sizeColumn.setSortType(TableColumn.SortType.DESCENDING);
        sizeColumn.setCellValueFactory(node -> Optional.ofNullable(node.getValue())
                        .map(NodeData::getAccumulatedSizeProperty)
                        .orElse(null)
        );

        fileNameColumn.setCellValueFactory(val -> val.getValue().getFilename());
    }
}

//TODO: sth is wrong with date sorting, probably miliseconds
//TODO: only biggest 50 files