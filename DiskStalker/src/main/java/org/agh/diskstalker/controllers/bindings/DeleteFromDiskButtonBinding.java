package org.agh.diskstalker.controllers.bindings;

import javafx.scene.control.TreeTableView;
import org.agh.diskstalker.model.NodeData;

public class DeleteFromDiskButtonBinding extends AbstractButtonBinding {
    public DeleteFromDiskButtonBinding(TreeTableView.TreeTableViewSelectionModel<NodeData> selectionModel) {
        super(
                () -> selectionModel.getSelectedItem() == null,
                selectionModel.selectedItemProperty()
        );
    }
}
