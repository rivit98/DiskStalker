package org.agh.diskstalker.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.controllers.cellFactories.DateColumnCellFactory;
import org.agh.diskstalker.controllers.cellFactories.SizeTableColumnCellFactory;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.tree.TreeBuilder;
import org.springframework.stereotype.Component;

import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@FxmlView("/views/FileInfoView.fxml")
public class FileInfoController extends AbstractTabController {
    @FXML
    private TableView<NodeData> dataTableView;
    @FXML
    private TableColumn<NodeData, FileTime> dateColumn;
    @FXML
    private TableColumn<NodeData, Number> sizeColumn;
    @FXML
    private TableColumn<NodeData, String> fileNameColumn;

    protected void configureSelectionModelListener() {
        var selectionModel = foldersTableView.getSelectionModel();
        selectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                Optional.ofNullable(foldersTableView.getSelectionModel().getSelectedItem())
                        .map(ObservedFolder::getTreeBuilder)
                        .map(TreeBuilder::getPathToTreeMap)
                        .map(HashMap::values)
                        .map(coll -> coll.stream().map(TreeItem::getValue))
                        .map(collection -> collection.filter(NodeData::isFile).collect(Collectors.toList()))
                        .ifPresent(treeFileNodes -> {
                            //FIXME: removing files from disk does not update this view
//                            var items = new FilteredList<>(FXCollections.observableArrayList(treeFileNodes), val -> val.getValue().isFile());
                            dataTableView.setItems(FXCollections.observableList(treeFileNodes));
                            dataTableView.getSortOrder().addAll(List.of(dateColumn, sizeColumn));
                        });
            } else {
                dataTableView.getItems().clear();
            }
        });
    }

    protected void prepareDataTableView() {
        dateColumn.setCellFactory(ttc -> new DateColumnCellFactory());
        dateColumn.setCellValueFactory(node -> Optional.ofNullable(node.getValue())
                .map(NodeData::getModificationDate)
                .orElseGet(SimpleObjectProperty::new)
        );

        sizeColumn.setCellFactory(val -> new SizeTableColumnCellFactory());
        sizeColumn.setCellValueFactory(node -> Optional.ofNullable(node.getValue())
                        .map(NodeData::getSizeProperty)
                        .orElse(null)
        );

        fileNameColumn.setCellValueFactory(val -> val.getValue().getFilename());
    }
}