package org.agh.diskstalker.statistics;

import lombok.Data;
import org.agh.diskstalker.model.interfaces.IObservedFolder;
import org.agh.diskstalker.model.tree.NodeData;

@Data
public abstract class AbstractRecognizeTypeMessage {
    public static final String UNKNOWN_TYPE = "unknown";

    protected String type;
    protected String oldType;
    protected final IObservedFolder folder;
    protected final NodeData nodeData;

    public AbstractRecognizeTypeMessage(IObservedFolder folder, NodeData nodeData) {
        this.folder = folder;
        this.nodeData = nodeData;
        this.oldType = nodeData.getType();
    }

    public abstract void doAction();
}
