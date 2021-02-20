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
import org.agh.diskstalker.controllers.FilesTypeController;
import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.agh.diskstalker.model.stats.StatsEntry;

//TODO: extract and generify
public class FilesTypeSelectedItemChangeListener implements ChangeListener<IObservedFolder> {
    private final FilesTypeController filesTopController;
    private final TableView<StatsEntry> dataTableView;
    private MapChangeListener<String, StatsEntry>previousListener;
    private ObservableMap<String, StatsEntry> previousMap;

    public FilesTypeSelectedItemChangeListener(FilesTypeController filesTopController) {
        this.filesTopController = filesTopController;
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
        var listener = createListener(items);

        typeStatisticsMap.addListener(listener);
        dataTableView.setItems(items);
        filesTopController.setSortOrder();
        previousMap = typeStatisticsMap;
        previousListener = listener;
    }

    private void clearItems() {
        previousListener = null;
        previousMap = null;
        dataTableView.getItems().clear();
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
        return FXCollections.observableArrayList(
                typeStatistics.values()
        );
    }

    private void clearOldListeners() {
        if(previousMap != null){
            previousMap.removeListener(previousListener);
        }
    }
}
