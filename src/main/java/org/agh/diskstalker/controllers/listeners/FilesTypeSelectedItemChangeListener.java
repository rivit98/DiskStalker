package org.agh.diskstalker.controllers.listeners;

import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import org.agh.diskstalker.controllers.FilesTypeController;
import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.agh.diskstalker.model.stats.StatsEntry;

public class FilesTypeSelectedItemChangeListener implements ChangeListener<IObservedFolder> {
    private final TableView<StatsEntry> dataTableView;
    private MapChangeListener<String, StatsEntry> previousListener;
    private ObservableMap<String, StatsEntry> previousMap;
    private SortedList<StatsEntry> prevItems;

    public FilesTypeSelectedItemChangeListener(FilesTypeController filesTopController) {
        this.dataTableView = filesTopController.getDataTableView();
    }

    @Override
    public void changed(ObservableValue<? extends IObservedFolder> observable, IObservedFolder oldValue, IObservedFolder newValue) {
        if (oldValue != null) {
            clearOldListeners();
        }

        if (newValue != null) {
            setItems(newValue);
        } else {
            clearItems();
        }
    }

    private void setItems(IObservedFolder selectedFolder) {
        var typeStatisticsMap = selectedFolder.getTypeStatistics().getStatMap();
        var items = createTypeList(typeStatisticsMap);
        var sortedItems = createSortedTypesList(items);
        var listener = createListener(items);

        typeStatisticsMap.addListener(listener);
        dataTableView.setItems(sortedItems);

        prevItems = sortedItems;
        previousMap = typeStatisticsMap;
        previousListener = listener;
    }

    private void clearItems() {
        previousListener = null;
        previousMap = null;
        prevItems.getSource().clear();
        prevItems = null;
    }

    private MapChangeListener<String, StatsEntry> createListener(ObservableList<StatsEntry> items) {
        return c -> {
            if (c.wasAdded()) {
                items.add(c.getValueAdded());
            } else if (c.wasRemoved()) {
                items.remove(c.getValueRemoved());
            }
        };
    }

    private ObservableList<StatsEntry> createTypeList(ObservableMap<String, StatsEntry> typeStatistics) {
        var list = FXCollections.<StatsEntry>observableArrayList(
                statEntry -> new Observable[] {statEntry.getLongProperty()}
                );
        list.setAll(typeStatistics.values());
        return list;
    }

    private SortedList<StatsEntry> createSortedTypesList(ObservableList<StatsEntry> items) {
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
