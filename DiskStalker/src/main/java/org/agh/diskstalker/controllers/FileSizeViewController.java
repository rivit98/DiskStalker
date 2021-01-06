package org.agh.diskstalker.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.rgielen.fxweaver.core.FxmlView;
import org.agh.diskstalker.model.tree.TreeFileNode;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;

@Component
@FxmlView("/views/FileSizeView.fxml")
public class FileSizeViewController extends AbstractTabController {
    @FXML
    private TableView<Map.Entry<Path,TreeFileNode>> dataTableView;

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

    protected void setSelectionModelListener() {
        foldersTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                var map = foldersTableView.getSelectionModel().getSelectedItem().getTreeBuilder().getPathToTreeMap();
                ObservableList<Map.Entry<Path, TreeFileNode>> items = FXCollections.observableArrayList(map.entrySet());
                dataTableView.setItems(items);
                dataTableView.getItems().removeIf(val -> val.getValue().getValue().isDirectory());

            } else {
                dataTableView.getItems().clear();
            }
        });
    }

    protected void prepareDataTableView() {
        TableColumn<Map.Entry<Path, TreeFileNode>, String> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(val -> new SimpleStringProperty(FileUtils.byteCountToDisplaySize(val.getValue().getValue().getValue().getSize())));
        sizeColumn.setComparator(new SizeComparator());

        TableColumn<Map.Entry<Path, TreeFileNode>, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(val -> new SimpleStringProperty(val.getValue().getValue().getValue().getName()));

        sizeColumn.setPrefWidth(120);
        nameColumn.setPrefWidth(319);

        dataTableView.getColumns().addAll(List.of(sizeColumn, nameColumn));
    }
}
