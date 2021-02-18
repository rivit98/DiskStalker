package org.agh.diskstalker.controllers.bindings;

import javafx.scene.control.TreeTableView;
import org.agh.diskstalker.model.tree.NodeData;

public class DeleteFromDiskButtonBinding extends AbstractButtonBooleanBinding {
    public DeleteFromDiskButtonBinding(TreeTableView.TreeTableViewSelectionModel<NodeData> selectionModel) {
        super(
                () -> selectionModel.getSelectedItem() == null,
                selectionModel.selectedItemProperty()
        );
    }
}
