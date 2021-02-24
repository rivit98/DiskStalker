package org.agh.diskstalker.controllers.bindings;

import javafx.scene.control.TreeTableView;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.tree.NodeData;

public class SetLargestFileButtonBinding extends AbstractButtonBooleanBinding {
    public SetLargestFileButtonBinding(
            MainController mainController,
            TreeTableView.TreeTableViewSelectionModel<NodeData> selectionModel
    ) {
        super(
                () -> (
                        !mainController.canSetLimitOnNode(selectionModel.getSelectedItem())
                                || mainController.getLargestFileField().getText().isEmpty()
                ),
                selectionModel.selectedItemProperty(),
                mainController.getLargestFileField().textProperty()
        );
    }
}
