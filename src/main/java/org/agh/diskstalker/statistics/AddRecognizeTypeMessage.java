package org.agh.diskstalker.statistics;

import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.agh.diskstalker.model.tree.NodeData;

public class AddRecognizeTypeMessage extends AbstractRecognizeTypeMessage {
    public AddRecognizeTypeMessage(IObservedFolder folder, NodeData nodeData) {
        super(folder, nodeData);
    }

    @Override
    public void doAction() {
        folder.getTypeStatistics().add(type);
    }
}
