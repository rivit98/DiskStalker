package org.agh.diskstalker.controllers.listeners;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import org.agh.diskstalker.controllers.FilesTopController;
import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.agh.diskstalker.model.tree.NodeData;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

public class FilesTopSelectedItemChangeListener implements ChangeListener<IObservedFolder> {
    private static final String LOADING_LABEL = "Loading...";
    private final FilesTopController filesTopController;
    private final TableView<NodeData> dataTableView;
    private final Node originalLabel;
    private final Node loadingLabel;
    private MapChangeListener<Path, TreeFileNode> previousListener;
    private ObservableMap<Path, TreeFileNode> previousMap;

    public FilesTopSelectedItemChangeListener(FilesTopController filesTopController) {
        this.filesTopController = filesTopController;
        this.dataTableView = filesTopController.getDataTableView();
        this.originalLabel = dataTableView.getPlaceholder();
        this.loadingLabel = new Label(LOADING_LABEL);
    }

    @Override
    public void changed(ObservableValue<? extends IObservedFolder> observable, IObservedFolder oldValue, IObservedFolder newValue) {
        if (oldValue != null) {
            clearOldListeners();
        }

        if (newValue != null && !newValue.isScanning()) {
            setItems(newValue);
        } else {
            dataTableView.setPlaceholder((newValue != null) ? loadingLabel : originalLabel);
            clearItems();
        }
    }

    private void setItems(IObservedFolder selectedFolder) {
        var nodesMap = selectedFolder.getNodesTree().getPathToTreeMap();
        var items = createFileList(nodesMap);
        var listener = createListener(items);

        nodesMap.addListener(listener);
        dataTableView.setItems(items);
        filesTopController.setSortOrder();
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
