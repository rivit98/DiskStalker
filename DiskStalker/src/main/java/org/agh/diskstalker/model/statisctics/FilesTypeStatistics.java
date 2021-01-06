package org.agh.diskstalker.model.statisctics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.agh.diskstalker.model.NodeData;
import org.agh.diskstalker.model.tree.TreeFileNode;

import java.nio.file.Path;
import java.util.HashMap;

public class FilesTypeStatistics {
    private final ObservableList<Type> typeStatistics;
    private final HashMap<Path, TreeFileNode> pathToTreeMap;
    private boolean statisticsSet;

    public FilesTypeStatistics(HashMap<Path, TreeFileNode> pathToTreeMap) {
        typeStatistics = FXCollections.observableArrayList();
        this.pathToTreeMap = pathToTreeMap;
        this.statisticsSet = false;
    }

    public ObservableList<Type> getTypeStatistics() {
        return typeStatistics;
    }

    public boolean isStatisticsSet() {
        return statisticsSet;
    }

    public void setTypeStatistics() {
        statisticsSet = true;
        var typeDetector = new TypeDetector();
        pathToTreeMap.forEach((path, node) -> {
            if(node.getValue().isFile()) {
                var type = typeDetector.detectType(path, typeStatistics);
                node.getValue().setType(type);
            }
        });
    }

    public void addNewNodeType(NodeData node) {
        var typeDetector = new TypeDetector();
        var type = typeDetector.detectType(node.getPath(), typeStatistics);
        node.setType(type);
    }

    public void decrementTypeCounter(NodeData node) {
        var foundedType = typeStatistics.stream()
                .filter(type -> type.getType().equals(node.getType()))
                .findFirst();

        foundedType.ifPresent(type -> {
            type.decrement();
            if(type.getQuantity() == 0) {
                typeStatistics.remove(type);
            }
        });
    }
}
