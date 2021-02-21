package org.agh.diskstalker.statistics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.Getter;
import org.agh.diskstalker.model.stats.StatsEntry;

import java.util.Optional;

@Getter
public class TypeStatistics {
    private final ObservableMap<String, StatsEntry> statMap = FXCollections.observableHashMap();

    public void add(String type) {
        var statEntry = statMap.get(type);
        if (statEntry == null) {
            statMap.put(type, new StatsEntry(type, 1L));
        } else {
            statEntry.increment();
        }
    }

    public void remove(String type) {
        Optional.ofNullable(statMap.get(type))
                .ifPresent(statsEntry -> {
                    if (statsEntry.getValue() == 1) {
                        statMap.remove(type);
                    } else {
                        statsEntry.decrement();
                    }
                });
    }

    public void update(String oldType, String newType) {
        if (oldType == null || oldType.equals(newType)) {
            return;
        }

        remove(oldType);
        add(newType);
    }
}
