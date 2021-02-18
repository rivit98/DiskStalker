package org.agh.diskstalker.controllers.bindings;

import javafx.scene.control.TreeTableView;
import org.agh.diskstalker.controllers.MainController;
import org.agh.diskstalker.model.tree.NodeData;

public class SetBiggestFileButtonBinding extends AbstractButtonBooleanBinding {
    public SetBiggestFileButtonBinding(
            MainController mainController,
            TreeTableView.TreeTableViewSelectionModel<NodeData> selectionModel
    ) {
        super(
                () -> (
                        !mainController.canSetLimitOnNode(selectionModel.getSelectedItem())
                                || mainController.getBiggestFileField().getText().isEmpty()
                ),
                selectionModel.selectedItemProperty(),
                mainController.getBiggestFileField().textProperty()
        );
    }
}
