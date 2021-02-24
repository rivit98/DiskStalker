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
    private final ObservableList<StatsEntry> currentItems;
    private MapChangeListener<String, StatsEntry> previousListener;
    private ObservableMap<String, StatsEntry> previousMap;

    public FilesTypeSelectedItemChangeListener(FilesTypeController controller) {
        this.dataTableView = controller.getDataTableView();
        this.currentItems = FXCollections.observableArrayList(
                statEntry -> new Observable[]{statEntry.getLongProperty()}
        );
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
        createTypeList(typeStatisticsMap);
        var sortedItems = createSortedTypesList(currentItems);
        var listener = createListener(currentItems);

        typeStatisticsMap.addListener(listener);
        dataTableView.setItems(sortedItems);
        dataTableView.scrollTo(0);

        previousMap = typeStatisticsMap;
        previousListener = listener;
    }

    private void clearItems() {
        previousListener = null;
        previousMap = null;
        currentItems.clear();
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

    private void createTypeList(ObservableMap<String, StatsEntry> typeStatistics) {
        currentItems.setAll(typeStatistics.values());
    }

    private SortedList<StatsEntry> createSortedTypesList(ObservableList<StatsEntry> items) {
        var sortedList = new SortedList<>(items);
        sortedList.comparatorProperty().bind(dataTableView.comparatorProperty());
        return sortedList;
    }

    private void clearOldListeners() {
        if (previousMap != null) {
            previousMap.removeListener(previousListener);
        }
    }
}
