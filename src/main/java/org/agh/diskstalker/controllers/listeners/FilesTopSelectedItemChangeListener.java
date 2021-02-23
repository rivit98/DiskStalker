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
import org.agh.diskstalker.model.stats.StatsEntry;
import org.agh.diskstalker.model.tree.NodeData;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

public class FilesTopSelectedItemChangeListener implements ChangeListener<IObservedFolder> {
    private static final String LOADING_LABEL = "Loading...";
    private final Node originalLabel;
    private final Node loadingLabel;

    private final TableView<NodeData> dataTableView;
    private MapChangeListener<Path, TreeFileNode> previousListener;
    private ObservableMap<Path, TreeFileNode> previousMap;
    private SortedList<NodeData> prevItems;

    private static final Comparator<NodeData> comparator =
            (o1, o2) -> Comparator
                    .comparingLong(NodeData::getSize).reversed()
                    .thenComparing(NodeData::getModificationTime)
                    .thenComparing(NodeData::getFileName)
                    .compare(o1, o2);

    public FilesTopSelectedItemChangeListener(FilesTopController controller) {
        this.dataTableView = controller.getDataTableView();
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
        var sortedItems = createSortedNodeList(items);
        var listener = createListener(items);

        nodesMap.addListener(listener);
        dataTableView.setItems(sortedItems);

        prevItems = sortedItems;
        previousMap = nodesMap;
        previousListener = listener;
    }

    private void clearItems() {
        previousListener = null;
        previousMap = null;
        if(prevItems != null){
            prevItems.getSource().clear();
            prevItems = null;
        }
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
        var list = FXCollections.<NodeData>observableArrayList(
                node -> new Observable[] {
                        node.getFilenameProperty(),
                        node.getAccumulatedSizeProperty(),
                        node.getModificationDateProperty()
                }
        );

        list.setAll(
                nodesMap.values().stream()
                .map(TreeItem::getValue)
                .filter(NodeData::isFile)
                .sorted(comparator)
                .limit(100)
                .collect(Collectors.toList())
        );

        return list;
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
