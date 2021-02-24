package org.agh.diskstalker.controllers.listeners;

import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import org.agh.diskstalker.controllers.FilesTopController;
import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.agh.diskstalker.model.tree.NodeData;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;
import java.util.stream.Collectors;

public class FilesTopSelectedItemChangeListener implements ChangeListener<IObservedFolder> {
    private static final String LOADING_LABEL = "Loading...";
    private final Node originalLabel;
    private final Node loadingLabel;

    private final TableView<NodeData> dataTableView;
    private final ObservableList<NodeData> currentItems;
    private MapChangeListener<Path, TreeFileNode> previousListener;
    private ObservableMap<Path, TreeFileNode> previousMap;

    public FilesTopSelectedItemChangeListener(FilesTopController controller) {
        this.dataTableView = controller.getDataTableView();
        this.originalLabel = dataTableView.getPlaceholder();
        this.loadingLabel = new Label(LOADING_LABEL);
        this.currentItems = FXCollections.<NodeData>observableArrayList(
                node -> new Observable[] {
                        node.getFilenameProperty(),
                        node.getAccumulatedSizeProperty(),
                        node.getModificationDateProperty()
                }
        );
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
        createFileList(nodesMap);
        var sortedItems = createSortedNodeList(currentItems);
        var listener = createListener(currentItems);

        nodesMap.addListener(listener);
        dataTableView.setItems(sortedItems);
        dataTableView.scrollTo(0);

        previousMap = nodesMap;
        previousListener = listener;
    }

    private void clearItems() {
        previousListener = null;
        previousMap = null;
        currentItems.clear();
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

    private void createFileList(ObservableMap<Path, TreeFileNode> nodesMap) {
        currentItems.setAll(
                nodesMap.values().stream()
                .map(TreeItem::getValue)
                .filter(NodeData::isFile)
                .collect(Collectors.toList())
        );
    }

    private SortedList<NodeData> createSortedNodeList(ObservableList<NodeData> items) {
        var sortedList = new SortedList<>(items);
        sortedList.comparatorProperty().bind(dataTableView.comparatorProperty());
        return sortedList;
    }

    private void clearOldListeners() {
        if(previousMap != null){
            previousMap.removeListener(previousListener);
        }
    }
}
