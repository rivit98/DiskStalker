package org.agh.diskstalker.comparators;

import org.agh.diskstalker.model.NodeData;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class NodeDataComparator {
    private NodeDataComparator() {

    }

    public static Comparator<NodeData> getComparator() {
        return Comparator
                .comparingLong(NodeData::getAccumulatedSize)
                .reversed()
                .thenComparing(nodeData -> nodeData
                        .getFilename()
                        .get()
                        .toLowerCase());
    }
}
