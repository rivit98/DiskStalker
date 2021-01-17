package org.agh.diskstalker.controllers.listeners;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import org.agh.diskstalker.controllers.FileInfoController;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.ObservedFolder;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;
import java.util.stream.Collectors;

public class SelectedItemChangeListener implements ChangeListener<ObservedFolder> {
    private final FileInfoController fileInfoController;
    private final TableView<NodeData> dataTableView;
    private MapChangeListener<Path, TreeFileNode> previousListener;
    private ObservableMap<Path, TreeFileNode> previousMap;

    public SelectedItemChangeListener(FileInfoController fileInfoController) {
        this.fileInfoController = fileInfoController;
        dataTableView = fileInfoController.getDataTableView();
    }

    @Override
    public void changed(ObservableValue<? extends ObservedFolder> observable, ObservedFolder oldValue, ObservedFolder newValue) {
        if (oldValue != null) {
            clearOldListeners();
        }

        if (newValue != null) {
            setItems(newValue);
        } else {
            clearItems();
        }
    }

    private void setItems(ObservedFolder selectedFolder) {
        var nodesMap = selectedFolder.getTreeBuilder().getPathToTreeMap();
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
                        .collect(Collectors.toList())
        );
    }

    private void clearOldListeners() {
        previousMap.removeListener(previousListener);
    }
}
