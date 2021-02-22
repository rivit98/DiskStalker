package org.agh.diskstalker.statistics.messages;

import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.agh.diskstalker.model.tree.NodeData;

public class RemoveRecognizeTypeMessage extends AbstractRecognizeTypeMessage {
    public RemoveRecognizeTypeMessage(IObservedFolder folder, NodeData nodeData) {
        super(folder, nodeData);
    }

    @Override
    public void doAction() {
        folder.getTypeStatistics().remove(oldType);
    }
}
