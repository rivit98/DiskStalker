package org.agh.diskstalker.statistics;

import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.agh.diskstalker.model.tree.NodeData;

public class UpdateRecognizeTypeMessage extends AbstractRecognizeTypeMessage {
    public UpdateRecognizeTypeMessage(IObservedFolder folder, NodeData nodeData) {
        super(folder, nodeData);
    }

    @Override
    public void doAction() {
        folder.getTypeStatistics().update(oldType, type);
    }
}
