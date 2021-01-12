package org.agh.diskstalker.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Component
@FxmlView("/views/FileInfoView.fxml")
public class FileInfoViewController extends AbstractTabController {
    @FXML
    private TableView<Map.Entry<Path, TreeFileNode>> dataTableView;
    @FXML
    private TableColumn<Map.Entry<Path, TreeFileNode>, String> dateColumn;
    @FXML
    private TableColumn<Map.Entry<Path, TreeFileNode>, String> sizeColumn;
    @FXML
    private TableColumn<Map.Entry<Path, TreeFileNode>, String> fileNameColumn;

    //FIXME:maybe there is fastest way to compare?
    private class SizeComparator implements Comparator<String> {
        private final HashMap<String, Integer> comparingMap;

        private SizeComparator() {
            comparingMap = new HashMap<>();
            comparingMap.put("bytes", 1);
            comparingMap.put("KB", 2);
            comparingMap.put("MB", 3);
            comparingMap.put("GB", 4);

        }

        @Override
        public int compare(String s1, String s2) {
            var firstSize = s1.split(" ");
            var secondSize = s2.split(" ");

            var val1 = comparingMap.get(firstSize[1]);
            var val2 = comparingMap.get(secondSize[1]);

            if(val1 < val2) {
                return 1;
            } else if(val1 == val2) {
                var res = Long.parseLong(firstSize[0]);
                var res2 = Long.parseLong(secondSize[0]);
                return Long.compare(res2, res);
            } else return -1;
        }
    }

    protected void configureSelectionModelListener() {
        foldersTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                var map = foldersTableView.getSelectionModel().getSelectedItem().getTreeBuilder().getPathToTreeMap();
                ObservableList<Map.Entry<Path, TreeFileNode>> items = FXCollections.observableArrayList(map.entrySet());
                dataTableView.setItems(items);
                dataTableView.getItems().removeIf(val -> val.getValue().getValue().isDirectory());
                dataTableView.getSortOrder().addAll(dateColumn, sizeColumn);
            } else {
                dataTableView.getItems().clear();
            }
        });
    }

    protected void prepareDataTableView() {
        dateColumn.setCellValueFactory(val -> new SimpleStringProperty(val.getValue().getValue().getValue().getModificationDate()));
        sizeColumn.setCellValueFactory(val -> new SimpleStringProperty(FileUtils.byteCountToDisplaySize(val.getValue().getValue().getValue().getSize())));
        sizeColumn.setComparator(new SizeComparator());
        fileNameColumn.setCellValueFactory(val -> new SimpleStringProperty(val.getValue().getValue().getValue().getName()));
    }
}