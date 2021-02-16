package org.agh.diskstalker.controllers.listeners;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import org.agh.diskstalker.controllers.FileInfoController;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

public class SelectedItemChangeListener implements ChangeListener<ObservedFolder> {
    private static final String LOADING_LABEL = "Loading...";
    private final FileInfoController fileInfoController;
    private final TableView<NodeData> dataTableView;
    private MapChangeListener<Path, TreeFileNode> previousListener;
    private ObservableMap<Path, TreeFileNode> previousMap;

    public SelectedItemChangeListener(FileInfoController fileInfoController) {
        this.fileInfoController = fileInfoController;
        this.dataTableView = fileInfoController.getDataTableView();
    }

    @Override
    public void changed(ObservableValue<? extends ObservedFolder> observable, ObservedFolder oldValue, ObservedFolder newValue) {
        if (oldValue != null) {
            clearOldListeners();
        }

        if (newValue != null && !newValue.isScanning()) {
            setItems(newValue);
        } else {
            dataTableView.setPlaceholder(new Label(LOADING_LABEL));
            clearItems();
        }
    }

    private void setItems(ObservedFolder selectedFolder) {
        var nodesMap = selectedFolder.getNodesTree().getPathToTreeMap();
        var items = createFileList(nodesMap);
        var listener = createListener(items);

        nodesMap.addListener(listener);
        dataTableView.setItems(items);
        fileInfoController.setSortOrder();
        previousMap = nodesMap;
        previousListener = listener;
    }

    private void clearItems() {
        previousListener = null;
        previousMap = null;
        dataTableView.getItems().clear();
    }

    private MapChangeListener<Path, TreeFileNode> createListener(ObservableList<NodeData> items) {
        return c -> {
            if (c.wasAdded()) {
                items.add(c.getValueAdded().getValue());
            } else if (c.wasRemoved()) {
                items.remove(c.getValueRemoved().getValue());
            }
        };
    }

    private ObservableList<NodeData> createFileList(ObservableMap<Path, TreeFileNode> nodesMap) {
        return FXCollections.observableArrayList(
                nodesMap.values().stream()
                        .map(TreeItem::getValue)
                        .filter(NodeData::isFile)
                        .sorted((o1, o2) -> Comparator.comparingLong(NodeData::getSize).reversed().compare(o1, o2))
                        .limit(100)
                        .collect(Collectors.toList())
        );
    }

    private void clearOldListeners() {
        if(previousMap != null){
            previousMap.removeListener(previousListener);
        }
    }
}
